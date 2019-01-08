package com.zhuang.workflow.service;

import java.util.List;

import com.zhuang.workflow.model.UserInfo;

public interface UserManagementService {
	
	UserInfo getUser(String userId);
	
	List<UserInfo> getUsersByRoleId(String roleId);
	
	List<UserInfo> getUsersByRoleName(String roleName);
	
}
