package com.reyco.moscow.core.cluster;

import java.util.List;

/** 
 * @author  reyco
 * @date    2022.04.15
 * @version v1.0.1 
 */
public interface ServerListChangeListener {
	
	void onChangeServerList(List<Server> servers);
	
}
