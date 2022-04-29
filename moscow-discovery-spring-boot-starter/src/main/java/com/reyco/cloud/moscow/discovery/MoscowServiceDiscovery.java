package com.reyco.cloud.moscow.discovery;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;

import com.reyco.cloud.moscow.client.MoscowServiceInstance;
import com.reyco.cloud.moscow.domain.Instance;
import com.reyco.cloud.moscow.properties.MoscowDiscoveryProperties;
import com.reyco.moscow.commons.execption.MoscowException;


/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
public class MoscowServiceDiscovery {
	
	private MoscowDiscoveryProperties discoveryProperties;

	public MoscowServiceDiscovery(MoscowDiscoveryProperties discoveryProperties) {
		this.discoveryProperties = discoveryProperties;
	}

	/**
	 * Return all instances for the given service.
	 * @param serviceId id of service
	 * @return list of instances
	 * @throws NacosException nacosException
	 */
	public List<ServiceInstance> getInstances(String serviceId) throws MoscowException{
		String groupName = discoveryProperties.getGroupName();
		List<Instance> instances = discoveryProperties.moscowServiceInstance()
				.selectInstances(serviceId, groupName, true);
		return hostToServiceInstanceList(instances, serviceId);
	}

	/**
	 * Return the names of all services.
	 * @return list of service names
	 * @throws NacosException nacosException
	 */
	public List<String> getServices() throws MoscowException {
		String groupName = discoveryProperties.getGroupName();
		List<String> services = discoveryProperties.moscowServiceInstance()
				.getServicesOfServer(groupName);
		return services;
	}

	private static List<ServiceInstance> hostToServiceInstanceList(
			List<Instance> instances, String serviceId) {
		List<ServiceInstance> result = new ArrayList<>(instances.size());
		for (Instance instance : instances) {
			ServiceInstance serviceInstance = hostToServiceInstance(instance, serviceId);
			if (serviceInstance != null) {
				result.add(serviceInstance);
			}
		}
		return result;
	}

	private static ServiceInstance hostToServiceInstance(Instance instance,
			String serviceId) {
		if (instance == null || !instance.isEnabled() || !instance.isHealthy()) {
			return null;
		}
		MoscowServiceInstance moscowServiceInstance = new MoscowServiceInstance();
		moscowServiceInstance.setHost(instance.getHost());
		moscowServiceInstance.setPort(instance.getPort());
		moscowServiceInstance.setServiceId(serviceId);
		return moscowServiceInstance;
	}
}
