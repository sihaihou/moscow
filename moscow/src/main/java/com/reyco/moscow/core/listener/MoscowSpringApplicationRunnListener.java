package com.reyco.moscow.core.listener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.reyco.moscow.commons.utils.InetUtils;

/** 
 * @author  reyco
 * @date    2022.04.08
 * @version v1.0.1 
 */
public class MoscowSpringApplicationRunnListener implements SpringApplicationRunListener{
	/**
	 * 
	 */
	public MoscowSpringApplicationRunnListener(SpringApplication springApplication,String[] args) {
		
	}
	@Override
	public void starting() {
		
	}

	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {
		if(StringUtils.isBlank(environment.getProperty("moscow.local.ip"))) {
			System.setProperty("moscow.local.ip", InetUtils.getSelfIp());
		}
		if(StringUtils.isBlank(environment.getProperty("server.port"))) {
			System.setProperty("server.port", "8999");
		}
		if(StringUtils.isBlank(environment.getProperty("application.version"))) {
			System.setProperty("moscow.mode", "stand-alone");
		}
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
		
	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
		
	}

	@Override
	public void started(ConfigurableApplicationContext context) {
		
	}

	@Override
	public void running(ConfigurableApplicationContext context) {
	}

	@Override
	public void failed(ConfigurableApplicationContext context, Throwable exception) {
		
	}

}
