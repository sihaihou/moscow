package com.reyco.moscow.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.core.domain.Instances;
import com.reyco.moscow.core.listener.InstancesListener;
import com.reyco.moscow.core.service.EphemeralServerRegisterService;
import com.reyco.moscow.core.service.PersistentServerRegisterService;
import com.reyco.moscow.core.service.ServerRegisterService;
import com.reyco.moscow.core.utils.KeyBuilder;

/**
 * @author reyco
 * @date 2022.03.15
 * @version v1.0.1
 */
@Service("delegateServerRegisterService")
public class DelegateServerRegisterServiceImpl implements ServerRegisterService<Instances> {

	@Autowired
	private PersistentServerRegisterService persistentServerRegisterService;

	@Autowired
	private EphemeralServerRegisterService ephemeralServerRegisterService;

	@Override
	public void register(String key, Instances instances) throws MoscowException {
		getMatchServerRegisterService(key).register(key, instances);
	}

	@Override
	public void remove(String key) throws MoscowException {
		getMatchServerRegisterService(key).remove(key);
	}

	@Override
	public void listen(String key, InstancesListener<Instances> listener) throws MoscowException {
		if (KeyBuilder.LISTENER_KEY_PREFIX.equals(key)) {
			persistentServerRegisterService.listen(key, listener);
			ephemeralServerRegisterService.listen(key, listener);
			return;
		}
		getMatchServerRegisterService(key).listen(key, listener);
	}

	@Override
	public void unlisten(String key, InstancesListener<Instances> listener) throws MoscowException {
		getMatchServerRegisterService(key).unlisten(key, listener);
	}

	/**
	 * 获取匹配ServerRegisterService
	 * 
	 * @param ephemeral
	 * @return
	 */
	private ServerRegisterService<Instances> getMatchServerRegisterService(String key) {
		return KeyBuilder.matchEphemeralInstanceListKey(key) ? ephemeralServerRegisterService
				: persistentServerRegisterService;
	}
}
