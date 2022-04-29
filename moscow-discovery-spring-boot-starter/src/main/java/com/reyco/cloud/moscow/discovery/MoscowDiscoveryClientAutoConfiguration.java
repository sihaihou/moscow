package com.reyco.cloud.moscow.discovery;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/** 
 * @author  reyco
 * @date    2022.04.07
 * @version v1.0.1 
 */
@Configuration
@AutoConfigureAfter(MoscowServiceDiscoveryAutoConfiguration.class)
public class MoscowDiscoveryClientAutoConfiguration {
	
	@Bean
	@ConditionalOnBean(MoscowServiceDiscovery.class)
	public DiscoveryClient moscowDiscoveryClient(MoscowServiceDiscovery serviceDiscovery) {
		return new MoscowDiscoveryClient(serviceDiscovery);
	}
	
}
