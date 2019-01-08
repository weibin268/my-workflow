package com.zhuang.workflow.impl.activiti.manager;

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;

public class UserTaskManager {

	@Autowired
	TaskService taskService;
	
	@Autowired
	HistoryService historyService;

	public boolean canExecuteTask(String taskId, String userId) {
		TaskQuery taskQuery = taskService.createTaskQuery().taskId(taskId).taskCandidateOrAssigned(userId);

		long count = taskQuery.count();

		return count > 0 ? true : false;
	}

	public boolean isRunningTask(String taskId) {

		TaskQuery taskQuery = taskService.createTaskQuery().taskId(taskId);

		long count = taskQuery.count();

		return count > 0 ? true : false;
	}

	public String getTaskAssignee(String processInstanceId, String taskDefinitionKey) {

		List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId(processInstanceId).taskDefinitionKey(taskDefinitionKey).list();
		if (!historicTaskInstances.isEmpty()) {
			return historicTaskInstances.get(0).getAssignee();

		} else {
			return null;
		}

	}

	public String getProcessInstanceId(String taskId) {
		
		HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		return historicTaskInstance.getProcessInstanceId();
	}
}
