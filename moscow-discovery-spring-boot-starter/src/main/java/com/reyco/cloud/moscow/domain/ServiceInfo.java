package com.reyco.cloud.moscow.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.reyco.moscow.commons.constans.Constants;

/**
 * @author reyco
 * @date 2022.04.07
 * @version v1.0.1
 */
public class ServiceInfo {
	
	private List<Instance> instances = new ArrayList<Instance>();
	
	private String serviceName;

	private String groupName;

	private String clusterName;
	
	private long lastRefTime = 0L;
	
	private long cacheMillis = 1000L;

	public ServiceInfo(String serviceName, String clusterName) {
		super();
		this.serviceName = serviceName;
		this.clusterName = clusterName;
	}

	public String getKey() {
		return getKey(serviceName, clusterName);
	}
	
	public static String getKey(String serviceName, String clusterName) {
		if (StringUtils.isNotBlank(clusterName)) {
			return serviceName + Constants.SERVICE_INFO_SPLITER + clusterName;
		}
		return serviceName;
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

	public String getClusterName() {
		return clusterName;
	}

	public long getCacheMillis() {
		return cacheMillis;
	}

	public void setCacheMillis(long cacheMillis) {
		this.cacheMillis = cacheMillis;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public long getLastRefTime() {
		return lastRefTime;
	}

	public void setLastRefTime(long lastRefTime) {
		this.lastRefTime = lastRefTime;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}
}
