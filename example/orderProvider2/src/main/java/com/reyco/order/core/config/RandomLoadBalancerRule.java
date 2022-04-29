package com.reyco.order.core.config;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

/** 
 * 随机负载均衡
 * @author  reyco
 * @date    2021.12.14
 * @version v1.0.1 
 */
//@Component
public class RandomLoadBalancerRule extends AbstractLoadBalancerRule{
	
	private static Logger logger = LoggerFactory.getLogger(RandomLoadBalancerRule.class);
	@Override
	public Server choose(Object key) {
		ILoadBalancer lb = this.getLoadBalancer();
		List<Server> servers = lb.getAllServers();
		int index = ThreadLocalRandom.current().nextInt(servers.size());
		Server server = servers.get(index);
		logger.info("server-port:"+server.getHostPort());
		return server;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig) {
		
	}
	

}
