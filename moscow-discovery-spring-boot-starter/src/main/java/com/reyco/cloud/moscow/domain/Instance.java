package com.reyco.cloud.moscow.domain;

import java.io.Serializable;

/**
 * @author reyco
 * @date 2022.03.15
 * @version v1.0.1
 */
public class Instance implements Serializable {
	
	private volatile long lastBeat = System.currentTimeMillis();
	/**
	 * unique id of this instance.
	 */
	private String instanceId;
	/**
	 * 
	 */
	private String namespaceId;
	/**
	 * instance ip
	 */
	private String host;

	/**
	 * instance port
	 */
	private int port;

	/**
	 * instance weight
	 */
	private double weight = 1.0D;

	/**
	 * instance health status
	 */
	private boolean healthy = true;

	/**
	 * If instance is enabled to accept request
	 */
	private boolean enabled = true;

	/**
	 * If instance is ephemeral
	 *
	 * @since 1.0.0
	 */
	private boolean ephemeral = true;
	private String groupName;
	/**
	 * cluster information of instance
	 */
	private String clusterName;

	/**
	 * Service information of instance
	 */
	private String serviceName;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getNamespaceId() {
		return namespaceId;
	}

	public void setNamespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
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

	public boolean isHealthy() {
		return healthy;
	}

	public void setHealthy(boolean healthy) {
		this.healthy = healthy;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEphemeral() {
		return ephemeral;
	}

	public void setEphemeral(boolean ephemeral) {
		this.ephemeral = ephemeral;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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

	public long getLastBeat() {
		return lastBeat;
	}

	public void setLastBeat(long lastBeat) {
		this.lastBeat = lastBeat;
	}

	@Override
	public String toString() {
		return "Instance [instanceId=" + instanceId + ", namespaceId=" + namespaceId + ", host=" + host + ", port="
				+ port + ", weight=" + weight + ", healthy=" + healthy + ", enabled=" + enabled + ", ephemeral="
				+ ephemeral + ", clusterName=" + clusterName + ", serviceName=" + serviceName + "]";
	}

	@Override
	public int hashCode() {
		return (this.namespaceId + this.instanceId + this.host + this.port).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Instance)) {
			return false;
		}
		Instance host = (Instance) obj;
		return hashCode() == host.hashCode();
	}
	public String toHostPort() {
		return host+":"+port;
	}
}
