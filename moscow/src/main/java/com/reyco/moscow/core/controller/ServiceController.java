package com.reyco.moscow.core.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reyco.moscow.commons.constans.Constants;
import com.reyco.moscow.commons.utils.R;
import com.reyco.moscow.core.service.ServiceManager;
import com.reyco.moscow.core.utils.RequestUtils;

/** 
 * @author  reyco
 * @date    2022.04.07
 * @version v1.0.1 
 */
@RestController
@RequestMapping("/v1/moscow/service")
public class ServiceController {
	public static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
	@Autowired
	private ServiceManager serviceManager;
	 
	@GetMapping("list")
	public Object list(HttpServletRequest request){
		String groupName = RequestUtils.required(request, "groupName");
		String namespaceId = RequestUtils.optional(request, "namespaceId",Constants.DEFAULT_NAMESPACE_ID);
		logger.debug("【获取服务名通知】namespaceId:{},groupName{}",namespaceId,groupName);
		List<String> serviceNameList = serviceManager.getAllServiceNameList(namespaceId);
		serviceNameList.stream().filter(serviceName->!serviceName.startsWith(groupName+Constants.SERVICE_INFO_SPLITER));
		for (int i=0;i<serviceNameList.size();i++) {
			serviceNameList.set(i, serviceNameList.get(i).replace(groupName+Constants.SERVICE_INFO_SPLITER, ""));
		}
		return R.success(serviceNameList);
	}
}
