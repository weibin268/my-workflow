package com.zhuang.workflow.impl.activiti.manager;

import static org.hamcrest.CoreMatchers.nullValue;

import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmProcessElement;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhuang.workflow.exception.HistoricTaskNotFoundException;
import com.zhuang.workflow.model.TaskDefModel;
import com.zhuang.workflow.impl.activiti.util.ActivitiJUELUtils;
import com.zhuang.workflow.enums.EndTaskVariableNames;

public class ProcessDefinitionManager {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    public ProcessDefinitionEntity getProcessDefinitionEntityByTaskId(String taskId) {

        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId)
                .singleResult();

        ProcessDefinitionEntity def = (ProcessDefinitionEntity) (((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(historicTaskInstance.getProcessDefinitionId()));

        return def;
    }

    public ProcessDefinitionEntity getProcessDefinitionEntityByKey(String proDefkey) {

        ProcessDefinitionEntity def = (ProcessDefinitionEntity) repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(proDefkey).latestVersion().singleResult();
        return def;
    }

    public ActivityImpl getActivityImpl(String taskId) {

        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId)
                .singleResult();

        if (historicTaskInstance == null) {
            throw new HistoricTaskNotFoundException("taskId:" + taskId);
        }

        ProcessDefinitionEntity def = (ProcessDefinitionEntity) (((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(historicTaskInstance.getProcessDefinitionId()));

        String activitiId = historicTaskInstance.getTaskDefinitionKey();

        List<ActivityImpl> activitiList = def.getActivities();

        for (ActivityImpl activityImpl : activitiList) {

            if (activitiId.equals(activityImpl.getId())) {// 当前任务
                return activityImpl;
            }
        }

        return null;
    }

    private TaskDefinition getTaskDefinition(String taskId) {

        ActivityImpl activityImpl = getActivityImpl(taskId);

        if (activityImpl != null) {
            return ((UserTaskActivityBehavior) activityImpl.getActivityBehavior()).getTaskDefinition();
        }

        return null;
    }

    private TaskDefinition getNextTaskDefinition(String taskId, Map<String, Object> params) {

        ActivityImpl activityImpl = getNextActivityImpl(taskId, params);

        if (activityImpl != null) {
            return ((UserTaskActivityBehavior) activityImpl.getActivityBehavior()).getTaskDefinition();
        }

        return null;
    }

    public ActivityImpl getNextActivityImpl(String taskId, Map<String, Object> params) {

        ActivityImpl activityImpl = getActivityImpl(taskId);

        if (activityImpl != null) {
            return getNextActivityImpl(activityImpl, params);
        }

        return null;
    }

    private ActivityImpl getNextActivityImpl(ActivityImpl activityImpl, Map<String, Object> params) {

        ActivityImpl result = null;

        List<PvmTransition> outgoingTransitions = activityImpl.getOutgoingTransitions();

        if (outgoingTransitions.size() == 1) {

            PvmActivity outActi = outgoingTransitions.get(0).getDestination();

            if (isGatewayElement(outActi)) {

                List<PvmTransition> gatewayOutgoingTransitions = outActi.getOutgoingTransitions();

                result = getNextActivityImpl(gatewayOutgoingTransitions, params);

            } else if ("userTask".equals(outActi.getProperty("type"))) {

                result = (ActivityImpl) outActi;

            } else if ("endEvent".equals(outActi.getProperty("type"))) {
                result = (ActivityImpl) outActi;
                /*
                 * result=new TaskDefinition(null);
                 *
                 * result.setKey(EndTaskVariableNames.KEY);
                 *
                 * //ValueExpression
                 * valueExpression=ExpressionFactory.newInstance().
                 * createValueExpression("结束", String.class);
                 *
                 * result.setNameExpression(new JuelExpression(null,
                 * EndTaskVariableNames.NAME));
                 */
            } else {
                System.out.println(outActi.getProperty("type"));
            }

        } else if (outgoingTransitions.size() > 1) {

            result = getNextActivityImpl(outgoingTransitions, params);

        }
        // System.out.println(result.getNameExpression().getExpressionText());
        return result;

    }

    private ActivityImpl getNextActivityImpl(List<PvmTransition> outgoingTransitions, Map<String, Object> params) {

        ActivityImpl result = null;

        if (outgoingTransitions.size() == 1) {

            ActivityImpl tempActivityImpl = (ActivityImpl) outgoingTransitions.get(0).getDestination();

            if (isGatewayElement(tempActivityImpl)) {
                return getNextActivityImpl(tempActivityImpl, params);
            } else {
                return tempActivityImpl;
            }

        } else if (outgoingTransitions.size() > 1) {
            PvmTransition correctGwOutTransi = null;
            PvmTransition defaultGwOutTransi = null;
            for (PvmTransition gwOutTransi : outgoingTransitions) {
                Object conditionText = gwOutTransi.getProperty("conditionText");
                if (conditionText == null) {
                    defaultGwOutTransi = gwOutTransi;
                }
                if (conditionText != null && ActivitiJUELUtils.evaluateBooleanResult(conditionText.toString(), params)) {
                    correctGwOutTransi = gwOutTransi;
                }
            }
            if (correctGwOutTransi == null) {
                correctGwOutTransi = defaultGwOutTransi;
            }
            if (correctGwOutTransi != null) {
                ActivityImpl tempActivityImpl = (ActivityImpl) correctGwOutTransi.getDestination();
                if (isGatewayElement(tempActivityImpl)) {
                    result = getNextActivityImpl(tempActivityImpl, params);
                } else {
                    result = tempActivityImpl;
                }
            }
        }
        return result;
    }

    public boolean isFirstTask(String taskId) {

        boolean result = false;

        ActivityImpl activityImpl = getActivityImpl(taskId);

        List<PvmTransition> incomingTransitions = activityImpl.getIncomingTransitions();

        for (PvmTransition pvmTransition : incomingTransitions) {

            PvmActivity pvmActivity = pvmTransition.getSource();

            if (pvmActivity.getProperty("type").equals("startEvent")) {
                result = true;
                break;
            }
        }

        return result;
    }

    public boolean isEndTask(String taskId) {

        boolean result = false;

        ActivityImpl activityImpl = getActivityImpl(taskId);

        List<PvmTransition> incomingTransitions = activityImpl.getOutgoingTransitions();

        for (PvmTransition pvmTransition : incomingTransitions) {

            PvmActivity pvmActivity = pvmTransition.getDestination();

            if (pvmActivity.getProperty("type").equals("endEvent")) {
                result = true;
                break;
            }
        }

        return result;
    }

    public List<ProcessDefinition> getProcessDefinitionList() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().active()
                .latestVersion().list();
        return processDefinitions;
    }

    public TaskDefModel convertActivityImplToTaskDefModel(ActivityImpl activityImpl) {
        TaskDefModel result = new TaskDefModel();

        TaskDefinition taskDefinition = (TaskDefinition) activityImpl.getProperty("taskDefinition");

        if ("endEvent".equals(activityImpl.getProperty("type"))) {
            result.setKey(EndTaskVariableNames.KEY);
            result.setName(EndTaskVariableNames.NAME);

            result.setAssignee("");
            result.setCandidateUser("");


        } else {
            result.setKey(taskDefinition.getKey());
            result.setName(taskDefinition.getNameExpression().toString() == null ? ""
                    : taskDefinition.getNameExpression().toString());

            result.setAssignee(taskDefinition.getAssigneeExpression() == null ? ""
                    : taskDefinition.getAssigneeExpression().toString());

            if (taskDefinition.getCandidateUserIdExpressions() != null) {
                for (Expression expression : taskDefinition.getCandidateUserIdExpressions()) {
                    result.setCandidateUser(expression.getExpressionText());
                }
            }
        }

        if (activityImpl.getActivityBehavior().getClass() == ParallelMultiInstanceBehavior.class) {
            result.setIsCountersign(true);
        } else {
            result.setIsCountersign(false);
        }

        return result;
    }

    public TaskDefModel getNextTaskDefModel(String taskId, Map<String, Object> params) {
        TaskDefModel result = null;

        ActivityImpl activityImpl = getNextActivityImpl(taskId, params);
        result = convertActivityImplToTaskDefModel(activityImpl);

        return result;
    }

    public boolean isGatewayElement(PvmProcessElement pvmProcessElement) {
        if (pvmProcessElement.getProperty("type").toString().toLowerCase().contains("gateway")) {
            return true;
        } else {
            return false;
        }
    }

}
