package com.zhuang.workflow.impl;

import com.zhuang.workflow.WorkflowBeansFactory;
import com.zhuang.workflow.enums.ProcessMainVariableNames;
import com.zhuang.workflow.model.NextTaskInfo;
import com.zhuang.workflow.model.UserInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiWorkflowEngineTest {

    @Test
    public void getProcessEngine() {
    }

    @Test
    public void getRepositoryService() {
    }

    @Test
    public void getRuntimeService() {
    }

    @Test
    public void getFormService() {
    }

    @Test
    public void getIdentityService() {
    }

    @Test
    public void getTaskService() {
    }

    @Test
    public void getHistoryService() {
    }

    @Test
    public void getManagementService() {
    }

    @Test
    public void getProcessDefinitionManager() {
    }

    @Test
    public void startNew() {

        Map<String, Object> formData=new HashMap<String, Object>();
	/*	formData.put("env:env1111", 1111);
		formData.put("env:env2222", 1111);*/
        formData.put("env:"+ ProcessMainVariableNames.PROC_TITLE,"bbbbbbbbbbbb");
        System.out.println(WorkflowBeansFactory.getWorkflowEngine().startNew("test01", "user1","f001",formData));

        System.out.println("success!");
    }

    @Test
    public void save() {
    }

    @Test
    public void submit() {
        List<String> users = new ArrayList<String>();
        users.add("zs");
        users.add("ls");
	/*	users.add("z002");
		users.add("z003");*/
        Map<String, Object> map=new HashMap<String, Object>();
        map.put("env_amount", 1189);
        map.put("env_choice", "提交");
        WorkflowBeansFactory.getWorkflowEngine().submit("420011","zwb",users, "同意", map);

        System.out.println("success!");
    }

    @Test
    public void delete() {
        List<String> users = new ArrayList<String>();
        users.add("user3");
	/*	users.add("z002");
		users.add("z003");*/
        Map<String, Object> map=new HashMap<String, Object>();
        map.put("env:amount", 89);
        WorkflowBeansFactory.getWorkflowEngine().delete("315007", "删除", map);

        System.out.println("success!");
    }

    @Test
    public void retrieveNextTaskInfo() {
        Map<String, Object> formData = new HashMap<String, Object>();
        formData.put("env_amount", 11);
        //formData.put("env_choice", "提交");

        // 370003 375012
        NextTaskInfo nextTaskInfo = WorkflowBeansFactory.getWorkflowEngine().retrieveNextTaskInfo("10011", formData);

        System.out.println(nextTaskInfo.getTaskKey());
        System.out.println(nextTaskInfo.getTaskName());

        for (UserInfo userInfo : nextTaskInfo.getUsers()) {
            System.out.println(userInfo.toString());
        }

        System.out.println("success!");
    }

    @Test
    public void retrieveFormData() {
        Map<String, Object> formData = WorkflowBeansFactory.getWorkflowEngine().retrieveFormData("237509");
        for (Map.Entry entry : formData.entrySet()) {
            System.out.println(entry.getValue());
        }

        System.out.println("success!");
    }

    @Test
    public void getEnvVarFromFormData() {
    }

    @Test
    public void setEnvVarToFormData() {
    }
}