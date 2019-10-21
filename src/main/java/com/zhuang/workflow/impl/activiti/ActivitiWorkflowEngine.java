package com.zhuang.workflow.impl.activiti;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zhuang.workflow.impl.activiti.manager.ProcessDefinitionManager;
import com.zhuang.workflow.impl.activiti.manager.ProcessInstanceManager;
import com.zhuang.workflow.impl.activiti.manager.ProcessVariablesManager;
import com.zhuang.workflow.impl.activiti.manager.UserTaskManager;
import com.zhuang.workflow.model.UserInfo;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhuang.workflow.AbstractWorkflowEngine;
import com.zhuang.workflow.NextTaskUsersHandler;
import com.zhuang.workflow.WorkflowActionListener;
import com.zhuang.workflow.WorkflowEngineContext;
import com.zhuang.workflow.enums.ProcessMainVariableNames;
import com.zhuang.workflow.enums.CommonVariableNames;
import com.zhuang.workflow.enums.CountersignVariableNames;
import com.zhuang.workflow.enums.FormDataVariableNames;
import com.zhuang.workflow.exception.HandlerNotFoundException;
import com.zhuang.workflow.model.NextTaskInfo;
import com.zhuang.workflow.model.TaskDefModel;
import com.zhuang.workflow.enums.WorkflowChoiceOptions;
import com.zhuang.workflow.service.UserManagementService;

public class ActivitiWorkflowEngine extends AbstractWorkflowEngine {

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private FormService formService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private ProcessDefinitionManager processDefinitionManager;
    @Autowired
    private ProcessVariablesManager processVariablesManager;
    @Autowired
    private ProcessInstanceManager processInstanceManager;
    @Autowired
    private UserTaskManager userTaskManager;
    @Autowired
    private UserManagementService userManagementService;

    public static final String ACTIVITI_ENV_VAR_KEY_PREFIX = "env_";

    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public RuntimeService getRuntimeService() {
        return runtimeService;
    }

    public FormService getFormService() {
        return formService;
    }

    public IdentityService getIdentityService() {
        return identityService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public HistoryService getHistoryService() {
        return historyService;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public ProcessDefinitionManager getProcessDefinitionManager() {
        return processDefinitionManager;
    }

    public String startNew(String processDefinitionKey, String userId, String businessKey, Map<String, Object> formData) {

        formData = ensureFormDataNotNull(formData);

        Map<String, Object> envVariables = getEnvVarFromFormData(formData);

        identityService.setAuthenticatedUserId(userId);

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey).latestVersion().singleResult();

        envVariables.put(ProcessMainVariableNames.PROC_DEF_KEY, processDefinition.getKey());

        envVariables.put(ProcessMainVariableNames.PROC_TYPE, processDefinition.getName());

        envVariables.put(ProcessMainVariableNames.PROC_CREATE_TIME, new Date());

        envVariables.put(ProcessMainVariableNames.PROC_CREATE_USERID, userId);

        UserInfo userInfo = userManagementService.getUser(userId);
        envVariables.put(ProcessMainVariableNames.PROC_CREATE_USER, userInfo.getUserName());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, envVariables);

        List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

        String firstTaskId = "";

        if (nextTaskList.size() == 1) {
            firstTaskId = nextTaskList.get(0).getId();
            taskService.setAssignee(firstTaskId, userId);
        }

        return processInstance.getId() + "|" + firstTaskId;
    }

    public void save(String taskId, String comment, Map<String, Object> formData) {

        formData = ensureFormDataNotNull(formData);

        taskService.setVariables(taskId, getEnvVarFromFormData(formData));

        List<String> nextUsers = new ArrayList<String>();

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);

        WorkflowEngineContext workflowEngineContext = new ActivitiWorkflowEngineContext(this);
        workflowEngineContext.setTaskId(taskId);
        workflowEngineContext.setComment(comment);
        workflowEngineContext.setNextUsers(nextUsers);
        workflowEngineContext.setFormData(formData);
        workflowEngineContext.setCurrentTaskDef(getCurrentTaskDef(taskId));
        workflowEngineContext.setNextTaskDef(getNextTaskDef(taskId, getEnvVarFromFormData(formData)));

