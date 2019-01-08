package com.zhuang.workflow.test;

import java.util.List;

import com.zhuang.workflow.WorkflowActionListener;
import com.zhuang.workflow.WorkflowEngineContext;
import com.zhuang.workflow.model.UserInfo;
import com.zhuang.workflow.enums.FormDataVariableNames;
import com.zhuang.workflow.enums.WorkflowChoiceOptions;

public class Test01ActionListener implements WorkflowActionListener {

	public void beforeSubmit(WorkflowEngineContext context) {

		System.out.println("beforeSubmit--------------------"+context.getChoice());
	}

	public void afterSubmit(WorkflowEngineContext context) {

		System.out.println("afterSubmit--------------------"+context.getChoice());
	}

	public void onSave(WorkflowEngineContext context) {
		System.out.println("onSave--------------------");
		
	}

	public void onRetrieveNextTaskUsers(List<UserInfo> nextTaskUsers, WorkflowEngineContext context) {
		System.out.println("onRetrieveNextTaskUsers--------------------");
		
	}

	public void onRetrieveFormData(WorkflowEngineContext context) {
		System.out.println("onRetrieveFormData--------------------");
		System.out.println("context.getCurrentTaskDef()"+context.getCurrentTaskDef());
		if(context.getCurrentTaskDef().getKey().equals("mgr1") || context.getCurrentTaskDef().getKey().equals("mgr2"))
		{
			if((Boolean)context.getFormData().get(FormDataVariableNames.IS_RUNNING_TASK))
			{
				context.getFormData().put(WorkflowChoiceOptions.BACK, true);
			}
		}
		
		if(context.getCurrentTaskDef().getIsCountersign())
		{
			if((Boolean)context.getFormData().get(FormDataVariableNames.IS_RUNNING_TASK))
			{
				context.getFormData().put(WorkflowChoiceOptions.APPROVE, true);
				context.getFormData().put(WorkflowChoiceOptions.REJECT, true);
				context.getFormData().put(WorkflowChoiceOptions.SUBMIT, false);
			}	
		}
	}

	public void beforeDelete(WorkflowEngineContext context) {
		System.out.println("beforeDelete--------------------");
	}

	public void afterDelete(WorkflowEngineContext context) {
		System.out.println("afterDelete--------------------");		
	}
}
