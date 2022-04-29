package com.reyco.moscow.core.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.reyco.moscow.core.smart.SmartBill;
import com.reyco.moscow.core.smart.SmartService;
import com.reyco.moscow.core.utils.RequestUtils;

/**
 * @author reyco
 * @date 2022.04.15
 * @version v1.0.1
 */
@RestController
@RequestMapping("/v1/moscow/smart")
public class SmartController {

	@Autowired
	private SmartService smartService;

	@PostMapping("vote")
	public Object vote(HttpServletRequest request) {
		SmartBill smartBill = smartService
				.receivedVote(JSON.parseObject(RequestUtils.required(request, "vote"), SmartBill.class));
		return smartBill;
	}

	@PostMapping("beat")
	public Object beat(HttpServletRequest request) throws Exception {
		SmartBill smartBill = smartService
				.receivedBeat(JSON.parseObject(RequestUtils.required(request, "beat"), SmartBill.class));
		return smartBill;
	}

	@GetMapping("/bill")
	public Object getPeer(HttpServletRequest request, HttpServletResponse response) {
		List<SmartBill> bills = smartService.getBills();
		SmartBill local = null;
		for (SmartBill bill : bills) {
			if (StringUtils.equals(bill.host, smartService.getLocalServer())) {
				local = bill;
				break;
			}
		}
		if (local == null) {
			local = new SmartBill();
			local.host = smartService.getLocalServer();
		}
		return local;
	}

}
