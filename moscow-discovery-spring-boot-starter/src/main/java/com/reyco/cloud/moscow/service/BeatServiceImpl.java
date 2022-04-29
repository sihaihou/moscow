package com.reyco.cloud.moscow.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.reyco.cloud.moscow.domain.Instance;
import com.reyco.cloud.moscow.net.MoscowServiceProxy;
import com.reyco.moscow.commons.domain.BeatInfo;
import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.commons.utils.JsonUtils;

/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
public class BeatServiceImpl implements BeatService{
	private static final Logger logger = LoggerFactory.getLogger(BeatServiceImpl.class);
	
	private ScheduledExecutorService executorService;
	
	private MoscowServiceProxy moscowServiceProxy;
	
	public final Map<String, BeatInfo> beatMap = new ConcurrentHashMap<String, BeatInfo>();
	
	public BeatServiceImpl(MoscowServiceProxy moscowServiceProxy, int threadCount) {
        this.moscowServiceProxy = moscowServiceProxy;

        executorService = new ScheduledThreadPoolExecutor(threadCount, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("com.reyco.moscow.beat.sender");
                return thread;
            }
        });
    }
	@Override
	public void addBeatInfo(String serviceName, BeatInfo beatInfo) throws MoscowException {
		String key = getKey(beatInfo);
		BeatInfo existBeat = null;
		if((existBeat=beatMap.remove(key))!=null) {
			existBeat.setStopped(true);
		}
		beatMap.put(key, beatInfo);
		executorService.schedule(new BeatTask(beatInfo), beatInfo.getPeriod(), TimeUnit.MILLISECONDS);
	}
	@Override
	public void removeBeatInfo(BeatInfo beatInfo) throws MoscowException {
		String key = getKey(beatInfo);
		BeatInfo existBeat = null;
		if((existBeat=beatMap.remove(key))!=null) {
			existBeat.setStopped(true);
			return;
		}
	}
	private String getKey(BeatInfo beatInfo){
		return beatInfo.getServiceName()+"@"+beatInfo.getHost()+"@"+beatInfo.getPort();
	}
	class BeatTask implements Runnable{
		private BeatInfo beatInfo;
		public BeatTask(BeatInfo beatInfo) {
			super();
			this.beatInfo = beatInfo;
		}
		@Override
		public void run() {
			if (beatInfo.isStopped()) {
                return;
            }
            long nextTime = beatInfo.getPeriod();
            try {
                String resultJson = moscowServiceProxy.sendBeat(beatInfo);
                if(StringUtils.isNotBlank(resultJson)) {
                	JSONObject result = JSON.parseObject(resultJson);
                	long interval = result.getIntValue("clientBeatInterval");
                	if (interval > 0) {
                		nextTime = interval;
                	}
                	int code = 200;
                	if (result.containsKey("code")) {
                		code = result.getIntValue("code");
                	}
                	if(code!=200) {
                		Instance instance = new Instance();
                		instance.setPort(beatInfo.getPort());
                		instance.setHost(beatInfo.getHost());
                		instance.setWeight(beatInfo.getWeight());
                		instance.setClusterName(beatInfo.getClusterName());
                		instance.setServiceName(beatInfo.getServiceName());
                		instance.setInstanceId(instance.getInstanceId());
                		instance.setEphemeral(true);
                		try {
                			moscowServiceProxy.registerService(beatInfo.getServiceName(),beatInfo.getGroupName(), instance);
                		} catch (Exception ignore) {
                		}
                	}
                }
            } catch (MoscowException ne) {
                logger.error("【客户端心跳】 发送心跳失败: {}, code: {}, msg: {}",JsonUtils.objToJson(beatInfo), ne.getCode(), ne.getMsg());
            }
            executorService.schedule(new BeatTask(beatInfo), nextTime, TimeUnit.MILLISECONDS);
		}
	}
}
