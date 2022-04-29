package com.reyco.moscow.core.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/** 
 * @author  reyco
 * @date    2022.03.16
 * @version v1.0.1 
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {
	static ApplicationContext applicationContext;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
