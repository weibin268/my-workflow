package com.zhuang.workflow.exception;

/**
 * 没有找到运行中的任务
 * @author zwb
 *
 */
public class RunningTaskNotFoundException extends RuntimeException {

	public RunningTaskNotFoundException() {
		super();
	}

	public RunningTaskNotFoundException(String s) {
		super(s);
	}

}
