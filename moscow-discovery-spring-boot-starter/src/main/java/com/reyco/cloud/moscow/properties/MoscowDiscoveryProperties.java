package com.reyco.cloud.moscow.properties;

import java.net.SocketException;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;

import com.reyco.cloud.moscow.factory.MoscowFactory;
import com.reyco.cloud.moscow.service.MoscowService;

/**
 * @author reyco
 * @date 2022.04.01
 * @version v1.0.1
 */
@ConfigurationProperties(MoscowDiscoveryProperties.PREFIX)
public class MoscowDiscoveryProperties {
	
	private static final Logger logger = LoggerFactory.getLogger(MoscowDiscoveryProperties.class);

	public final static String PREFIX = "spring.cloud.moscow.discovery";

	@Autowired
	private InetUtils inetUtils;
	
	private String host;

	private int port = -1;

	private String serverAddr;
	/**
	 * 命名空间
	 */
	private String namespace = "public";
	/**
	 * 服务名称
	 */
	private String serviceName;
	/**
	 * 权重
	 */
	private float weight = 1;
	/**
	 * 集群名称
	 */
	private String clusterName = "DEFAULT";

	/**
	 * 组
	 */
	private String groupName = "DEFAULT_GROUP";
	/**
	 * 是否开启注册
	 */
	private boolean registerEnabled = true;

	private Map<String, String> metadata;

	private static MoscowService moscowService;

	@PostConstruct
	public void init() throws SocketException {
		this.host = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
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

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroup(String groupName) {
		this.groupName = groupName;
	}

	public boolean isRegisterEnabled() {
		return registerEnabled;
	}

	public void setRegisterEnabled(boolean registerEnabled) {
		this.registerEnabled = registerEnabled;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public MoscowService moscowServiceInstance() {
		if (null != moscowService) {
			return moscowService;
		}
		try {
			moscowService = MoscowFactory.createMoscowService(getMoscowProperties());
		} catch (Exception e) {
			logger.error("create naming service error!properties={},e=,", this, e);
			return null;
		}
		return moscowService;
	}

	private Properties getMoscowProperties() {
		Properties properties = new Properties();
		properties.put("serverAddr", serverAddr);
		properties.put("clusterName", clusterName);
		properties.put("namespace", namespace);
		return properties;
	}
}
