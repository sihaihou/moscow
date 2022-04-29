package com.reyco.order.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
	private RestTemplate restTemplate;
	@GetMapping
	public Object test() throws MoscowException {
		return "192.168.1.137:8001";
	}
	@GetMapping("test1")
	public Object test1() throws MoscowException {
		String result = restTemplate.getForObject("http://orderService/test", String.class);
		return result;
	}
}
