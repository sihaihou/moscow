package com.reyco.moscow.commons.net;

import com.reyco.moscow.commons.ToString;

/** 
 * @author  reyco
 * @date    2022.04.08
 * @version v1.0.1 
 */
public class UdpRequest extends ToString{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1188158958906800446L;
	/**
	 */
	private int code;
	private int type;
	private String data;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
}
