package com.zhuang.workflow.impl.activiti.manager;

import java.io.InputStream;

import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

public class DeploymentManager {
	
	private String basePath="";

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@Autowired
	private RepositoryService repositoryService;
	
	public void deployByClasspathResource(String resourceName,String deployName) {
		String resourcePath=basePath+resourceName;
		repositoryService.createDeployment().name(deployName)
		.addClasspathResource(resourcePath).deploy();
	}
	
	public void deployByInputStream(String resourceName, InputStream inputStream) {
		
		repositoryService.createDeployment().name(resourceName).addInputStream(resourceName, inputStream).deploy();
		
	}
}
