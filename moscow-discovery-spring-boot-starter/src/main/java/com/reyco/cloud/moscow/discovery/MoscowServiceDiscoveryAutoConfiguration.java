package com.reyco.cloud.moscow.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
public class MoscowServiceDiscoveryAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public MoscowServiceDiscovery moscowServiceDiscovery(
			MoscowDiscoveryProperties discoveryProperties) {
		return new MoscowServiceDiscovery(discoveryProperties);
	}

}
