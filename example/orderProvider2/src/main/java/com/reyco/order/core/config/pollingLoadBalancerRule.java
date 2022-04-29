package com.reyco.order.core.config;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

/** 
 * 轮询负载均衡
 * @author  reyco
 * @date    2021.12.14
 * @version v1.0.1 
 */
@Component
public class pollingLoadBalancerRule extends AbstractLoadBalancerRule{
	
	private static Logger logger = LoggerFactory.getLogger(pollingLoadBalancerRule.class);
	private LongAdder longAdder = new LongAdder();
	@Override
	public Server choose(Object key) {
		ILoadBalancer lb = this.getLoadBalancer();
		List<Server> servers = lb.getAllServers();
		int intValue = longAdder.intValue();
		int index = intValue%servers.size();
		Server server = servers.get(index);
		logger.info("server-port:"+server.getHostPort());
		longAdder.increment();
		return server;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig) {
		
	}
	

}
