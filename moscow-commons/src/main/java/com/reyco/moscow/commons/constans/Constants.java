package com.reyco.moscow.commons.constans;

import java.util.concurrent.TimeUnit;

/**
 * @author reyco
 * @date 2022.03.16
 * @version v1.0.1
 */
public class Constants {
	
	public static final Double INSTANCE_WEIGHT = 1d;
	
	public static final Boolean INSTANCE_HEALTHY = true;
	
	public static final Boolean INSTANCE_ENABLED = true;
	
	public static final Boolean INSTANCE_EPHEMERAL = true;
	
	public static final String KEY_SPLITER = "@@";
	
	public static final String DOUBLE_POUND  = "##";
	
	public static final String SERVICE_INFO_SPLITER  = "@@";
	
	public static final String DEFAULT_NAMESPACE_ID = "public";
	
	public static final String DEFAULT_GROUP_ID = "DEFAULT_GROUP";
	
	public static final String DEFAULT_CLUSTER_ID = "DEFAULT";
	
	public static final long DEFAULT_HEART_BEAT_INTERVAL = TimeUnit.SECONDS.toMillis(5);
	
	public static final long DEFAULT_HEART_BEAT_TIMEOUT = TimeUnit.SECONDS.toMillis(15);

	public static final long DEFAULT_IP_DELETE_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
	
	

}
