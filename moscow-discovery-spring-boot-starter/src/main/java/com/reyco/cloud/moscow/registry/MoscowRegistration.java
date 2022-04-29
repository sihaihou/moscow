package com.reyco.cloud.moscow.registry;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ManagementServerPortUtils;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.reyco.cloud.moscow.properties.MoscowDiscoveryProperties;

/**
 * @author reyco
 * @date 2022.04.01
 * @version v1.0.1
 */
public class MoscowRegistration implements Registration, ServiceInstance,InitializingBean {

	private MoscowDiscoveryProperties moscowDiscoveryProperties;
	private ApplicationContext applicationContext;

	public MoscowRegistration(MoscowDiscoveryProperties moscowDiscoveryProperties,ApplicationContext applicationContext) {
		super();
		this.moscowDiscoveryProperties = moscowDiscoveryProperties;
		this.applicationContext = applicationContext;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, String> metadata = moscowDiscoveryProperties.getMetadata();
		Environment environment = applicationContext.getEnvironment();
		Integer managementPort = ManagementServerPortUtils.getPort(applicationContext);
		if (null != managementPort) {
			metadata.put("port", managementPort.toString());
			String contextPath = environment.getProperty("management.server.servlet.context-path");
			String address = environment.getProperty("management.server.address");
			if (!StringUtils.isEmpty(contextPath)) {
				metadata.put("contextPath", contextPath);
			}
			if (!StringUtils.isEmpty(address)) {
				metadata.put("address", address);
			}
		}
	}
	@Override
	public String getServiceId() {
		return moscowDiscoveryProperties.getServiceName();
	}

	@Override
	public String getHost() {
		return moscowDiscoveryProperties.getHost();
	}

	@Override
	public int getPort() {
		return moscowDiscoveryProperties.getPort();
	}
	public void setPort(int port) {
		this.moscowDiscoveryProperties.setPort(port);
	}
	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public URI getUri() {
		try {
			String url = moscowDiscoveryProperties.getHost() + ":" + moscowDiscoveryProperties.getPort();
			return new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, String> getMetadata() {
		return null;
	}

	public MoscowDiscoveryProperties getMoscowDiscoveryProperties() {
		return moscowDiscoveryProperties;
	}

	public void setMoscowDiscoveryProperties(MoscowDiscoveryProperties moscowDiscoveryProperties) {
		this.moscowDiscoveryProperties = moscowDiscoveryProperties;
	}
}
