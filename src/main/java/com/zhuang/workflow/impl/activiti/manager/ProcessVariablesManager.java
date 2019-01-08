package com.zhuang.workflow.impl.activiti.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessVariablesManager {
	
	@Autowired
	private HistoryService historyService;
	
	public Map<String, Object> getProcessVariablesByTaskId(String taskId)
	{
		Map<String, Object> result=new HashMap<String, Object>();
		
		HistoricTaskInstance historicTaskInstance= historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		
	    List<HistoricVariableInstance> historicVariableInstances =historyService.createHistoricVariableInstanceQuery().processInstanceId(historicTaskInstance.getProcessInstanceId()).list();
		
	    for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
			result.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
		}
	    
		return result;
	}
}
