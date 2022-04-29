package com.reyco.cloud.moscow.registry;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.reyco.cloud.moscow.properties.MoscowDiscoveryProperties;


/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
@Configuration
@EnableConfigurationProperties(MoscowDiscoveryProperties.class)
@AutoConfigureAfter({ AutoServiceRegistrationConfiguration.class,
	AutoServiceRegistrationAutoConfiguration.class,MoscowDiscoveryProperties.class})
public class MoscowServiceRegistryAutoConfiguration {
	
	@Bean
	public MoscowServiceRegistry moscowServiceRegistry(MoscowDiscoveryProperties discoveryProperties) {
		return new MoscowServiceRegistry(discoveryProperties);
	}

	@Bean
	@ConditionalOnBean(AutoServiceRegistrationProperties.class)
	public MoscowRegistration moscowRegistration(MoscowDiscoveryProperties discoveryProperties,ApplicationContext context) {
		return new MoscowRegistration(discoveryProperties, context);
	}

	@Bean
	@ConditionalOnBean(AutoServiceRegistrationProperties.class)
	public MoscowAutoServiceRegistration moscowAutoServiceRegistration(
			MoscowServiceRegistry registry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			MoscowRegistration registration) {
		return new MoscowAutoServiceRegistration(registry,autoServiceRegistrationProperties, registration);
	}
}
