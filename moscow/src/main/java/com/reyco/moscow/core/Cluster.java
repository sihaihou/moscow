package com.reyco.moscow.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.reyco.moscow.core.HealthCheck.ClusterHealthCheckTask;
import com.reyco.moscow.core.HealthCheck.HealthCheckReactor;
import com.reyco.moscow.core.domain.Instance;

/**
 * @author reyco
 * @date 2022.03.15
 * @version v1.0.1
 */
public class Cluster {
	/**
	 * Name of belonging service
	 */
	private String serviceName;

	/**
	 * Name of cluster
	 */
	private String name;

	private Set<Instance> persistentInstances = new HashSet<>();

	private Set<Instance> ephemeralInstances = new HashSet<>();

	private Service service;

	private ClusterHealthCheckTask clusterHealthCheckTask;

	private volatile boolean inited = false;

	public Cluster(String clusterName, Service service) {
		this.name = clusterName;
		this.service = service;
	}

	public void init() {
		if (inited) {
			return;
		}
		clusterHealthCheckTask = new ClusterHealthCheckTask(this);

		HealthCheckReactor.scheduleCheck(clusterHealthCheckTask);
		inited = true;
	}

	public List<Instance> allIPs() {
		List<Instance> allInstances = new ArrayList<>();
		allInstances.addAll(persistentInstances);
		allInstances.addAll(ephemeralInstances);
		return allInstances;
	}

	public List<Instance> allIPs(boolean ephemeral) {
		return ephemeral ? new ArrayList<>(ephemeralInstances) : new ArrayList<>(persistentInstances);
	}

	public void updateIPs(List<Instance> instances, boolean ephemeral) {

		Set<Instance> toUpdateInstances = ephemeral ? ephemeralInstances : persistentInstances;

		HashMap<String, Instance> oldIPMap = new HashMap<>(toUpdateInstances.size());

		for (Instance ip : toUpdateInstances) {
			oldIPMap.put(ip.toHostPort(), ip);
		}
		List<Instance> updatedIPs = subtract(instances, oldIPMap.values());
		for (Instance instance : updatedIPs) {
			if (!instance.isHealthy()) {
				instance.setHealthy(true);
			}
		}
		List<Instance> ips = subtract(oldIPMap.values(), instances);
		toUpdateInstances = new HashSet<>(ips);
		if (ephemeral) {
			ephemeralInstances = toUpdateInstances;
		} else {
			persistentInstances = toUpdateInstances;
		}
	}

	public List<Instance> subtract(Collection<Instance> a, Collection<Instance> b) {
		Map<String, Instance> oldInstancesMap = new ConcurrentHashMap<String, Instance>();
		for (Instance oldInstance : a) {
			oldInstancesMap.put(oldInstance.toHostPort(), oldInstance);
		}
		Map<String, Instance> newInstanceMap = new ConcurrentHashMap<>();
		for (Instance newInstance : b) {
			if (!oldInstancesMap.containsKey(newInstance.toHostPort())) {
				newInstanceMap.put(newInstance.toHostPort(), newInstance);
			}
		}
		return new ArrayList<>(newInstanceMap.values());
	}

	public Set<Instance> getPersistentInstances() {
		return persistentInstances;
	}

	public void setPersistentInstances(Set<Instance> persistentInstances) {
		this.persistentInstances = persistentInstances;
	}

	public Set<Instance> getEphemeralInstances() {
		return ephemeralInstances;
	}

	public void setEphemeralInstances(Set<Instance> ephemeralInstances) {
		this.ephemeralInstances = ephemeralInstances;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		if (this.service != null) {
			return;
		}
		this.service = service;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
