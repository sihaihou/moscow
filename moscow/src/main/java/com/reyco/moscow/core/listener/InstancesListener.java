package com.reyco.moscow.core.listener;

import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.core.domain.Instances;

/**
 * @author reyco
 * @date 2022.03.23
 * @version v1.0.1
 */
public interface InstancesListener<T extends Instances> {

	void onChange(String key, T value) throws MoscowException;

	void onDelete(String key) throws MoscowException;
}
