package com.reyco.cloud.moscow.service;

import java.util.List;

import com.reyco.cloud.moscow.domain.Instance;
import com.reyco.moscow.commons.execption.MoscowException;
/**
 * @author reyco
 * @date 2022.04.02
 * @version v1.0.1
 */
public interface MoscowService {

	void registerInstance(String serviceName, String host, int port) throws MoscowException;

	void registerInstance(String serviceName, String host, int port, String clusterName) throws MoscowException;

	void registerInstance(String serviceName, String groupName, String host, int port) throws MoscowException;

	void registerInstance(String serviceName, String groupName, String clusterName, String host, int port)
			throws MoscowException;

	void registerInstance(String serviceName, Instance instance) throws MoscowException;

	void registerInstance(String serviceName, String groupName, Instance instance) throws MoscowException;

	void deregisterInstance(String serviceName, String ip, int port) throws MoscowException;

	void deregisterInstance(String serviceName, String groupName, String ip, int port) throws MoscowException;

	void deregisterInstance(String serviceName, String ip, int port, String clusterName) throws MoscowException;

	void deregisterInstance(String serviceName, String groupName, String ip, int port, String clusterName)
			throws MoscowException;

	void deregisterInstance(String serviceName, Instance instance) throws MoscowException;

	void deregisterInstance(String serviceName, String groupName, Instance instance) throws MoscowException;

	List<Instance> getAllInstances(String serviceName) throws MoscowException;

	List<Instance> getAllInstances(String serviceName, String groupName) throws MoscowException;

	List<Instance> getAllInstances(String serviceName, boolean subscribe) throws MoscowException;

	List<Instance> getAllInstances(String serviceName, String groupName, boolean subscribe) throws MoscowException;

	List<Instance> getAllInstances(String serviceName, List<String> clusters) throws MoscowException;

	List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters) throws MoscowException;

	List<Instance> selectInstances(String serviceName, boolean healthy) throws MoscowException;

	List<Instance> selectInstances(String serviceName, String groupName, boolean healthy) throws MoscowException;

	List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy) throws MoscowException;

	List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy)
			throws MoscowException;

	Instance selectOneHealthyInstance(String serviceName) throws MoscowException;

	Instance selectOneHealthyInstance(String serviceName, String groupName) throws MoscowException;

	Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws MoscowException;

	Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters)
			throws MoscowException;
	
    List<String> getServicesOfServer() throws MoscowException;
    
    List<String> getServicesOfServer(String groupName) throws MoscowException;
    
}
