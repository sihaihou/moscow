package com.reyco.moscow.core.controller;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reyco.moscow.commons.constans.BeatType;
import com.reyco.moscow.commons.constans.Constants;
import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.commons.utils.JsonUtils;
import com.reyco.moscow.commons.utils.R;
import com.reyco.moscow.core.Cluster;
import com.reyco.moscow.core.Service;
import com.reyco.moscow.core.domain.Beat;
import com.reyco.moscow.core.domain.Instance;
import com.reyco.moscow.core.service.ServiceManager;
import com.reyco.moscow.core.udp.PushServer;
import com.reyco.moscow.core.utils.RequestUtils;

/**
 * @author reyco
 * @date 2022.03.15
 * @version v1.0.1
 */
@RestController
@RequestMapping("/v1/moscow/instance")
public class InstanceController {
	public static final Logger logger = LoggerFactory.getLogger(InstanceController.class);
	@Autowired
	private ServiceManager serviceManager;
	
	@Autowired
	private PushServer pushServer;
	
	@PostMapping
	public Object register(HttpServletRequest request) throws Exception {
		Instance instance = getInstance(request);
		logger.debug("【注册服务通知】："+instance);
		serviceManager.registerInstance(instance);
		return "ok";
	}
	@DeleteMapping
	public Object deregister(HttpServletRequest request) throws Exception {
		Instance instance = getInstance(request);
		logger.debug("【取消服务通知】："+instance);
		serviceManager.removeInstance(instance.getNamespaceId(), instance.getServiceName(), instance.isEphemeral(), instance);
		return "ok";
	}
	@GetMapping("/list")
	public Object list(HttpServletRequest request) throws Exception {
		String namespaceId = RequestUtils.optional(request, "namespaceId",Constants.DEFAULT_NAMESPACE_ID);
		String clusterName = RequestUtils.optional(request, "clusterName",Constants.DEFAULT_CLUSTER_ID);
		String serviceName = RequestUtils.required(request, "serviceName");
		String host = RequestUtils.optional(request, "host", StringUtils.EMPTY);
		Integer udpPort = Integer.parseInt(RequestUtils.optional(request, "udpPort", "0"));
		logger.debug("【拉取服务通知】namespaceId：{},clusterName:{},serviceName:{},host:{},udpPort:{}",namespaceId,clusterName,serviceName,host,udpPort);
		if(udpPort>0) {
			pushServer.addClient(namespaceId, serviceName, clusterName, new InetSocketAddress(host, udpPort));
		}
		Service service;
		Map<String, Object> result = new HashMap<String,Object>();
		result.put("name", serviceName);
        result.put("clusterName", clusterName);
		if((service=serviceManager.getService(namespaceId, serviceName))==null) {
			logger.debug("no instance to serve for service: {}"+serviceName);
            result.put("hosts",new ArrayList<>());
			return R.success(result);
		}
		Map<String, Cluster> clusterMap;
		Cluster cluster;
		if((clusterMap=service.getClusterMap())!=null) {
			if((cluster = clusterMap.get(clusterName))!=null) {
				Set<Instance> instanceSet = cluster.getEphemeralInstances();
				instanceSet.addAll(cluster.getPersistentInstances());
				instanceSet.stream().filter(instance->{
					return instance.isHealthy();
				});
				result.put("hosts", instanceSet);
				return R.success(result);
			}
		}
		result.put("hosts",new ArrayList<>());
		return R.success(result);
	}
	
	@PutMapping("/beat")
    public Object beat(HttpServletRequest request) throws Exception {
		String namespaceId = RequestUtils.optional(request, "namespaceId",Constants.DEFAULT_NAMESPACE_ID);
		String clusterName = RequestUtils.optional(request, "clusterName",Constants.DEFAULT_CLUSTER_ID);
		String serviceName = RequestUtils.required(request, "serviceName");
		String beatJson = RequestUtils.required(request, "beat");
		Beat beat = JsonUtils.jsonToObj(beatJson, Beat.class);
		logger.debug("【心跳通知】beat:{}",beat);
		Instance instance = serviceManager.getInstance(namespaceId, serviceName, clusterName, beat.getHost(), beat.getPort());
		if(instance==null) {
			instance = new Instance();
			instance.setNamespaceId(namespaceId);
			instance.setClusterName(clusterName);
			instance.setServiceName(serviceName);
			instance.setHost(beat.getHost());
			instance.setPort(beat.getPort());
			instance.setHealthy(true);
			instance.setWeight(beat.getWeight());
			instance.setEnabled(true);
			instance.setEphemeral(beat.isEphemeral());
			serviceManager.registerInstance(instance);
		}
		Service service = serviceManager.getService(namespaceId, serviceName);
        if (service == null) {
            throw new MoscowException("service not found: " + serviceName + "@" + namespaceId);
        }
        service.processClientBeat(beat);
        Map<String, Object> result = new HashMap<String,Object>();
        result.put("clientBeatInterval", BeatType.SERVICE_BEAT_INTERVAL.getTime());
        return result;
	}
	/**
	 * 获取请求Instance
	 * @param request
	 * @return
	 */
	private Instance getInstance(HttpServletRequest request) {
		String namespaceId = RequestUtils.optional(request, "namespaceId",Constants.DEFAULT_NAMESPACE_ID);
		String clusterName = RequestUtils.optional(request, "clusterName",Constants.DEFAULT_CLUSTER_ID);
		String serviceName = RequestUtils.required(request, "serviceName");
		String host = RequestUtils.required(request, "host");
		String port = RequestUtils.required(request, "port");
		String weight = RequestUtils.optional(request, "weight",Constants.INSTANCE_WEIGHT.toString());
		String healthyStr = RequestUtils.optional(request, "healthy",Constants.INSTANCE_HEALTHY.toString());
		Boolean healthy = true;
		if(StringUtils.isNotBlank(healthyStr)) {
			healthy = BooleanUtils.toBoolean(healthyStr);
		}
		String enabledStr = RequestUtils.optional(request, "enabled",Constants.INSTANCE_ENABLED.toString());
		Boolean enabled = true;
		if(StringUtils.isNotBlank(enabledStr)) {
			enabled = BooleanUtils.toBoolean(enabledStr);
		}
		Boolean ephemeral = true;
		String ephemeralStr = RequestUtils.optional(request, "ephemeral",Constants.INSTANCE_EPHEMERAL.toString());
		if(StringUtils.isNotBlank(ephemeralStr)) {
			ephemeral = BooleanUtils.toBoolean(ephemeralStr);
		}
		Instance instance = new Instance();
		instance.setNamespaceId(namespaceId);
		instance.setClusterName(clusterName);
		instance.setServiceName(serviceName);
		instance.setHost(host);
		instance.setPort(Integer.parseInt(port));
		instance.setHealthy(healthy);
		instance.setWeight(Double.parseDouble(weight));
		instance.setEnabled(enabled);
		instance.setEphemeral(ephemeral);
		return instance;
	}
}
