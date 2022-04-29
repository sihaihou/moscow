package com.reyco.moscow.core.domain;

import java.io.Serializable;

import com.reyco.moscow.commons.ToString;

/**
 * @author reyco
 * @date 2022.03.25
 * @version v1.0.1
 */
public class Beat extends ToString{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2445330918755531918L;
	private String namespaceId;
	private String clusterName;
	private String serviceName;
	private String host;
	private Integer port;
	private double weight;
    private boolean ephemeral = true;
	public String getNamespaceId() {
		return namespaceId;
	}
	public void setNamespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public boolean isEphemeral() {
		return ephemeral;
	}
	public void setEphemeral(boolean ephemeral) {
		this.ephemeral = ephemeral;
	}
}
