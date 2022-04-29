package com.reyco.moscow.core.HealthCheck;

import com.reyco.moscow.core.Cluster;

/** 
 * @author  reyco
 * @date    2022.03.17
 * @version v1.0.1 
 */
public class ClusterHealthCheckTask implements Runnable{

	private long checkRTNormalized = -1;
	private Cluster cluster;
	private long startTime;
	public ClusterHealthCheckTask(Cluster cluster) {
		this.cluster = cluster;
	}
	@Override
	public void run() {
		
	}
	public Cluster getCluster() {
		return cluster;
	}
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getCheckRTNormalized() {
		return checkRTNormalized;
	}
	public void setCheckRTNormalized(long checkRTNormalized) {
		this.checkRTNormalized = checkRTNormalized;
	}
}
