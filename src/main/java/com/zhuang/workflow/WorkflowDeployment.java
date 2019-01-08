package com.zhuang.workflow;

import java.io.InputStream;
import java.util.Map;

import com.zhuang.workflow.model.PageInfo;
import com.zhuang.workflow.model.DeploymentInfo;

public interface WorkflowDeployment {
	
	void deployByInputStream(String resourceName, InputStream inputStream);

	PageInfo<DeploymentInfo> getDeploymentInfoPage(int pageNo, int pageSize, Map<String, Object> conditions);
}
