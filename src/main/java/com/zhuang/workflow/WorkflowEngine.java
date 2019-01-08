package com.zhuang.workflow;

import java.util.List;
import java.util.Map;

import com.zhuang.workflow.model.NextTaskInfo;

/**
 * 工作流引擎接口
 * 
 * @author zwb
 *
 */
public interface WorkflowEngine {
	/**
	 * 启动新的流程实例
	 * 
	 * @param processDefinitionKey
	 *            流程定义Key
	 * @param userId
	 *            用户ID
	 * @param businessKey
	 *            业务表主键
	 * @param formData
	 *            业务表单数据
	 * @return
	 */
	String startNew(String processDefinitionKey, String userId, String businessKey, Map<String, Object> formData);

	/**
	 * 保存
	 * 
	 * @param taskId
	 *            任务ID
	 * @param comment
	 *            备注（审批意见）
	 * @param formData
	 *            业务表单数据
	 */
	void save(String taskId, String comment, Map<String, Object> formData);

	/**
	 * 提交
	 * 
	 * @param taskId
	 *            任务ID
	 * @param userId
	 *            用户ID
	 * @param nextUsers
	 *            下一步处理人
	 * @param comment
	 *            备注（审批意见）
	 * @param formData
	 *            业务表单数据
	 */
	void submit(String taskId,String userId, List<String> nextUsers, String comment, Map<String, Object> formData);

	 /**
	 * 删除
	 * @param taskId
	 *            任务ID
	 * @param comment
	 *            备注（审批意见）
	 * @param formData
	 *            业务表单数据
	 */
	void delete(String taskId, String comment, Map<String, Object> formData);
	
	/**
	 * 计算下一步处理人
	 * @param taskId
	 * @param formData
	 * @return
	 */
	NextTaskInfo retrieveNextTaskInfo(String taskId, Map<String, Object> formData);
	
	/***
	 * 获取表单数据
	 * @param taskId
	 * @return
	 */
	Map<String, Object> retrieveFormData(String taskId);
}
