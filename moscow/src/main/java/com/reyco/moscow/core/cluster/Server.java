package com.reyco.moscow.core.cluster;

import java.util.Objects;

import com.reyco.moscow.commons.ToString;
import com.reyco.moscow.core.constans.Constant;

/** 
 * @author  reyco
 * @date    2022.04.15
 * @version v1.0.1 
 */
public class Server extends ToString {
	private String host;
	private int port;
	private double weight=1.0;
	private Boolean alive = false;
	private Long lastRefreshTime;
	public Server() {
	}
	public Server(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public Boolean getAlive() {
		return alive;
	}
	public void setAlive(Boolean alive) {
		this.alive = alive;
	}
	public Long getLastRefreshTime() {
		return lastRefreshTime;
	}
	public void setLastRefreshTime(Long lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}
	public String getKey(){
		return host+Constant.COLON+port;
	}
	@Override
	public int hashCode() {
		return Objects.hash(getKey());
	}
	@Override
	public boolean equals(Object obj) {
		if(obj==null) {
			return false;
		}
		if(!(obj instanceof Server)) {
			return false;
		}
		Server server = (Server)obj;
		return Objects.equals(this.getKey(), server.getKey());
	}
}
