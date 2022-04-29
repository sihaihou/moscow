package com.reyco.cloud.moscow.ribben;

import com.netflix.loadbalancer.Server;
import com.reyco.cloud.moscow.domain.Instance;

/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
public class MoscowServer extends Server{
	
	private Instance instance;
	/**
	 * @param host
	 * @param port
	 */
	public MoscowServer(final Instance instance) {
		super(instance.getHost(), instance.getPort());
		this.instance = instance;
	}
	public Instance getInstance() {
		return instance;
	}
	public void setInstance(Instance instance) {
		this.instance = instance;
	}
}
