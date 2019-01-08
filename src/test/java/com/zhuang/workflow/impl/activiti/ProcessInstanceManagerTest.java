package com.zhuang.workflow.impl.activiti;

import com.zhuang.workflow.WorkflowBeansFactory;
import org.junit.Test;

public class ProcessInstanceManagerTest {

    @Test
    public void getApplyUserId() {
    }

    @Test
    public void deleteProcessInstanceByTaskId() {
        WorkflowBeansFactory.getProcessInstanceManager().deleteProcessInstanceByTaskId("282529","");
        System.out.println("success!");
    }

    @Test
    public void isProcessFinished() {
    }
}