package com.reyco.moscow.core.domain;

import java.util.List;

import java.io.Serializable;

/** 
 * @author  reyco
 * @date    2022.03.23
 * @version v1.0.1 
 */
public class Instances implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1913483691569182772L;
	private List<Instance> instanceList;

	public List<Instance> getInstanceList() {
		return instanceList;
	}

	public void setInstanceList(List<Instance> instanceList) {
		this.instanceList = instanceList;
	}

	@Override
	public String toString() {
		return "Instances [instanceList=" + instanceList + "]";
	}
}
