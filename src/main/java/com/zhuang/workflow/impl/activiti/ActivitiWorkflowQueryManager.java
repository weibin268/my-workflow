package com.zhuang.workflow.impl.activiti;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.zhuang.workflow.model.FlowInfo;
import com.zhuang.workflow.model.TaskInfo;
import com.zhuang.workflow.util.DateUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfoQuery;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhuang.workflow.WorkflowQueryManager;
import com.zhuang.workflow.impl.activiti.manager.ProcessDefinitionManager;
import com.zhuang.workflow.impl.activiti.manager.ProcessInstanceManager;
import com.zhuang.workflow.enums.ProcessMainVariableNames;
import com.zhuang.workflow.impl.activiti.manager.ProcessVariablesManager;
import com.zhuang.workflow.model.PageInfo;
import com.zhuang.workflow.enums.EndTaskVariableNames;
import com.zhuang.workflow.model.ProcDefModel;
import com.zhuang.workflow.service.UserManagementService;

public class ActivitiWorkflowQueryManager implements WorkflowQueryManager {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private ProcessVariablesManager processVariablesManager;

    @Autowired
    private ProcessInstanceManager processInstanceManager;

    @Autowired
    ProcessDefinitionManager processDefinitionManager;

    public PageInfo<FlowInfo> getMyTodoListPage(String userId, int pageNo, int pageSize, Map<String, Object> conditions) {

        //总记录查询
        TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned(userId);

        List<FlowInfo> flowInfoList = new ArrayList<FlowInfo>();

        //设置查询筛选条件
        setTaskQueryConditions(taskQuery, conditions);

        //设置排序
        taskQuery.orderByTaskCreateTime().desc();

        PageInfo<FlowInfo> result = new PageInfo<FlowInfo>(pageNo, pageSize, new Long(taskQuery.count()).intValue(),
                flowInfoList);

        //得到分页记录
        List<Task> taskList = taskQuery.listPage(result.getPageStartRow() - 1, result.getPageSize());

        //设置流程信息实体值
        for (Task task : taskList) {

            FlowInfo flowInfo = new FlowInfo();
            flowInfo.setTaskId(task.getId());
            flowInfo.setCurrentActivityName(task.getName());

            Map<String, Object> processVariables = runtimeService.getVariables(task.getExecutionId());
            fillFlowInfoModel(flowInfo, processVariables);
            flowInfoList.add(flowInfo);
        }

        return result;
    }

    public PageInfo<FlowInfo> getMyDoneListPage(String userId, int pageNo, int pageSize, Map<String, Object> conditions) {

        //总记录查询
        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId).finished();

        List<FlowInfo> flowInfoList = new ArrayList<FlowInfo>();

        //设置查询筛选条件
        setTaskQueryConditions(historicTaskInstanceQuery, conditions);

        //设置排序
        historicTaskInstanceQuery.orderByTaskCreateTime().desc();

        PageInfo<FlowInfo> result = new PageInfo<FlowInfo>(pageNo, pageSize, new Long(historicTaskInstanceQuery.count()).intValue(),
                flowInfoList);

        //得到分页记录
        List<HistoricTaskInstance> historicTaskInstances = historicTaskInstanceQuery.listPage(result.getPageStartRow() - 1, result.getPageSize());

        //设置流程信息实体值
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {

            FlowInfo flowInfo = new FlowInfo();
            flowInfo.setTaskId(historicTaskInstance.getId());


            String currentActivityName = "";
            if (processInstanceManager.isProcessFinished(historicTaskInstance.getProcessInstanceId())) {
                currentActivityName = EndTaskVariableNames.NAME;
            } else {

                List<Task> tasks = taskService.createTaskQuery().processInstanceId(historicTaskInstance.getProcessInstanceId()).list();
                if (tasks.size() > 0) {
                    currentActivityName = tasks.get(0).getName();
                }
            }

            flowInfo.setCurrentActivityName(currentActivityName);

            Map<String, Object> processVariables = processVariablesManager.getProcessVariablesByTaskId(historicTaskInstance.getId());
            fillFlowInfoModel(flowInfo, processVariables);
            flowInfoList.add(flowInfo);
        }

