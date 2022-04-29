package com.reyco.cloud.moscow.discovery;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.reyco.moscow.commons.execption.MoscowException;

/**
 * @author reyco
 * @date 2022.04.07
 * @version v1.0.1
 */
public class MoscowDiscoveryClient implements DiscoveryClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MoscowDiscoveryClient.class);
	
	private MoscowServiceDiscovery serviceDiscovery;

	public MoscowDiscoveryClient(MoscowServiceDiscovery serviceDiscovery) {
		super();
		this.serviceDiscovery = serviceDiscovery;
	}

	@Override
	public String description() {
		return this.getClass().getName();
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		try {
			return serviceDiscovery.getInstances(serviceId);
		} catch (MoscowException e) {
			logger.error("不能获取该:" + serviceId + "的实例,msg:{}",e);
			throw new RuntimeException("不能获取该:" + serviceId + "的实例",e);
		}
	}

	@Override
	public List<String> getServices() {
		try {
			return serviceDiscovery.getServices();
		}catch (Exception e) {
			logger.error("获取服务名称失败", e);
			return Collections.emptyList();
		}
	}

}
