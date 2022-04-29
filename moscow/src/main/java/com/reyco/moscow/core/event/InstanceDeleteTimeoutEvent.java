package com.reyco.moscow.core.event;

import org.springframework.context.ApplicationEvent;

import com.reyco.moscow.core.domain.Instance;

/** 
 * @author  reyco
 * @date    2022.03.16
 * @version v1.0.1 
 */
public class InstanceDeleteTimeoutEvent extends ApplicationEvent{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6908030084750646123L;
	private Instance instance;
	/**
	 * @param source
	 */
	public InstanceDeleteTimeoutEvent(Object source,Instance instance) {
		super(source);
		this.instance = instance;
	}
	public Instance getInstance() {
		return instance;
	}
	public void setInstance(Instance instance) {
		this.instance = instance;
	}
}
