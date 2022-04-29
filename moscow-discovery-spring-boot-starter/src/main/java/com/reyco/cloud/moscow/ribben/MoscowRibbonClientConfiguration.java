package com.reyco.cloud.moscow.ribben;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.PropertiesFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;
import com.reyco.cloud.moscow.properties.MoscowDiscoveryProperties;

/** 
 * @author  reyco
 * @date    2022.04.07
 * @version v1.0.1 
 */
@Configuration
public class MoscowRibbonClientConfiguration {
	
	@Autowired
	private PropertiesFactory propertiesFactory;
	
	@Bean
	@ConditionalOnMissingBean
	public ServerList<?> ribbonServerList(IClientConfig config,
			MoscowDiscoveryProperties discoveryProperties) {
		if (this.propertiesFactory.isSet(ServerList.class, config.getClientName())) {
			ServerList<?> serverList = this.propertiesFactory.get(ServerList.class, config,
					config.getClientName());
			return serverList;
		}
		MoscowServerList serverList = new MoscowServerList(discoveryProperties);
		serverList.initWithNiwsConfig(config);
		return serverList;
	}
}
