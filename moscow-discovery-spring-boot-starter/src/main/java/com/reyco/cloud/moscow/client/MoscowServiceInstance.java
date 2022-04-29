package com.reyco.cloud.moscow.client;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
public class MoscowServiceInstance implements ServiceInstance {
	private String serviceId;

	private String host;

	private int port;

	private boolean secure;
	@Override
	public String getServiceId() {
		return serviceId;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public URI getUri() {
		return null;
	}

	@Override
	public Map<String, String> getMetadata() {
		return null;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}
}
