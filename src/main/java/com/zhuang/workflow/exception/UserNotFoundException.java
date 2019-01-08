package com.zhuang.workflow.exception;

/**
 * 没有找到用户导常
 * @author zwb
 *
 */
public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException() {
		super();
	}

	public UserNotFoundException(String s) {
		super(s);
	}

}
