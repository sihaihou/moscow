package com.reyco.moscow.core.HealthCheck;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.core.Service;
import com.reyco.moscow.core.domain.Instance;
import com.reyco.moscow.core.event.InstanceDeleteTimeoutEvent;
import com.reyco.moscow.core.event.InstanceHeartbeatTimeoutEvent;
import com.reyco.moscow.core.service.ServiceManager;
import com.reyco.moscow.core.udp.PushServer;
import com.reyco.moscow.core.utils.SpringContextUtils;
/**
 * @author reyco
 * @date 2022.03.16
 * @version v1.0.1
 */
public class ClientBeatCheckTask implements Runnable {
	public static final Logger logger = LoggerFactory.getLogger(ClientBeatCheckTask.class);
	private Service service;
	
	public ClientBeatCheckTask(Service service) {
		this.service = service;
	}

	@Override
	public void run() {
		if(service!=null) {
			List<Instance> allIPs = service.allIPs(true);
			for (Iterator<Instance> iterator = allIPs.iterator();iterator.hasNext();) {
				Instance instance = iterator.next();
				if(System.currentTimeMillis() - instance.getLastBeat() > instance.getInstanceHeartBeatTimeOut()) {
					if(instance.isHealthy()) {
						instance.setHealthy(false);
						logger.debug("【服务不健康通知】[instanceId:"+instance.getInstanceId()+",namespaceId："+instance.getNamespaceId()
						+",host:"+instance.getHost()+",port:"+instance.getPort()+",clusterName:"+instance.getClusterName()
						+",serviceName:"+instance.getServiceName()+"],心跳超时15s，服务实例设置不健康!");
						SpringContextUtils.getApplicationContext().getBean(PushServer.class).serviceChanged(service);
						SpringContextUtils.getApplicationContext().publishEvent(new InstanceHeartbeatTimeoutEvent(this, instance));
					}
				}
				if(System.currentTimeMillis() - instance.getLastBeat() > instance.getIpDeleteTimeout()) {
					logger.debug("【服务宕机通知】[instanceId:"+instance.getInstanceId()+",namespaceId："+instance.getNamespaceId()
					+",host:"+instance.getHost()+",port:"+instance.getPort()+",clusterName:"+instance.getClusterName()
					+",serviceName:"+instance.getServiceName()+"],心跳超时30s，服务被移除!");
					deleteIP(instance);
					SpringContextUtils.getApplicationContext().publishEvent(new InstanceDeleteTimeoutEvent(this, instance));
				}
			}
		}
	}
	
	public String taskKey(){
		return service.getName();
	}
	private void deleteIP(Instance instance) {
		ServiceManager serviceManager = SpringContextUtils.getApplicationContext().getBean(ServiceManager.class);
		try {
			serviceManager.removeInstance(instance.getNamespaceId(), instance.getServiceName(), instance.isEphemeral(), instance);
		} catch (MoscowException e) {
			e.printStackTrace();
		}
	}
}
