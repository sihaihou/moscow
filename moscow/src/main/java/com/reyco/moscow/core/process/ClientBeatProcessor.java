package com.reyco.moscow.core.process;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reyco.moscow.core.Cluster;
import com.reyco.moscow.core.Service;
import com.reyco.moscow.core.domain.Beat;
import com.reyco.moscow.core.domain.Instance;
import com.reyco.moscow.core.udp.PushServer;
import com.reyco.moscow.core.utils.SpringContextUtils;

/**
 * @author reyco
 * @date 2022.03.25
 * @version v1.0.1
 */
public class ClientBeatProcessor implements Runnable {
	public static final Logger logger = LoggerFactory.getLogger(ClientBeatProcessor.class);
	private Beat beat;
	private Service service;
	public Beat getBeat() {
		return beat;
	}
	public void setBeat(Beat beat) {
		this.beat = beat;
	}
	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}
	@Override
	public void run() {
		Service service = this.service;
        String host = beat.getHost();
        String clusterName = beat.getClusterName();
        Integer port = beat.getPort();
        Cluster cluster = service.getClusterMap().get(clusterName);
        List<Instance> instances = cluster.allIPs(true);
        for (Instance instance : instances) {
            if (instance.getHost().equals(host) && instance.getPort()==port) {
                instance.setLastBeat(System.currentTimeMillis());
                if (!instance.isHealthy()) {
                    instance.setHealthy(true);
                    logger.debug("【异常服务恢复通知】service:{}，host：{}:{}@{},msg:client beat ok",
                        cluster.getService().getName(), host, port, cluster.getName());
                    SpringContextUtils.getApplicationContext().getBean(PushServer.class).serviceChanged(service);
                }
            }
        }
	}
}
