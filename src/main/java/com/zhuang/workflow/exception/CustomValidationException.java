package com.zhuang.workflow.exception;

/**
 * 自定义校验异常
 * @author zwb
 *
 */
public class CustomValidationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2353369970951982508L;

	public CustomValidationException() {
		super();
	}
	
	public CustomValidationException(String s) {
		super(s);
	}
	
}