        return result;

    }

    public List<TaskInfo> getHistoryTaskInfoList(String taskId) {

        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();

        String instanceId = historicTaskInstance.getProcessInstanceId();

        List<TaskInfo> taskInfos = getHistoryTaskInfoListByInstanceId(instanceId);

        return taskInfos;
    }

    private List<TaskInfo> getHistoryTaskInfoListByInstanceId(String instanceId) {
        List<TaskInfo> taskInfos = new ArrayList<TaskInfo>();

        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId).orderByTaskCreateTime().asc().list();

        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {

            TaskInfo taskInfo = new TaskInfo();

            taskInfo.setId(historicTaskInstance.getId());
            taskInfo.setKey(historicTaskInstance.getTaskDefinitionKey());
            taskInfo.setName(historicTaskInstance.getName());

            taskInfo.setUserId(historicTaskInstance.getAssignee());
            taskInfo.setUserName(userManagementService.getUser(taskInfo.getUserId()).getUserName());


            if (taskInfo.getUserId() == null) {
                List<String> userIds = new ArrayList<String>();
                List<String> userNames = new ArrayList<String>();

                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskInfo.getId());

                for (IdentityLink identityLink : identityLinks) {
                    if (!identityLink.getType().equals("candidate")) {
                        continue;
                    }

                    userIds.add(identityLink.getUserId());
                    userNames.add(userManagementService.getUser(identityLink.getUserId()).getUserName());
                }

                taskInfo.setUserId(StringUtils.join(userIds.toArray(new String[userIds.size()]), ","));
                taskInfo.setUserName(StringUtils.join(userNames.toArray(new String[userNames.size()]), ","));
            }

            taskInfo.setStartTime(historicTaskInstance.getStartTime());
            taskInfo.setEndTime(historicTaskInstance.getEndTime());
            List<Comment> comments = taskService.getTaskComments(historicTaskInstance.getId());
            if (comments.size() > 0) {
                taskInfo.setComment(comments.get(0).getFullMessage());
            }
            taskInfos.add(taskInfo);
        }

        if (taskInfos.size() > 0) {
            TaskInfo lastTask = taskInfos.get(taskInfos.size() - 1);
            if (lastTask.getEndTime() != null) {
                boolean isEndTask = processDefinitionManager.isEndTask(lastTask.getId());
                if (isEndTask) {
                    TaskInfo taskInfo = new TaskInfo();

                    taskInfo.setId(EndTaskVariableNames.ID);
                    taskInfo.setKey(EndTaskVariableNames.KEY);
                    taskInfo.setName(EndTaskVariableNames.NAME);
                    taskInfo.setUserId(EndTaskVariableNames.USERID);
                    taskInfo.setUserName(EndTaskVariableNames.USERNAME);
                    taskInfo.setStartTime(lastTask.getEndTime());
                    taskInfo.setEndTime(lastTask.getEndTime());
                    taskInfo.setComment(EndTaskVariableNames.COMMENT);

                    taskInfos.add(taskInfo);

                }
            }
        }

        return taskInfos;
    }

    private void setTaskQueryConditions(TaskInfoQuery taskInfoQuery, Map<String, Object> conditions) {

        if (conditions != null) {

            if (conditions.containsKey(ProcessMainVariableNames.PROC_DEF_KEY)) {
                Object objProcDefKey = conditions.get(ProcessMainVariableNames.PROC_DEF_KEY);
                if (objProcDefKey != null && !objProcDefKey.toString().trim().isEmpty()) {
                    taskInfoQuery.processVariableValueEquals(ProcessMainVariableNames.PROC_DEF_KEY,
                            objProcDefKey);
                }
            }

            if (conditions.containsKey(ProcessMainVariableNames.PROC_TYPE)) {
                Object objProcType = conditions.get(ProcessMainVariableNames.PROC_TYPE);
                if (objProcType != null && !objProcType.toString().trim().isEmpty()) {
                    taskInfoQuery.processVariableValueEquals(ProcessMainVariableNames.PROC_TYPE,
                            objProcType);
                }
            }

            if (conditions.containsKey(ProcessMainVariableNames.PROC_TITLE)) {
                Object objProcTitle = conditions.get(ProcessMainVariableNames.PROC_TITLE);
                if (objProcTitle != null && !objProcTitle.toString().trim().isEmpty()) {
                    taskInfoQuery.processVariableValueLike(ProcessMainVariableNames.PROC_TITLE,
                            "%" + objProcTitle.toString() + "%");
                }
            }

            if (conditions.containsKey(ProcessMainVariableNames.PROC_CREATE_USER)) {
                Object objProcCreateUserName = conditions.get(ProcessMainVariableNames.PROC_CREATE_USER);
                if (objProcCreateUserName != null && !objProcCreateUserName.toString().trim().isEmpty()) {
                    taskInfoQuery.processVariableValueLike(ProcessMainVariableNames.PROC_CREATE_USER,
                            "%" + objProcCreateUserName.toString() + "%");
                }
            }

            String proCreateTimeStart = ProcessMainVariableNames.PROC_CREATE_TIME + "_START";
            if (conditions.containsKey(proCreateTimeStart)) {
                Object objProcCreateTimeStart = conditions.get(proCreateTimeStart);
                if (objProcCreateTimeStart != null && !objProcCreateTimeStart.toString().trim().isEmpty()) {
                    Date dProcCreateTimeStart = null;
                    dProcCreateTimeStart = DateUtils.parseDate(objProcCreateTimeStart.toString() + " 00:00:00");
                    taskInfoQuery.processVariableValueGreaterThanOrEqual(ProcessMainVariableNames.PROC_CREATE_TIME, dProcCreateTimeStart);
                }
            }

            String proCreateTimeEnd = ProcessMainVariableNames.PROC_CREATE_TIME + "_END";
            if (conditions.containsKey(proCreateTimeEnd)) {
                Object objProcCreateTimeEnd = conditions.get(proCreateTimeEnd);
                if (objProcCreateTimeEnd != null && !objProcCreateTimeEnd.toString().trim().isEmpty()) {
                    Date dProcCreateTimeEnd = null;
                    dProcCreateTimeEnd = DateUtils.parseDate(objProcCreateTimeEnd.toString() + " 23:59:59");
                    taskInfoQuery.processVariableValueLessThanOrEqual(ProcessMainVariableNames.PROC_CREATE_TIME, dProcCreateTimeEnd);
                }
            }

        }

    }

    private void fillFlowInfoModel(FlowInfo flowInfo, Map<String, Object> processVariables) {

        if (processVariables.containsKey(ProcessMainVariableNames.PROC_DEF_KEY)) {
            flowInfo.setDefKey(processVariables.get(ProcessMainVariableNames.PROC_DEF_KEY).toString());
        }
        if (processVariables.containsKey(ProcessMainVariableNames.PROC_TITLE)) {
            flowInfo.setTitle(processVariables.get(ProcessMainVariableNames.PROC_TITLE).toString());
        }
        if (processVariables.containsKey(ProcessMainVariableNames.PROC_TYPE)) {
            Object objProcType = processVariables.get(ProcessMainVariableNames.PROC_TYPE);
            flowInfo.setType(objProcType == null ? "" : objProcType.toString());
        }
        if (processVariables.containsKey(ProcessMainVariableNames.PROC_CREATE_TIME)) {
            flowInfo.setCreateTime((Date) processVariables.get(ProcessMainVariableNames.PROC_CREATE_TIME));
        }
        if (processVariables.containsKey(ProcessMainVariableNames.PROC_CREATE_USERID)) {
            flowInfo.setCreateUserId(processVariables.get(ProcessMainVariableNames.PROC_CREATE_USERID).toString());
        }
        if (processVariables.containsKey(ProcessMainVariableNames.PROC_CREATE_USER) && processVariables.get(ProcessMainVariableNames.PROC_CREATE_USER) != null) {
            flowInfo.setCreateUser(processVariables.get(ProcessMainVariableNames.PROC_CREATE_USER).toString());
        }

    }

    public List<ProcDefModel> getProcDefList() {

        List<ProcDefModel> procDefModels = new ArrayList<ProcDefModel>();

        List<ProcessDefinition> processDefinitions = processDefinitionManager.getProcessDefinitionList();

        for (ProcessDefinition processDefinition : processDefinitions) {
            ProcDefModel procDefModel = new ProcDefModel();

            procDefModel.setKey(processDefinition.getKey());
            procDefModel.setName(processDefinition.getName());
            procDefModel.setDescription(processDefinition.getDescription());

            procDefModels.add(procDefModel);
        }

        return procDefModels;
    }

}
