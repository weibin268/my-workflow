package com.zhuang.workflow;

import java.util.List;

import com.zhuang.workflow.model.UserInfo;

public interface NextTaskUsersHandler {
	
	public List<UserInfo> execute(WorkflowEngineContext workflowEngineContext);
	
}
