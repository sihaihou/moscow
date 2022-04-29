package com.reyco.moscow.core.service.impl;

import org.springframework.stereotype.Service;

import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.core.domain.Instances;
import com.reyco.moscow.core.listener.InstancesListener;
import com.reyco.moscow.core.service.PersistentServerRegisterService;

/** 
 * @author  reyco
 * @date    2022.03.15
 * @version v1.0.1 
 */
@Service
public class PersistentServerRegisterServiceImpl implements PersistentServerRegisterService {
	
	@Override
	public void register(String key, Instances instances) throws MoscowException {
		
	}

	@Override
	public void remove(String key) throws MoscowException {
		
	}

	@Override
	public void listen(String key, InstancesListener listener) throws MoscowException {
		
	}

	@Override
	public void unlisten(String key, InstancesListener listener) throws MoscowException {
		
	}

}
