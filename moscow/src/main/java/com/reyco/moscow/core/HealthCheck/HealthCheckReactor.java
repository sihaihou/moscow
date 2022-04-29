package com.reyco.moscow.core.HealthCheck;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author reyco
 * @date 2022.03.16
 * @version v1.0.1
 */
public class HealthCheckReactor {
	
	public static final Logger logger = LoggerFactory.getLogger(HealthCheckReactor.class);
	
	private static final ScheduledExecutorService EXECUTOR;

	@SuppressWarnings("rawtypes")
	private static Map<String, ScheduledFuture> futureMap = new ConcurrentHashMap<>();
	
	static {
		int processorCount = Runtime.getRuntime().availableProcessors();
		EXECUTOR = Executors.newScheduledThreadPool(processorCount <= 1 ? 1 : processorCount / 2, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				thread.setName("com.reyco.moscow.core.health");
				return thread;
			}
		});
	}
	public static ScheduledFuture<?> scheduleCheck(ClusterHealthCheckTask clusterHealthCheckTask) {
		 clusterHealthCheckTask.setStartTime(System.currentTimeMillis());
	     return EXECUTOR.schedule(clusterHealthCheckTask, clusterHealthCheckTask.getCheckRTNormalized(), TimeUnit.MILLISECONDS);
	}
	public static void scheduleCheck(ClientBeatCheckTask task) {
		futureMap.putIfAbsent(task.taskKey(), EXECUTOR.scheduleWithFixedDelay(task, 5000, 5000, TimeUnit.MILLISECONDS));
	}
	public static void cancelCheck(ClientBeatCheckTask task) {
		@SuppressWarnings("rawtypes")
		ScheduledFuture scheduledFuture = futureMap.get(task.taskKey());
		if (scheduledFuture == null) {
			return;
		}
		try {
			scheduledFuture.cancel(true);
		} catch (Exception e) {
			logger.error("[CANCEL-CHECK] cancel failed!", e);
		}
	}

	public static ScheduledFuture<?> scheduleNow(Runnable task) {
		return EXECUTOR.schedule(task, 0, TimeUnit.MILLISECONDS);
	}
}
