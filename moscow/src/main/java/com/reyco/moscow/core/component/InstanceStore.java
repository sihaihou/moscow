package com.reyco.moscow.core.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.reyco.moscow.core.domain.Instances;

/** 
 * @author  reyco
 * @date    2022.03.24
 * @version v1.0.1 
 */
@Component
public class InstanceStore {
	
	private Map<String,Instances> instancesMap = new ConcurrentHashMap<String, Instances>();
	
	public Map<String, Instances> getInstancesMap() {
		return instancesMap;
	}

	public void setInstancesMap(Map<String, Instances> instancesMap) {
		this.instancesMap = instancesMap;
	}
	public void put(String key,Instances instances) {
		instancesMap.put(key, instances);
	}
	public void remove(String key,Instances instances) {
		instancesMap.remove(key);
	}
	public Instances get(String key) {
		return instancesMap.get(key);
	}
	public Boolean contains(String key) {
		return instancesMap.containsKey(key);
	}
}
