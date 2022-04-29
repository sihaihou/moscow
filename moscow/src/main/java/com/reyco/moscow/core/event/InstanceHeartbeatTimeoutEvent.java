package com.reyco.moscow.core.event;

import org.springframework.context.ApplicationEvent;

import com.reyco.moscow.core.domain.Instance;

/** 
 * @author  reyco
 * @date    2022.03.16
 * @version v1.0.1 
 */
public class InstanceHeartbeatTimeoutEvent extends ApplicationEvent{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2472251776068754748L;
	private Instance instance;
	/**
	 * @param source
	 */
	public InstanceHeartbeatTimeoutEvent(Object source,Instance instance) {
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
