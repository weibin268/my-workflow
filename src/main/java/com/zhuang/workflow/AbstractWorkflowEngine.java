package com.zhuang.workflow;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工作流引擎抽象基类
 * 
 * @author zwb
 *
 */
public abstract class AbstractWorkflowEngine implements WorkflowEngine {

	/**
	 * 工作流动作监听器集合
	 */
	protected WorkflowActionListenerMap workflowActionListeners;

	protected NextTaskUsersHandlerMap nextTaskUsersHandlers;

	public WorkflowActionListenerMap getWorkflowActionListeners() {

		return workflowActionListeners;
	}

	public void setWorkflowActionListeners(WorkflowActionListenerMap workflowActionListeners) {
		this.workflowActionListeners = workflowActionListeners;
	}

	public NextTaskUsersHandlerMap getNextTaskUsersHandlers() {
		return nextTaskUsersHandlers;
	}

	public void setNextTaskUsersHandlers(NextTaskUsersHandlerMap nextTaskUsersHandlers) {
		this.nextTaskUsersHandlers = nextTaskUsersHandlers;
	}

	public static class NextTaskUsersHandlerMap extends LinkedHashMap<String,NextTaskUsersHandler>
	{
		public NextTaskUsersHandlerMap(Map<String, NextTaskUsersHandler> map)
		{
			super(map);
		}
	}

	public static class WorkflowActionListenerMap extends LinkedHashMap<String,WorkflowActionListener>
	{
		public WorkflowActionListenerMap(Map<String, WorkflowActionListener> map)
		{
			super(map);
		}
	}
}
