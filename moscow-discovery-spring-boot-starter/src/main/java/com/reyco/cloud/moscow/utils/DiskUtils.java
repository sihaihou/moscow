package com.reyco.cloud.moscow.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.reyco.cloud.moscow.domain.ServiceInfo;

/** 
 * @author  reyco
 * @date    2022.04.07
 * @version v1.0.1 
 */
public class DiskUtils {
	private static final Logger logger = LoggerFactory.getLogger(DiskUtils.class);
	public static Map<String,ServiceInfo> read(String dir){
		Map<String,ServiceInfo> serviceInfoMap = new HashMap<>();
		try {
			File fileDir = new File(dir);
			if(!fileDir.exists()) {
				fileDir.mkdirs();
			}
			File[] files = fileDir.listFiles();
			for (File file : files) {
				if(!file.isFile()) {
					continue;
				}
				String fileName = URLDecoder.decode(file.getName(), "UTF-8");
				StringBuilder sb = new StringBuilder();
				if(fileName.endsWith(".meta")) {
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new FileReader(file));
						String content;
						while ((content = reader.readLine()) != null) {
							sb.append(content);
						}
						String serviceInfoMapJson = sb.toString().intern();
						ServiceInfo serviceInfo = JSON.parseObject(serviceInfoMapJson, ServiceInfo.class);
						serviceInfoMap.put(fileName.replace(".meta", ""), serviceInfo);
					} catch (Exception e) {
						//e.printStackTrace();
						logger.error("【读取失败】 fileName："+fileName);
					}finally {
						if(reader!=null) {
							reader.close();
						}
					}
				}
			}
			
		} catch (Exception e) {
		}
		return serviceInfoMap;
	}
	public static Map<String,ServiceInfo> write(ServiceInfo serviceInfo,String dir){
		try {
			File fileDir = new File(dir);
			if(!fileDir.exists()) {
				fileDir.mkdirs();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
}
