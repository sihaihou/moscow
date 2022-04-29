package com.reyco.cloud.moscow.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.reyco.cloud.moscow.domain.Instance;
import com.reyco.cloud.moscow.domain.ServiceInfo;
import com.reyco.cloud.moscow.net.MoscowServiceProxy;
import com.reyco.cloud.moscow.utils.MoscowUtils;
import com.reyco.moscow.commons.constans.Constants;
import com.reyco.moscow.commons.domain.BeatInfo;
import com.reyco.moscow.commons.execption.MoscowException;

/**
 * @author reyco
 * @date 2022.04.02
 * @version v1.0.1
 */
public class MoscowServiceImpl implements MoscowService {
	
	private String cacheDir;
	private MoscowServiceProxy moscowServiceProxy;
	
	private BeatService beatService;
	
	private HostService hostService;
	
	public static void main(String[] args) throws MoscowException, IOException {
		MoscowServiceProxy moscowServiceProxy = new MoscowServiceProxy("", "127.0.0.1");
		BeatService beatService = new BeatServiceImpl(moscowServiceProxy, 2);
		MoscowService moscowService = new MoscowServiceImpl(moscowServiceProxy,beatService);
		moscowService.registerInstance("orderServer", "", "order", "127.0.0.1", 8001);
		System.in.read();
	}
	public MoscowServiceImpl(Properties properties) {
		init(properties);
	}
	private void init(Properties properties) {
		String namespace = properties.getProperty("namespace");
		String address = properties.getProperty("serverAddr");
		moscowServiceProxy = new MoscowServiceProxy(namespace, address);
		beatService = new BeatServiceImpl(moscowServiceProxy, 2);
		hostService = new HostServiceImpl(moscowServiceProxy,cacheDir,false,2);
    }
	public MoscowServiceImpl(MoscowServiceProxy moscowServiceProxy,BeatService beatService) {
		super();
		this.moscowServiceProxy = moscowServiceProxy;
		this.beatService = beatService;
	}

	@Override
	public void registerInstance(String serviceName, String host, int port) throws MoscowException {

	}

	@Override
	public void registerInstance(String serviceName, String host, int port, String clusterName) throws MoscowException {

	}

	@Override
	public void registerInstance(String serviceName, String groupName, String host, int port) throws MoscowException {

	}

	@Override
	public void registerInstance(String serviceName, String groupName, String clusterName, String host, int port)
			throws MoscowException {
		Instance instance = new Instance();
		instance.setHost(host);
		instance.setPort(port);
		instance.setWeight(1.0);
		instance.setClusterName(clusterName);
		registerInstance(serviceName, groupName, instance);
	}

	@Override
	public void registerInstance(String serviceName, Instance instance) throws MoscowException {

	}

	@Override
	public void registerInstance(String serviceName, String groupName, Instance instance) throws MoscowException {
		serviceName=MoscowUtils.getServiceName(groupName, serviceName);
		 if (instance.isEphemeral()) {
	            BeatInfo beatInfo = new BeatInfo();
	            beatInfo.setServiceName(serviceName);
	            beatInfo.setHost(instance.getHost());
	            beatInfo.setPort(instance.getPort());
	            beatInfo.setClusterName(instance.getClusterName());
	            beatInfo.setWeight(instance.getWeight());
	            beatInfo.setPeriod(Constants.DEFAULT_HEART_BEAT_INTERVAL);
	            beatService.addBeatInfo(serviceName, beatInfo);
		 }
		 moscowServiceProxy.registerService(serviceName, groupName, instance);
	}

	@Override
	public void deregisterInstance(String serviceName, String ip, int port) throws MoscowException {

	}

	@Override
	public void deregisterInstance(String serviceName, String groupName, String ip, int port) throws MoscowException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deregisterInstance(String serviceName, String ip, int port, String clusterName) throws MoscowException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deregisterInstance(String serviceName, String groupName, String ip, int port, String clusterName)
			throws MoscowException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deregisterInstance(String serviceName, Instance instance) throws MoscowException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deregisterInstance(String serviceName, String groupName, Instance instance) throws MoscowException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Instance> getAllInstances(String serviceName) throws MoscowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, String groupName) throws MoscowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, boolean subscribe) throws MoscowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, String groupName, boolean subscribe)
			throws MoscowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, List<String> clusters) throws MoscowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters)
			throws MoscowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Instance> selectInstances(String serviceName, boolean healthy) throws MoscowException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Instance> selectInstances(String serviceName, String groupName, boolean healthy)
			throws MoscowException {
		return selectInstances(serviceName, groupName, new ArrayList<String>(), healthy);
	}

	@Override
	public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy)
			throws MoscowException {
		return selectInstances(serviceName,Constants.DEFAULT_GROUP_ID,clusters, healthy);
	}

	@Override
	public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy)
			throws MoscowException {
		ServiceInfo serviceInfo = hostService.getServiceInfo(MoscowUtils.getServiceName(groupName, serviceName), StringUtils.join(clusters, ","));
		return serviceInfo.getInstances();
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName) throws MoscowException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, String groupName) throws MoscowException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws MoscowException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters)
			throws MoscowException {
		return null;
	}
	@Override
	public List<String> getServicesOfServer() throws MoscowException {
		return getServicesOfServer(null);
	}
	@Override
	public List<String> getServicesOfServer(String groupName) throws MoscowException {
		return moscowServiceProxy.getServiceList(groupName);
	}

}
