package com.reyco.moscow.core.event;

import org.springframework.context.ApplicationEvent;

import com.reyco.moscow.core.Service;

/**
 * @author reyco
 * @date 2022.03.25
 * @version v1.0.1
 */
public class ServiceChangeEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3638760650032979039L;
	private Service service;

	public ServiceChangeEvent(Object source, Service service) {
		super(source);
		this.service = service;
	}

	public Service getService() {
		return service;
	}
}
