package com.reyco.cloud.moscow.service;

import com.reyco.cloud.moscow.domain.ServiceInfo;

/** 
 * @author  reyco
 * @date    2022.04.07
 * @version v1.0.1 
 */
public interface HostService {
	
	ServiceInfo getServiceInfo(String serviceName, String clusters);
	
	void proccessServiceJson(String json);
}
