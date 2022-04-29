package com.reyco.cloud.moscow.service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.reyco.cloud.moscow.domain.Instance;
import com.reyco.cloud.moscow.domain.ServiceInfo;
import com.reyco.cloud.moscow.net.MoscowServiceProxy;
import com.reyco.cloud.moscow.utils.DiskUtils;

/**
 * @author reyco
 * @date 2022.04.07
 * @version v1.0.1
 */
public class HostServiceImpl implements HostService {

	private static final Logger logger = LoggerFactory.getLogger(HostServiceImpl.class);
	
	public static final long DEFAULT_DELAY = 2000L;

	private static final long UPDATE_HOLD_INTERVAL = 5000L;

	private final Map<String, ScheduledFuture<?>> futureMap = new HashMap<String, ScheduledFuture<?>>();

	private MoscowServiceProxy moscowServiceProxy;

	private Map<String, ServiceInfo> serviceInfoMap;

	private Map<String, Object> updatingMap;
	
	private PushClient pushClient;
	
	private ScheduledExecutorService executor;

	private String cacheDir;

	public HostServiceImpl(MoscowServiceProxy moscowServiceProxy, String cacheDir, Boolean loadCache, int threadCount) {
		executor = new ScheduledThreadPoolExecutor(threadCount, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				thread.setName("com.reyco.moscow.client.updater");
				return thread;
			}
		});
		this.moscowServiceProxy = moscowServiceProxy;
		this.cacheDir = cacheDir;
		if (loadCache) {
			this.serviceInfoMap = new ConcurrentHashMap<String, ServiceInfo>(DiskUtils.read(this.cacheDir));
		} else {
			this.serviceInfoMap = new ConcurrentHashMap<String, ServiceInfo>();
		}
		this.updatingMap = new ConcurrentHashMap<String, Object>();
		this.pushClient = new PushClient(this);
	}

	@Override
	public ServiceInfo getServiceInfo(String serviceName, String clusterName) {
		ServiceInfo serviceInfoCache = getServiceInfoCache(serviceName, clusterName);
		if (serviceInfoCache == null) {
			serviceInfoCache = new ServiceInfo(serviceName, clusterName);
			putServiceInfoCache(serviceInfoCache.getKey(), serviceInfoCache);
			update(serviceName, clusterName);
		} else if (isUpdating(serviceName)) {
			if (UPDATE_HOLD_INTERVAL > 0) {
				synchronized (serviceInfoCache) {
					try {
						serviceInfoCache.wait(UPDATE_HOLD_INTERVAL);
					} catch (InterruptedException e) {
						logger.error("[获取服务列表失败] serviceName:" + serviceName + ", clusterName:" + clusterName, e);
					}
				}
			}
		}
		scheduleUpdateIfAbsent(serviceName, clusterName);
		return getServiceInfoCache(serviceName, clusterName);
	}

	private Boolean isUpdating(String serviceName) {
		return updatingMap.containsKey(serviceName);
	}

	private void update(String serviceName, String clusters) {
		updatingMap.put(serviceName, new Object());
		updateServiceNow(serviceName, clusters);
		updatingMap.remove(serviceName);
	}

	/**
	 * @param serviceName
	 * @param clusters
	 */
	private void updateServiceNow(String serviceName, String clusterName) {
		ServiceInfo serviceInfoCache = getServiceInfoCache(serviceName, clusterName);
		try {
			String result = moscowServiceProxy.queryList(serviceName, clusterName,InetAddress.getLocalHost().getHostAddress(),pushClient.getUdpPort(), false);
			if (StringUtils.isNotBlank(result)) {
				JSONObject resultJson = JSON.parseObject(result);
				JSONObject dataJson = resultJson.getJSONObject("data");
				JSONObject infoJson = dataJson.getJSONObject("info");
				JSONArray jsonArray = infoJson.getJSONArray("hosts");
				List<Instance> instances = new ArrayList<Instance>();
				for (Object obj : jsonArray) {
					Instance instance = JSON.parseObject(obj.toString(), Instance.class);
					instances.add(instance);
				}
				serviceInfoCache.setInstances(instances);
			}
		} catch (Exception e) {
			logger.error("【服务更新失败】serviceName:{},e:{}", serviceName, e);
		} finally {
			if (serviceInfoCache != null) {
				synchronized (serviceInfoCache) {
					serviceInfoCache.notifyAll();
				}
			}
		}
	}
	public void proccessServiceJson(String json) {
		JSONObject jsonObject = JSON.parseObject(json);
		String serviceName = jsonObject.getString("serviceName");
		String groupName = jsonObject.getString("groupName");
		String clusterName = jsonObject.getString("clusterName");
		Long lastRefTime = jsonObject.getLong("lastRefTime");
		JSONArray jsonArray = jsonObject.getJSONArray("instances");
		List<Instance> instances = new ArrayList<>();
		for (Object instanceJsonObj : jsonArray) {
			Instance instance = JSON.parseObject(instanceJsonObj.toString(), Instance.class);
			instances.add(instance);
		}
		ServiceInfo newServiceInfo = new ServiceInfo(serviceName, clusterName);
		newServiceInfo.setGroupName(groupName);
		newServiceInfo.setLastRefTime(lastRefTime);
		newServiceInfo.setInstances(instances);
		if(newServiceInfo!=null) {
			serviceInfoMap.put(newServiceInfo.getKey(), newServiceInfo);
		}
	}
	public void scheduleUpdateIfAbsent(String serviceName, String clusterName) {
		if (futureMap.get(ServiceInfo.getKey(serviceName, clusterName)) != null) {
			return;
		}
		synchronized (futureMap) {
			if (futureMap.get(ServiceInfo.getKey(serviceName, clusterName)) != null) {
				return;
			}
			ScheduledFuture<?> future = addTask(new UpdateTask(serviceName, clusterName));
			futureMap.put(ServiceInfo.getKey(serviceName, clusterName), future);
		}
	}

	public synchronized ScheduledFuture<?> addTask(UpdateTask task) {
		return executor.schedule(task, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
	}

	private void putServiceInfoCache(String key, ServiceInfo serviceInfoCache) {
		serviceInfoMap.put(key, serviceInfoCache);
	}

	private ServiceInfo getServiceInfoCache(String serviceName, String clusters) {
		return serviceInfoMap.get(ServiceInfo.getKey(serviceName, clusters));
	}

	public class UpdateTask implements Runnable {
		long lastRefTime = Long.MAX_VALUE;
		private String clusterName;
		private String serviceName;

		public UpdateTask(String serviceName, String clusterName) {
			this.serviceName = serviceName;
			this.clusterName = clusterName;
		}

		@Override
		public void run() {
			try {
				ServiceInfo serviceObj = serviceInfoMap.get(ServiceInfo.getKey(serviceName, clusterName));
				if (serviceObj == null) {
					updateServiceNow(serviceName, clusterName);
					executor.schedule(this, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
					return;
				}
				if (serviceObj.getLastRefTime() <= lastRefTime) {
					updateServiceNow(serviceName, clusterName);
					serviceObj = serviceInfoMap.get(ServiceInfo.getKey(serviceName, clusterName));
				} else {
					refreshOnly(serviceName, clusterName);
				}
				lastRefTime = serviceObj.getLastRefTime();
				executor.schedule(this, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
			} catch (Throwable e) {
				logger.warn("[NA] failed to update serviceName: " + serviceName, e);
			}

		}
	}

	public void refreshOnly(String serviceName, String clusters) {
		try {
			moscowServiceProxy.queryList(serviceName, clusters,InetAddress.getLocalHost().getHostAddress(),pushClient.getUdpPort(), false);
		} catch (Exception e) {
			logger.error("[NA] failed to update serviceName: " + serviceName, e);
		}
	}
}
