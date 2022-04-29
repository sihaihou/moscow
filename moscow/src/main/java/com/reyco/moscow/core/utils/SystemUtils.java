package com.reyco.moscow.core.utils;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

/** 
 * @author  reyco
 * @date    2022.04.15
 * @version v1.0.1 
 */
public class SystemUtils {
	//-Dmoscow.standalone=true -Dmoscow.home=D:\application\workspace\moscow\data -Dserver.port=8999
	//moscow启动模型键
	public static final String STANDALONE_MODE_PROPERTY_NAME = "moscow.standalone";
	//moscow启动模型键--单机
	public static final String STANDALONE_MODE_ALONE = "standalone";
	//moscow启动模型键--集群
	public static final String STANDALONE_MODE_CLUSTER = "cluster";
	
	
	public static final String MOSCOW_HOME_KEY = "moscow.home";
	
	public static final String CLUSTER_CONF = "cluster.conf";

	public static final String CONFIG_BAK_DIR = System.getProperty("user.home", "/home/admin") + "/moscow/bak_data";

	
	public static final String MOSCOW_HOME = getMoscowHome();
	
	public final static String CLUSTER_CONF_FILE = MOSCOW_HOME + File.separator + "conf" + File.separator + "cluster.conf";
	
	public static final boolean STANDALONE_MODE = Boolean.getBoolean(STANDALONE_MODE_PROPERTY_NAME);
	
	public static String getSystemEnv(String key) {
		return System.getenv(key);
	}

	public static String getMoscowHome() {
		String moscowHome = System.getProperty(MOSCOW_HOME_KEY);
		if (StringUtils.isBlank(moscowHome)) {
			moscowHome = System.getProperty("user.home") + File.separator + "moscow";
		}
		return moscowHome;
	}
}
