package com.reyco.moscow.core.config;

import org.springframework.beans.BeansException;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.reyco.moscow.core.cluster.ServerListManager;

/**
 * @author reyco
 * @date 2022.04.15
 * @version v1.0.1
 */
@Component
public class RunningConfig implements ApplicationListener<ServletWebServerInitializedEvent>, ApplicationContextAware {

	private int port;

	private String contextPath;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * @return the applicationContext
	 */
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void onApplicationEvent(ServletWebServerInitializedEvent event) {
		port = event.getWebServer().getPort();
		contextPath = event.getApplicationContext().getServletContext().getContextPath();
		applicationContext.getBean(ServerListManager.class).initMethod();
	}

	public int getPort() {
		return port;
	}

	public String getContextPath() {
		return contextPath;
	}

	/**
	 * @return the environment
	 */
	public String getProperty(String key) {
		return applicationContext.getEnvironment().getProperty(key);
	}
}
