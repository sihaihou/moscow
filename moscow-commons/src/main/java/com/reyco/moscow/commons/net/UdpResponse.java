package com.reyco.moscow.commons.net;

import java.io.Serializable;

import com.reyco.moscow.commons.ToString;

/** 
 * @author  reyco
 * @date    2022.04.08
 * @version v1.0.1 
 */
public class UdpResponse extends ToString{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4360680599737618662L;
	private int code;
	private int type;
	public String data;
	
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
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
