package com.reyco.moscow.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.reyco.moscow.commons.constans.Constants;
import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.core.HealthCheck.ClientBeatCheckTask;
import com.reyco.moscow.core.HealthCheck.HealthCheckReactor;
import com.reyco.moscow.core.domain.Beat;
import com.reyco.moscow.core.domain.Instance;
import com.reyco.moscow.core.domain.Instances;
import com.reyco.moscow.core.listener.InstancesListener;
import com.reyco.moscow.core.process.ClientBeatProcessor;
import com.reyco.moscow.core.udp.PushServer;
import com.reyco.moscow.core.utils.KeyBuilder;
import com.reyco.moscow.core.utils.SpringContextUtils;

/**
 * @author reyco
 * @date 2022.03.15
 * @version v1.0.1
 */
public class Service implements InstancesListener<Instances> {
	private Map<String, Cluster> clusterMap = new HashMap<>();
	private String namespaceId;
	private String name;
	private ClientBeatCheckTask clientBeatCheckTask = new ClientBeatCheckTask(this);

	public void init() {

		HealthCheckReactor.scheduleCheck(clientBeatCheckTask);

		for (Map.Entry<String, Cluster> entry : clusterMap.entrySet()) {
			entry.getValue().setService(this);
			entry.getValue().init();
		}
	}

	public List<Instance> allIPs() {
		List<Instance> allIPs = new ArrayList<>();
		for (Map.Entry<String, Cluster> entry : clusterMap.entrySet()) {
			allIPs.addAll(entry.getValue().allIPs());
		}

		return allIPs;
	}

	public List<Instance> allIPs(Boolean ephemeral) {
		List<Instance> allIPs = new ArrayList<>();
		for (Map.Entry<String, Cluster> entry : clusterMap.entrySet()) {
			allIPs.addAll(entry.getValue().allIPs(ephemeral));
		}
		return allIPs;
	}

	public List<Instance> allIPs(List<String> clusters) {
		List<Instance> allIPs = new ArrayList<>();
		for (String cluster : clusters) {
			Cluster clusterObj = clusterMap.get(cluster);
			if (clusterObj == null) {
				continue;
			}
			allIPs.addAll(clusterObj.allIPs());
		}
		return allIPs;
	}

	@Override
	public void onChange(String key, Instances value) throws MoscowException {
		updateIPs(value.getInstanceList(), KeyBuilder.matchEphemeralInstanceListKey(key));
	}

	@Override
	public void onDelete(String key) throws MoscowException {

	}

	public void updateIPs(Collection<Instance> instances, boolean ephemeral) {
		Map<String, List<Instance>> ipMap = new HashMap<>(clusterMap.size());
		for (String clusterName : clusterMap.keySet()) {
			ipMap.put(clusterName, new ArrayList<>());
		}
		for (Instance instance : instances) {
			if (StringUtils.isBlank(instance.getClusterName())) {
				instance.setClusterName(Constants.DEFAULT_NAMESPACE_ID);
			}
			if (!clusterMap.containsKey(instance.getClusterName())) {
				Cluster cluster = new Cluster(instance.getClusterName(), this);
				cluster.init();
				clusterMap.put(instance.getClusterName(), cluster);
			}
			List<Instance> instanceIps = ipMap.get(instance.getClusterName());
			if (instanceIps == null) {
				instanceIps = new LinkedList<>();
				ipMap.put(instance.getClusterName(), instanceIps);
			}
			instanceIps.add(instance);
		}
		for (Map.Entry<String, List<Instance>> entry : ipMap.entrySet()) {
			List<Instance> instanceIps = entry.getValue();
			clusterMap.get(entry.getKey()).updateIPs(instanceIps, ephemeral);
		}
		SpringContextUtils.getApplicationContext().getBean(PushServer.class).serviceChanged(this);
	}

	public void processClientBeat(final Beat beat) {
		ClientBeatProcessor clientBeatProcessor = new ClientBeatProcessor();
		clientBeatProcessor.setService(this);
		clientBeatProcessor.setBeat(beat);
		HealthCheckReactor.scheduleNow(clientBeatProcessor);
	}

	public Map<String, Cluster> getClusterMap() {
		return clusterMap;
	}

	public void setClusterMap(Map<String, Cluster> clusterMap) {
		this.clusterMap = clusterMap;
	}

	public String getNamespaceId() {
		return namespaceId;
	}

	public void setNamespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
