package com.reyco.moscow.core.service.impl;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.core.component.InstanceStore;
import com.reyco.moscow.core.constans.ActionType;
import com.reyco.moscow.core.domain.Instances;
import com.reyco.moscow.core.listener.InstancesListener;
import com.reyco.moscow.core.service.EphemeralServerRegisterService;

/** 
 * @author  reyco
 * @date    2022.03.15
 * @version v1.0.1 
 */
@Service
public class EphemeralServerRegisterServiceImpl implements EphemeralServerRegisterService{
	public static final Logger logger = LoggerFactory.getLogger(EphemeralServerRegisterServiceImpl.class);
	private ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("com.reyco.moscow.instanceser");
            return t;
        }
    });
	@Autowired
	private InstanceStore instanceStore;
	
	public volatile Instanceser instanceser = new Instanceser();
	
	private Map<String, CopyOnWriteArrayList<InstancesListener<Instances>>> listeners = new ConcurrentHashMap<>();
	
	@PostConstruct
	public void init() {
		executor.submit(instanceser);
	}
	
	@Override
	public void register(String key, Instances instances) throws MoscowException {
		onPut(key,instances);
		doDispatcher();
	}
	@Override
	public void remove(String key) throws MoscowException {
		listeners.remove(key);
	}
	@Override
	public void listen(String key, InstancesListener<Instances> listener) throws MoscowException {
		if(!listeners.containsKey(key)) {
			listeners.put(key, new CopyOnWriteArrayList<>());
		}
		if(listeners.get(key).contains(listener)) {
			return;
		}
		listeners.get(key).add(listener);
	}
	@Override
	public void unlisten(String key, InstancesListener<Instances> listener) throws MoscowException {
		if(!listeners.containsKey(key)) {
			return;
		}
		for (InstancesListener<Instances> instancesListener : listeners.get(key)) {
			if(instancesListener.equals(listener)) {
				listeners.get(key).remove(listener);
			}
		}
	}
	/**
	 * @param key
	 * @param instances
	 */
	private void onPut(String key, Instances instances) {
		instanceStore.put(key, instances);
		instanceser.addTask(key, ActionType.CHANGE);
	}
	/**
	 * 
	 */
	private void doDispatcher() {
		//分发到其它节点
		logger.debug("分发到其它节点");
	}

	class Instanceser implements Runnable{
		
		private ConcurrentHashMap<String, String> services = new ConcurrentHashMap<>();
		
		private BlockingQueue<Pair> tasks = new LinkedBlockingQueue<Pair>();
		
		private void addTask(String key,ActionType action) {
			if(!services.containsKey(key) && action.equals(ActionType.CHANGE)) {
				services.put(key, "");
			}
			tasks.add(new Pair(key,action));
		}
		@Override
		public void run() {
			try {
				while(true) {
					Pair pair = tasks.take();
					String key = pair.getKey();
					services.remove(key);
					for (InstancesListener<Instances> listener : listeners.get(key)) {
						if(pair.getAction().equals(ActionType.CHANGE)) {
							listener.onChange(key, instanceStore.get(key));
							continue;
						}
						if(pair.getAction().equals(ActionType.DELETE)) {
							listener.onDelete(key);
							continue;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		class Pair{
			private String key;
			private ActionType action;
			public Pair() {
			}
			public Pair(String key, ActionType action) {
				this.key = key;
				this.action = action;
			}
			public String getKey() {
				return key;
			}
			public void setKey(String key) {
				this.key = key;
			}
			public ActionType getAction() {
				return action;
			}
			public void setAction(ActionType action) {
				this.action = action;
			}
		}
	}

}
