package com.reyco.moscow.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.core.Cluster;
import com.reyco.moscow.core.Service;
import com.reyco.moscow.core.domain.Instance;
import com.reyco.moscow.core.domain.Instances;
import com.reyco.moscow.core.listener.InstancesListener;
import com.reyco.moscow.core.utils.KeyBuilder;

/**
 * @author reyco
 * @date 2022.03.15
 * @version v1.0.1
 */
@Component
public class ServiceManager implements InstancesListener<Instances> {
	
	public static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);
	
	private volatile Object putServiceLock = new Object();
	/**
	 * map<namespaceId,map<serviceName,Service>>
	 */
	private static final Map<String, Map<String, Service>> SERVICE_MAP = new ConcurrentHashMap<>();

	@Resource(name = "delegateServerRegisterService")
	private ServerRegisterService<Instances> serverRegisterService;

	@PostConstruct
	public void init() {
		try {
			serverRegisterService.listen(KeyBuilder.LISTENER_KEY_PREFIX, this);
		} catch (MoscowException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取service
	 * 
	 * @param namespaceId
	 * @param serviceName
	 * @return
	 */
	public Service getService(String namespaceId, String serviceName) {
		if (SERVICE_MAP.get(namespaceId) == null) {
			return null;
		}
		return chooseServiceMap(namespaceId).get(serviceName);
	}

	/**
	 * 
	 * @param namespaceId
	 * @return
	 */
	public Map<String, Service> chooseServiceMap(String namespaceId) {
		return SERVICE_MAP.get(namespaceId);
	}

	/**
	 * 
	 * @param instance
	 * @throws MoscowException
	 */
	public void registerInstance(Instance instance) throws MoscowException {
		createServiceIfNecessary(instance.getNamespaceId(), instance.getServiceName());
		Service service = getService(instance.getNamespaceId(), instance.getServiceName());
		if (service == null) {
			throw new MoscowException(502, "参数异常");
		}
		addInstance(instance.getNamespaceId(), instance.getServiceName(), instance.isEphemeral(), instance);
	}

	public void addInstance(String namespaceId, String serviceName, boolean ephemeral, Instance... ips)
			throws MoscowException {

		String key = KeyBuilder.buildInstanceListKey(namespaceId, serviceName, ephemeral);

		Service service = getService(namespaceId, serviceName);

		synchronized (service) {
			List<Instance> instanceList = addIpAddresses(service, ephemeral, ips);
			Instances instances = new Instances();
			instances.setInstanceList(instanceList);
			serverRegisterService.register(key, instances);
		}
	}

	public void removeInstance(String namespaceId, String serviceName, boolean ephemeral, Instance... ips)
			throws MoscowException {
		Service service = getService(namespaceId, serviceName);

		synchronized (service) {
			removeInstance(namespaceId, serviceName, ephemeral, service, ips);
		}
	}

	public void removeInstance(String namespaceId, String serviceName, boolean ephemeral, Service service,
			Instance... ips) throws MoscowException {

		String key = KeyBuilder.buildInstanceListKey(namespaceId, serviceName, ephemeral);

		List<Instance> instanceList = substractIpAddresses(service, ephemeral, ips);

		Instances instances = new Instances();
		instances.setInstanceList(instanceList);

		serverRegisterService.register(key, instances);
	}

	/**
	 * @param service
	 * @param ephemeral
	 * @param ips
	 * @return
	 */
	private List<Instance> addIpAddresses(Service service, boolean ephemeral, Instance... ips) {
		return updateIpAddresses(service, "add", ephemeral, ips);
	}

	/**
	 * @param service
	 * @param string
	 * @param ephemeral
	 * @param ips
	 * @return
	 */
	private List<Instance> updateIpAddresses(Service service, String action, boolean ephemeral, Instance... ips) {
		List<Instance> currentIPs = service.allIPs(ephemeral);
		Map<String, Instance> instancesMap = new HashMap<>();
		for (Instance instance : currentIPs) {
			instancesMap.put(instance.toHostPort(), instance);
		}
		for (Instance instance : ips) {
			if (service.getClusterMap().containsKey(instance.getClusterName())) {
				Cluster cluster = new Cluster(instance.getClusterName(), service);
				cluster.init();
				service.getClusterMap().put(instance.getClusterName(), cluster);
			}
			if ("remove".equals(action)) {
				instancesMap.remove(instance.toHostPort());
			} else {
				instancesMap.put(instance.toHostPort(), instance);
			}
		}
		return new ArrayList<>(instancesMap.values());
	}

	public Instance getInstance(String namespaceId, String serviceName, String cluster, String ip, int port) {
		Service service = getService(namespaceId, serviceName);
		if (service == null) {
			return null;
		}
		List<String> clusters = new ArrayList<>();
	    clusters.add(cluster);
	    List<Instance> ips = service.allIPs(clusters);
        if (ips == null || ips.isEmpty()) {
            return null;
        }

        for (Instance instance : ips) {
            if (instance.getHost().equals(ip) && instance.getPort() == port) {
                return instance;
            }
        }
        return null;
	}

	public List<Instance> substractIpAddresses(Service service, boolean ephemeral, Instance... ips)
			throws MoscowException {
		return updateIpAddresses(service, "remove", ephemeral, ips);
	}

	private void createServiceIfNecessary(String namespaceId, String serviceName) throws MoscowException {
		Service service = getService(namespaceId, serviceName);
		if (service == null) {
			logger.debug("【创建服务通知】service不存在,创建service【namespaceId=" + namespaceId + ",serviceName=" + serviceName + "】");
			service = new Service();
			service.setName(serviceName);
			service.setNamespaceId(namespaceId);
			putServiceAndInit(service);
		}
	}

	private void putServiceAndInit(Service service) throws MoscowException {
		putService(service);
		service.init();
		logger.debug("【初始化服务通知】初始化service【namespaceId=" + service.getNamespaceId() + ",serviceName=" + service.getName() + "】");
		serverRegisterService.listen(KeyBuilder.buildInstanceListKey(service.getNamespaceId(), service.getName(), true),
				service);
		serverRegisterService
				.listen(KeyBuilder.buildInstanceListKey(service.getNamespaceId(), service.getName(), false), service);
	}

	private void putService(Service service) {
		if (!SERVICE_MAP.containsKey(service.getNamespaceId())) {
			synchronized (putServiceLock) {
				if (!SERVICE_MAP.containsKey(service.getNamespaceId())) {
					SERVICE_MAP.put(service.getNamespaceId(), new ConcurrentHashMap<>(16));
				}
			}
		}
		SERVICE_MAP.get(service.getNamespaceId()).put(service.getName(), service);
	}
	
	public List<String> getAllServiceNameList(String namespaceId) {
        if (chooseServiceMap(namespaceId) == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(chooseServiceMap(namespaceId).keySet());
    }

	@Override
	public void onChange(String key, Instances value) throws MoscowException {

	}

	@Override
	public void onDelete(String key) throws MoscowException {

	}
}
