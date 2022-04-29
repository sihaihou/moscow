package com.reyco.order.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.reyco.cloud.moscow.discovery.MoscowServiceDiscovery;
import com.reyco.moscow.commons.execption.MoscowException;

/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
@RestController
@RequestMapping("test")
public class TestController {
	
	@Autowired
	private MoscowServiceDiscovery moscowServiceDiscovery;
	
	@Autowired
	private DiscoveryClient discoveryClient;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@GetMapping
	public Object test() throws MoscowException {
		/*List<String> services = moscowServiceDiscovery.getServices();
		System.out.println("services:"+services);
		List<String> services2 = discoveryClient.getServices();
		System.out.println("services2:"+services2);*/
		String result = restTemplate.getForObject("http://orderProvider/test", String.class);
		return result;
	}
}
