package com.reyco.cloud.moscow.service;

import com.reyco.moscow.commons.domain.BeatInfo;
import com.reyco.moscow.commons.execption.MoscowException;

/**
 * @author reyco
 * @date 2022.04.06
 * @version v1.0.1
 */
public interface BeatService {

	void addBeatInfo(String serviceName, BeatInfo beatInfo) throws MoscowException;

	void removeBeatInfo(BeatInfo beatInfo) throws MoscowException;
}
