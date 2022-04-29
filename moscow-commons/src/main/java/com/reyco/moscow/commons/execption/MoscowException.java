package com.reyco.moscow.commons.execption;


/** 
 * @author  reyco
 * @date    2022.03.16
 * @version v1.0.1 
 */
public class MoscowException extends Exception{
	private Integer code;
	private String msg;

	public MoscowException(String msg) {
		this.msg = msg;
	}
	public MoscowException(Integer code, String msg) {
		super(msg);
		this.code = code;
		this.msg = msg;
	}
	public MoscowException(Integer code,Throwable e) {
		super(e);
		this.code = code;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
