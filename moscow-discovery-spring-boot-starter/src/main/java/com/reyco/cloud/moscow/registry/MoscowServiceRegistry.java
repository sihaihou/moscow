package com.reyco.cloud.moscow.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import com.reyco.cloud.moscow.domain.Instance;
import com.reyco.cloud.moscow.properties.MoscowDiscoveryProperties;
import com.reyco.cloud.moscow.service.MoscowService;
import com.reyco.moscow.commons.execption.MoscowException;

/** 
 * @author  reyco
 * @date    2022.04.01
 * @version v1.0.1 
 */
public class MoscowServiceRegistry implements ServiceRegistry<Registration>{
	private static final Logger logger = LoggerFactory.getLogger(MoscowServiceRegistry.class);
	private MoscowDiscoveryProperties moscowDiscoveryProperties;
	private MoscowService moscowService;
	
	public MoscowServiceRegistry(MoscowDiscoveryProperties moscowDiscoveryProperties) {
		super();
		this.moscowDiscoveryProperties = moscowDiscoveryProperties;
		this.moscowService = moscowDiscoveryProperties.moscowServiceInstance();
	}

	@Override
	public void register(Registration registration) {
		Instance instance = getMoscowInstanceFromRegistration(registration);
		String serviceName = moscowDiscoveryProperties.getServiceName();
		String groupName = moscowDiscoveryProperties.getGroupName();
		try {
			moscowService.registerInstance(serviceName, groupName, instance);
			logger.debug("【注册服务】serviceName:{},groupName:{},instance:{}",serviceName,groupName,instance);
		} catch (MoscowException e) {
			logger.error("【服务注册失败】serviceName:{},groupName:{},instance:{},msg:",serviceName,groupName,instance,e);
			e.printStackTrace();
		}
	}

	@Override
	public void deregister(Registration registration) {
		
	}

	@Override
	public void close() {
		
	}

	@Override
	public void setStatus(Registration registration, String status) {
	}

	@Override
	public <T> T getStatus(Registration registration) {
		return null;
	}
	private Instance getMoscowInstanceFromRegistration(Registration registration) {
		Instance instance = new Instance();
		instance.setHost(registration.getHost());
		instance.setPort(registration.getPort());
		instance.setInstanceId(registration.getInstanceId());
		instance.setNamespaceId(moscowDiscoveryProperties.getNamespace());
		instance.setWeight(moscowDiscoveryProperties.getWeight());
		instance.setClusterName(moscowDiscoveryProperties.getClusterName());
		instance.setServiceName(moscowDiscoveryProperties.getServiceName());
		instance.setGroupName(moscowDiscoveryProperties.getGroupName());
		return instance;
	}
}
