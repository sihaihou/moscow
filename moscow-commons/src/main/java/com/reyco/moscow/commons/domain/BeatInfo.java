package com.reyco.moscow.commons.domain;

import com.reyco.moscow.commons.ToString;
/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
public class BeatInfo extends ToString{
	private String namespaceId;
	private String clusterName;
	private String serviceName;
	private String groupName;
	private String host;
	private Integer port;
	private double weight;
    private boolean ephemeral = true;
    private Long period;
    private volatile boolean stopped;
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
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
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
	public Long getPeriod() {
		return period;
	}
	public void setPeriod(Long period) {
		this.period = period;
	}
	public boolean isStopped() {
		return stopped;
	}
	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
}
