package com.zhuang.workflow.impl.activiti;

import com.zhuang.workflow.WorkflowBeansFactory;
import com.zhuang.workflow.model.TaskDefModel;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessDefinitionManagerTest {

    @Test
    public void getProcessDefinitionEntityByTaskId() {

        WorkflowBeansFactory.getProcessDefinitionManager().getProcessDefinitionEntityByTaskId("237509");

    }

    @Test
    public void getProcessDefinitionEntityByKey() {
        ProcessDefinitionEntity processDefinitionEntity = WorkflowBeansFactory.getProcessDefinitionManager().getProcessDefinitionEntityByKey("test01");
        System.out.println(processDefinitionEntity.getKey());
        System.out.println(processDefinitionEntity.getName());
    }

    @Test
    public void getActivityImpl() {
    }

    @Test
    public void getNextActivityImpl() {
        String taskId="510010";
        Map<String, Object> params =new HashMap<String, Object>();

        ActivityImpl activityImpl = WorkflowBeansFactory.getProcessDefinitionManager().getNextActivityImpl(taskId, params);

        System.out.println(activityImpl.getActivityBehavior().getClass()==ParallelMultiInstanceBehavior.class);
    }

    @Test
    public void isFirstTask() {

        System.out.println(WorkflowBeansFactory.getProcessDefinitionManager().isFirstTask("212506"));

    }

    @Test
    public void isEndTask() {

        System.out.println(WorkflowBeansFactory.getProcessDefinitionManager().isEndTask("370003"));

    }

    @Test
    public void getProcessDefinitionList() {
        List<ProcessDefinition> processDefinitions=WorkflowBeansFactory.getProcessDefinitionManager().getProcessDefinitionList();
        for (ProcessDefinition processDefinition : processDefinitions) {
            System.out.println(processDefinition.getKey()+"|"+processDefinition.getName());
        }

        System.out.println("success!");
    }

    @Test
    public void convertActivityImplToTaskDefModel() {
        String taskId="510010";
        Map<String, Object> params =new HashMap<String, Object>();

        ActivityImpl activityImpl = WorkflowBeansFactory.getProcessDefinitionManager().getNextActivityImpl(taskId, params);

        TaskDefModel taskDefModel=WorkflowBeansFactory.getProcessDefinitionManager().convertActivityImplToTaskDefModel(activityImpl);

        System.out.println(taskDefModel);
    }

    @Test
    public void getNextTaskDefModel() {
    }
}