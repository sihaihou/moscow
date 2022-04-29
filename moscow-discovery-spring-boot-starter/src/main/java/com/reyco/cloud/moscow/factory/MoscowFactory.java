package com.reyco.cloud.moscow.factory;

import java.lang.reflect.Constructor;
import java.util.Properties;

import com.reyco.cloud.moscow.service.MoscowService;
import com.reyco.moscow.commons.execption.MoscowException;

/**
 * @author reyco
 * @date 2022.04.06
 * @version v1.0.1
 */
public class MoscowFactory {

	public static MoscowService createMoscowService(Properties properties) throws MoscowException {
		try {
			Class<?> moscowServiceClass = Class.forName("com.reyco.cloud.moscow.service.MoscowServiceImpl");
			Constructor<?> constructor =  moscowServiceClass.getConstructor(Properties.class);
			MoscowService moscowService = (MoscowService) constructor.newInstance(properties);
			return moscowService;
		} catch (Throwable e) {
			throw new MoscowException(500, e);
		}
	}
}
