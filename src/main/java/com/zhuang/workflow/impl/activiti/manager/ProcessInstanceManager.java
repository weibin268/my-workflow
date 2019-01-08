package com.zhuang.workflow.impl.activiti.manager;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessInstanceManager {

	@Autowired
	HistoryService historyService;


	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	public String getApplyUserId(String taskId) {
		
		HistoricTaskInstance historicTaskInstance =historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		
		HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService
				.createHistoricProcessInstanceQuery();
		HistoricProcessInstance historicProcessInstance = historicProcessInstanceQuery
				.processInstanceId(historicTaskInstance.getProcessInstanceId()).singleResult();
		
		return historicProcessInstance.getStartUserId();
		
	}

	public void deleteProcessInstanceByTaskId(String taskId,String deleteReason) {
		
		String processInstanceId=taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
		runtimeService.deleteProcessInstance(processInstanceId,deleteReason);
		
	}

	public boolean isProcessFinished(String processInstanceId) {
		
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		return processInstance==null?true:false;
	
	}
}
