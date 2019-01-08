package com.zhuang.workflow.impl.activiti.handler;

import java.util.ArrayList;
import java.util.List;

import com.zhuang.workflow.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhuang.workflow.NextTaskUsersHandler;
import com.zhuang.workflow.WorkflowEngineContext;
import com.zhuang.workflow.service.UserManagementService;

public class RoleIdsHandler implements NextTaskUsersHandler {

	@Autowired
	private UserManagementService userManagementService;
	
	public List<UserInfo> execute(WorkflowEngineContext workflowEngineContext) {

		List<UserInfo> result = new ArrayList<UserInfo>();
		
		String roleId = workflowEngineContext.getComment();

		if (roleId != null) {
			result = userManagementService.getUsersByRoleId(roleId);
		}
		
		return result;
	}

}
