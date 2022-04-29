package com.reyco.moscow.core.service;

import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.core.domain.Instances;
import com.reyco.moscow.core.listener.InstancesListener;

/** 
 * @author  reyco
 * @date    2022.03.15
 * @version v1.0.1 
 */
public interface ServerRegisterService<T extends Instances> {
	
	/**
	 * 注册服务
	 * @param t
	 * @throws Exception
	 */
	void register(String key,T t) throws MoscowException;
	
	void remove(String key) throws MoscowException;
	 
	void listen(String key, InstancesListener<Instances> listener) throws MoscowException;
	
	void unlisten(String key, InstancesListener<Instances> listener) throws MoscowException;
}
