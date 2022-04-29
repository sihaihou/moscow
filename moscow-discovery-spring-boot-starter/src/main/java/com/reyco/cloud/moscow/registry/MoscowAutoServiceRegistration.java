package com.reyco.cloud.moscow.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.util.Assert;


/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
public class MoscowAutoServiceRegistration extends AbstractAutoServiceRegistration<Registration> {
	
	private static final Logger logger = LoggerFactory.getLogger(MoscowAutoServiceRegistration.class);

	private MoscowRegistration registration;
	/**
	 * @param serviceRegistry
	 */
	protected MoscowAutoServiceRegistration(ServiceRegistry<Registration> serviceRegistry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			MoscowRegistration registration) {
		super(serviceRegistry,autoServiceRegistrationProperties);
		this.registration = registration;
	}

	@Override
	protected Object getConfiguration() {
		return registration.getMoscowDiscoveryProperties();
	}

	@Override
	protected boolean isEnabled() {
		return registration.getMoscowDiscoveryProperties().isRegisterEnabled();
	}
	@Override
	protected void register() {
		super.register();
	}
	@Override
	protected Registration getRegistration() {
		if (this.registration.getPort() < 0 && this.getPort().get() > 0) {
			this.registration.setPort(this.getPort().get());
		}
		Assert.isTrue(this.registration.getPort() > 0, "service.port has not been set");
		return this.registration;
	}
	@Override
	protected void registerManagement() {
		if (!this.registration.getMoscowDiscoveryProperties().isRegisterEnabled()) {
			return;
		}
		super.registerManagement();
	}
	@Override
	protected Registration getManagementRegistration() {
		return null;
	}

}
