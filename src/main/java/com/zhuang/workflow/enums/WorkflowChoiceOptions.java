package com.zhuang.workflow.enums;

public class WorkflowChoiceOptions {

	private static final String STOREKEY = "choice";

	public static final String SAVE = "保存";
	
	public static final String DELETE = "刪除";

	public static final String BACK = "退回";

	public static final String REJECT = "驳回";
	
	public static final String APPROVE = "批准";

	public static final String SUBMIT = "提交";

	public static final String AGREE = "同意";

	public static final String DISAGREE = "不同意";

	public static String getStoreKey() {
		return STOREKEY;
	}
}
