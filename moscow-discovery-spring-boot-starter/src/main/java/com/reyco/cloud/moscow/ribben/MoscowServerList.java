package com.reyco.cloud.moscow.ribben;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.reyco.cloud.moscow.domain.Instance;
import com.reyco.cloud.moscow.properties.MoscowDiscoveryProperties;

/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
public class MoscowServerList extends AbstractServerList<MoscowServer> {
	
	private MoscowDiscoveryProperties discoveryProperties;
	
	private String serviceId;
	
	public MoscowServerList(MoscowDiscoveryProperties discoveryProperties) {
		super();
		this.discoveryProperties = discoveryProperties;
	}

	@Override
	public List<MoscowServer> getInitialListOfServers() {
		return getServers();
	}

	@Override
	public List<MoscowServer> getUpdatedListOfServers() {
		return getServers();
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig) {
		this.serviceId = clientConfig.getClientName();
	}
	private List<MoscowServer> getServers() {
		try {
			String groupName = discoveryProperties.getGroupName();
			List<Instance> instances = discoveryProperties.moscowServiceInstance().selectInstances(serviceId,groupName, true);
			return instancesToServerList(instances);
		}catch (Exception e) {
			throw new IllegalStateException("moscow注册中心没有发现服务, serviceId=" + serviceId,e);
		}
	}

	private List<MoscowServer> instancesToServerList(List<Instance> instances) {
		List<MoscowServer> result = new ArrayList<>();
		if (CollectionUtils.isEmpty(instances)) {
			return result;
		}
		for (Instance instance : instances) {
			result.add(new MoscowServer(instance));
		}
		return result;
	}
}