        if (workflowActionListener != null) {
            workflowActionListener.onSave(workflowEngineContext);
        }

    }

    public void submit(String taskId, String userId, List<String> nextUsers, String comment, Map<String, Object> formData) {

        formData = ensureFormDataNotNull(formData);
        TaskDefModel currentTaskDef = getCurrentTaskDef(taskId);
        Map<String, Object> envVariables = getEnvVarFromFormData(formData);
        String choice = getChoiceFromFormData(formData);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (currentTaskDef.getIsCountersign()) {
            calcCountersignVariables(taskId, envVariables, choice);
        }

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);

        WorkflowEngineContext workflowEngineContext = new ActivitiWorkflowEngineContext(this);
        workflowEngineContext.setTaskId(taskId);
        workflowEngineContext.setComment(comment);
        workflowEngineContext.setNextUsers(nextUsers);
        workflowEngineContext.setFormData(formData);
        workflowEngineContext.setCurrentTaskDef(currentTaskDef);
        workflowEngineContext.setNextTaskDef(getNextTaskDef(taskId, envVariables));
        workflowEngineContext.setChoice(choice);

        if (workflowActionListener != null) {
            workflowActionListener.beforeSubmit(workflowEngineContext);
        }

        run(task, userId, nextUsers, comment, envVariables, workflowEngineContext);

        if (workflowActionListener != null) {
            workflowActionListener.afterSubmit(workflowEngineContext);
        }

    }

    public void delete(String taskId, String comment, Map<String, Object> formData) {

        formData = ensureFormDataNotNull(formData);

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);

        WorkflowEngineContext workflowEngineContext = new ActivitiWorkflowEngineContext(this);
        workflowEngineContext.setTaskId(taskId);
        workflowEngineContext.setComment(comment);
        workflowEngineContext.setFormData(formData);
        workflowEngineContext.setCurrentTaskDef(getCurrentTaskDef(taskId));
        workflowEngineContext.setChoice(getChoiceFromFormData(formData));

        if (workflowActionListener != null) {
            workflowActionListener.beforeDelete(workflowEngineContext);
        }

        processInstanceManager.deleteProcessInstanceByTaskId(taskId, comment);

        if (workflowActionListener != null) {
            workflowActionListener.afterDelete(workflowEngineContext);
        }

    }

    private void run(Task task, String userId, List<String> nextUsers, String comment, Map<String, Object> envVariables, WorkflowEngineContext workflowEngineContext) {

        Boolean isCountersign4Next = workflowEngineContext.getNextTaskDef().getIsCountersign();
        Boolean isCountersign4Current = workflowEngineContext.getCurrentTaskDef().getIsCountersign();

        if (comment != null) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
        }

        if (isCountersign4Next) {
            envVariables.put(CountersignVariableNames.COUNTERSIGN_USERS, nextUsers);
        }

        taskService.setAssignee(task.getId(), userId);
        taskService.complete(task.getId(), envVariables);

        if (!(isCountersign4Next || isCountersign4Current)) {
            setTaskUser(task.getId(), nextUsers);
        }

        if (isCountersign4Current) {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
            if (tasks.size() >= 0) {
                TaskDefModel newTaskDefModel = processDefinitionManager.convertActivityImplToTaskDefModel(
                        processDefinitionManager.getActivityImpl(tasks.get(0).getId()));
                if (newTaskDefModel.getIsCountersign() == false) {
                    setTaskUser(task.getId(), nextUsers);
                }
            }
        }
    }

    public NextTaskInfo retrieveNextTaskInfo(String taskId, Map<String, Object> formData) {
        NextTaskInfo result = new NextTaskInfo();
        List<UserInfo> userInfoList = new ArrayList<UserInfo>();
        String choice = getChoiceFromFormData(formData);
        Map<String, Object> envVariables = getEnvVarFromFormData(formData);
        TaskDefModel currentTaskDef = getCurrentTaskDef(taskId);

        if (currentTaskDef.getIsCountersign()) {
            calcCountersignVariables(taskId, envVariables, choice);
        }

        TaskDefModel nextTaskDefModel = getNextTaskDef(taskId, envVariables);
        result.setTaskKey(nextTaskDefModel.getKey());
        result.setTaskName(nextTaskDefModel.getName());

        WorkflowEngineContext workflowEngineContext = new ActivitiWorkflowEngineContext(this);
        workflowEngineContext.setTaskId(taskId);
        workflowEngineContext.setFormData(formData);
        workflowEngineContext.setCurrentTaskDef(currentTaskDef);
        workflowEngineContext.setNextTaskDef(nextTaskDefModel);
        workflowEngineContext.setChoice(choice);
        initNextTaskUsers(userInfoList, taskId, workflowEngineContext);
        String configValue = null;
        if (nextTaskDefModel.getIsCountersign()) {
            configValue = nextTaskDefModel.getCandidateUser();
        } else {
            configValue = nextTaskDefModel.getAssignee();
        }
        String[] arrConfigValue = configValue.split(CommonVariableNames.NAME_VALUE_SEPARATOR);
        String handlerKey = arrConfigValue[0];
        String handlerParams = arrConfigValue.length > 1 ? arrConfigValue[1] : null;

        if (handlerKey.startsWith(CommonVariableNames.HANDLER_NAME_PREFIX)) {
            handlerKey = handlerKey.replace(CommonVariableNames.HANDLER_NAME_PREFIX, "");
            NextTaskUsersHandler nextTaskUsersHandler = nextTaskUsersHandlers.get(handlerKey);

            if (nextTaskUsersHandler == null) {
                throw new HandlerNotFoundException("在“nextTaskUsersHandlers”中找不到key为“" + handlerKey + "”的NextTaskUsersHandler！");
            } else {
                workflowEngineContext.setComment(handlerParams);
                userInfoList.addAll(nextTaskUsersHandler.execute(workflowEngineContext));
            }
        }
        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);
        if (workflowActionListener != null) {
            workflowActionListener.onRetrieveNextTaskUsers(userInfoList, workflowEngineContext);
        }
        result.setIsCountersign(nextTaskDefModel.getIsCountersign());
        result.setUsers(userInfoList);
        return result;
    }

    public Map<String, Object> retrieveFormData(String taskId) {

        Map<String, Object> formData = new HashMap<String, Object>();

        Map<String, Object> processVariables = processVariablesManager.getProcessVariablesByTaskId(taskId);
        setEnvVarToFormData(processVariables, formData);

        TaskDefModel currentTaskDefModel = getCurrentTaskDef(taskId);
        ProcessDefinition processDefinition = processDefinitionManager.getProcessDefinitionEntityByTaskId(taskId);

        formData.put(FormDataVariableNames.IS_FIRST_TASK, processDefinitionManager.isFirstTask(taskId));
        formData.put(FormDataVariableNames.CURRENT_TASK_KEY, currentTaskDefModel.getKey());
        formData.put(FormDataVariableNames.CURRENT_TASK_NAME, currentTaskDefModel.getName());
        formData.put(FormDataVariableNames.IS_RUNNING_TASK, userTaskManager.isRunningTask(taskId));
        formData.put(FormDataVariableNames.PRO_DEF_KEY, processDefinition.getKey());
        formData.put(FormDataVariableNames.PRO_DEF_NAME, processDefinition.getName());

        WorkflowActionListener workflowActionListener = getWorkflowActionListenerByTaskId(taskId);
        if (workflowActionListener != null) {
            WorkflowEngineContext workflowEngineContext = new ActivitiWorkflowEngineContext(this);
            workflowEngineContext.setTaskId(taskId);
            workflowEngineContext.setFormData(formData);
            workflowEngineContext.setCurrentTaskDef(currentTaskDefModel);
            workflowActionListener.onRetrieveFormData(workflowEngineContext);
        }

        return formData;

    }

    public Map<String, Object> getEnvVarFromFormData(Map<String, Object> formData) {
        Map<String, Object> result = new HashMap<String, Object>();

        for (Entry<String, Object> formItem : formData.entrySet()) {
            if (formItem.getKey().startsWith(ACTIVITI_ENV_VAR_KEY_PREFIX)) {
                result.put(formItem.getKey().replace(ACTIVITI_ENV_VAR_KEY_PREFIX, ""), formItem.getValue());
            }
        }

        return result;
    }

    public void setEnvVarToFormData(Map<String, Object> envVar, Map<String, Object> formData) {
        for (Entry<String, Object> entry : envVar.entrySet()) {
            formData.put(ACTIVITI_ENV_VAR_KEY_PREFIX + entry.getKey(), entry.getValue());
        }
    }

    private void initNextTaskUsers(List<UserInfo> userInfos, String taskId, WorkflowEngineContext workflowEngineContext) {
        if (workflowEngineContext.getChoice().equals(WorkflowChoiceOptions.BACK)) {
            String nextTaskUser = userTaskManager.getTaskAssignee(userTaskManager.getProcessInstanceId(taskId),
                    workflowEngineContext.getNextTaskDef().getKey());
            if (nextTaskUser != null) {
                UserInfo userInfo = userManagementService.getUser(nextTaskUser);
                userInfos.add(userInfo);
            }
        }
    }

    private void setTaskUser(String preTaskId, List<String> users) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .taskId(preTaskId).singleResult();
        if (users != null && users.size() != 0) {
            List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(historicTaskInstance
                    .getProcessInstanceId()).list();
            for (Task nextTask : nextTaskList) {
                if (users.size() == 1) {
                    // nextTask.setAssignee(nextUsers.get(0));
                    taskService.setAssignee(nextTask.getId(), users.get(0));
                } else {
                    for (String userId : users) {
                        taskService.addCandidateUser(nextTask.getId(), userId);
                    }
                    taskService.setAssignee(nextTask.getId(), null);
                }
            }
        }
    }

    private String getChoiceFromFormData(Map<String, Object> formData) {
        Object objChoice = formData.get(ACTIVITI_ENV_VAR_KEY_PREFIX + WorkflowChoiceOptions.getStoreKey());
        return objChoice == null ? "" : objChoice.toString();
    }

    private Map<String, Object> ensureFormDataNotNull(Map<String, Object> formData) {
        if (formData == null) {
            formData = new HashMap<String, Object>();
        }
        return formData;
    }

    private WorkflowActionListener getWorkflowActionListenerByTaskId(String taskId) {
        ProcessDefinitionEntity processDefinitionEntity = processDefinitionManager.getProcessDefinitionEntityByTaskId(taskId);
        WorkflowActionListener workflowActionListener = workflowActionListeners.get(processDefinitionEntity.getKey());
        return workflowActionListener;
    }

    private TaskDefModel getCurrentTaskDef(String taskId) {
        TaskDefModel taskDefModel = new TaskDefModel();
        ActivityImpl activityImpl = processDefinitionManager.getActivityImpl(taskId);
        taskDefModel = processDefinitionManager.convertActivityImplToTaskDefModel(activityImpl);
        return taskDefModel;
    }

    private TaskDefModel getNextTaskDef(String taskId, Map<String, Object> params) {
        return processDefinitionManager.getNextTaskDefModel(taskId, params);
    }

    private void calcCountersignVariables(String taskId, Map<String, Object> envVariables, String choice) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        Object objCountersignApprovedCount = runtimeService.getVariable(task.getProcessInstanceId(), CountersignVariableNames.COUNTERSIGN_APPROVED_COUNT);
        Integer countersignApprovedCount = null;
        if (objCountersignApprovedCount == null) {
            countersignApprovedCount = new Integer(0);
        } else {
            countersignApprovedCount = (Integer) objCountersignApprovedCount;
        }

        Object objCountersignRejectedCount = runtimeService.getVariable(task.getProcessInstanceId(), CountersignVariableNames.COUNTERSIGN_REJECTED_COUNT);
        Integer countersignRejectedCount = null;
        if (objCountersignRejectedCount == null) {
            countersignRejectedCount = new Integer(0);
        } else {
            countersignRejectedCount = (Integer) objCountersignRejectedCount;
        }

        if (choice.equals(WorkflowChoiceOptions.APPROVE)) {
            ++countersignApprovedCount;
        } else if (choice.equals(WorkflowChoiceOptions.REJECT)) {
            ++countersignRejectedCount;
        }

        envVariables.put(CountersignVariableNames.COUNTERSIGN_APPROVED_COUNT, countersignApprovedCount);
        envVariables.put(CountersignVariableNames.COUNTERSIGN_REJECTED_COUNT, countersignRejectedCount);

    }
}
