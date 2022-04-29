package com.reyco.cloud.moscow.utils;

import com.reyco.moscow.commons.constans.Constants;

/** 
 * @author  reyco
 * @date    2022.04.07
 * @version v1.0.1 
 */
public class MoscowUtils {
	/**
	 * 
	 * @param groupName
	 * @param serviceName
	 * @return
	 */
	public static String getServiceName(String groupName,String serviceName) {
		StringBuilder sb = new StringBuilder();
		sb.append(groupName)
		.append(Constants.SERVICE_INFO_SPLITER)
		.append(serviceName);
		return sb.toString().intern();
	}
}
