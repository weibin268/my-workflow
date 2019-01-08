package com.zhuang.workflow.impl;

import com.zhuang.workflow.WorkflowBeansFactory;
import com.zhuang.workflow.model.PageInfo;
import com.zhuang.workflow.model.FlowInfo;
import com.zhuang.workflow.model.ProcDefModel;
import com.zhuang.workflow.model.TaskInfo;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivitiWorkflowQueryManagerTest {

    @Test
    public void getMyTodoListPage() {
        Map<String, Object> map =new HashMap<String, Object>();
        //map.put(ProcessMainVariableNames.PROC_TITLE, "%20170112");
        PageInfo<FlowInfo> pageInfo = WorkflowBeansFactory.getWorkflowQueryManager()
                .getMyTodoListPage("user1",1, 10, map );

        System.out.println(pageInfo.getList().size());
        System.out.println(pageInfo.getTotalRows());
        System.out.println(pageInfo.getPageStartRow());
        System.out.println(pageInfo.getHasPreviousPage());
        System.out.println(pageInfo.getHasNextPage());

        for (FlowInfo flowInfo : pageInfo.getList()) {
            System.out.println(flowInfo);
        }
    }

    @Test
    public void getMyDoneListPage() {
        System.out.println("testGetMyDoneListPage>>>>>>");

        Map<String, Object> map =new HashMap<String, Object>();
        //map.put(ProcessMainVariableNames.PROC_TITLE, "%20170112");
        PageInfo<FlowInfo> pageInfo = WorkflowBeansFactory.getWorkflowQueryManager()
                .getMyDoneListPage("zwb",1, 10, map );

        System.out.println(pageInfo.getList().size());
        System.out.println(pageInfo.getTotalRows());
        System.out.println(pageInfo.getPageStartRow());
        System.out.println(pageInfo.getHasPreviousPage());
        System.out.println(pageInfo.getHasNextPage());

        for (FlowInfo flowInfo : pageInfo.getList()) {
            System.out.println(flowInfo);
        }
        System.out.println("<<<<<<<testGetMyDoneListPage");
    }

    @Test
    public void getHistoryTaskInfoList() {
        List<TaskInfo> taskInfos = WorkflowBeansFactory.getWorkflowQueryManager()
                .getHistoryTaskInfoList("425037");
        for (TaskInfo taskInfo : taskInfos) {
            System.out.println(taskInfo.toString());
        }
        System.out.println("success!");
    }

    @Test
    public void getProcDefList() {
        List<ProcDefModel> procDefModels = WorkflowBeansFactory.getWorkflowQueryManager().getProcDefList();
        for (ProcDefModel procDefModel : procDefModels) {
            System.out.println(procDefModel);
        }
    }
}