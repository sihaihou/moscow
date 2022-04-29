package com.reyco.moscow.core.cluster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.reyco.moscow.commons.utils.InetUtils;
import com.reyco.moscow.core.config.RunningConfig;
import com.reyco.moscow.core.constans.Constant;
import com.reyco.moscow.core.utils.SystemUtils;

/**
 * @author reyco	
 * @date 2022.04.15
 * @version v1.0.1
 */
@Component
public class ServerListManager{
	
	public static final Logger logger = LoggerFactory.getLogger(ServerListManager.class);

	@Autowired
	private RunningConfig runningConfig;
	
	private List<Server> servers = new ArrayList<>();

	private volatile List<ServerListChangeListener> serverListChangeListeners = new ArrayList<>();

	private ScheduledExecutorService serverListUpdaterExecutorService = Executors.newScheduledThreadPool(1,
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					thread.setName("com.reyco.moscow.cluster.serverListUpdater");
					return thread;
				}
			});
	private ScheduledExecutorService serverListChangeExecutorService = Executors.newScheduledThreadPool(1,
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					thread.setName("com.reyco.moscow.cluster.serverListChange");
					return thread;
				}
			});

	public void initMethod() {
		serverListUpdaterExecutorService.scheduleWithFixedDelay(new ServerListUpdater(), 0, 5000,TimeUnit.MILLISECONDS);
	}
	public void addListen(ServerListChangeListener serverListChangeListener) {
		serverListChangeListeners.add(serverListChangeListener);
    }
	
	private class ServerListUpdater implements Runnable {
		@Override
		public void run() {
			try {
				// 获取新老服务列表
				List<Server> refreshServerList = refreshServerList();
				List<Server> oldServers = servers;
				if (CollectionUtils.isEmpty(refreshServerList)) {
					logger.warn("【更新服务】刷新serverList失败");
					return;
				}
				// 1，serverlist是否修改
				Boolean serverListChange = false;
				List<Server> newServers = (List<Server>) CollectionUtils.subtract(refreshServerList, oldServers);
				List<Server> deadServers = (List<Server>) CollectionUtils.subtract(oldServers, refreshServerList);
				if (CollectionUtils.isNotEmpty(newServers)) {
					serverListChange = true;
					servers.addAll(newServers);
					logger.debug("【更新服务】新上线的服务：" + newServers);
				}
				if (CollectionUtils.isNotEmpty(deadServers)) {
					serverListChange = true;
					servers.removeAll(deadServers);
					logger.debug("【更新服务】下线的服务：" + deadServers);
				}
				// 2通知serverListChangeListeners
				if (serverListChange) {
					serverListChangeExecutorService.execute(new Runnable() {
						@Override
						public void run() {
							for (ServerListChangeListener serverListChangeListener : serverListChangeListeners) {
								serverListChangeListener.onChangeServerList(servers);
							}
						}
					});
				}
			} catch (Exception e) {
				logger.error("【更新服务】更新服务失败：{}",e);
			}
		}

		/**
		 * 属性serverList
		 * 
		 * @return
		 */
		private List<Server> refreshServerList() {
			List<Server> servers = new ArrayList<>();
			if (SystemUtils.STANDALONE_MODE) {
				Server server = new Server();
				server.setHost(InetUtils.getSelfIp());
				server.setPort(runningConfig.getPort());
				servers.add(server);
				return servers;
			}
			List<String> serverList = new ArrayList<>();
			try {
				serverList = loadServerListForClusterConf();
				logger.debug("加载cluster.conf集群配置,serverList：" + serverList);
			} catch (Exception e) {
				logger.error("加载cluster.conf集群配置失败");
			}
			if (CollectionUtils.isEmpty(serverList)) {
				serverList = loadServerListForSystemEnvironment();
				logger.debug("加载SystemEnvironment集群配置,serverList：" + serverList);
			}
			if (CollectionUtils.isNotEmpty(serverList)) {
				String host;
				int port;
				for (String server : serverList) {
					if (server.contains(Constant.COLON)) {
						host = server.split(Constant.COLON)[0];
						port = Integer.parseInt(server.split(Constant.COLON)[1]);
					} else {
						host = server;
						port = runningConfig.getPort();
					}
					Server member = new Server(host, port);
					servers.add(member);
				}
			}
			return servers;
		}

		/**
		 * 从环境变量中加载ServerList
		 * 
		 * @return
		 */
		private List<String> loadServerListForSystemEnvironment() {
			String servers = SystemUtils.getSystemEnv("cluster.conf");
			List<String> serverList = new ArrayList<String>();
			if (StringUtils.isNotBlank(servers)) {
				if (servers.contains(Constant.COMMA)) {
					serverList = Arrays.asList(servers.split(Constant.COMMA));
					return serverList;
				}
				if (servers.contains(Constant.SEMICOLON)) {
					serverList = Arrays.asList(servers.split(Constant.SEMICOLON));
					return serverList;
				}
			}
			return null;
		}

		/**
		 * 从集群配置文件加载ServerList
		 * 
		 * @return
		 * @throws IOException
		 */
		private List<String> loadServerListForClusterConf() throws IOException {
			BufferedReader reader = null;
			List<String> servers = null;
			String server;
			try {
				reader = new BufferedReader(new FileReader(SystemUtils.CLUSTER_CONF_FILE));
				while ((server = reader.readLine()) != null) {
					if (servers == null) {
						servers = new ArrayList<>();
					}
					servers.add(server);
				}
				return servers;
			} catch (Exception e) {
				logger.error("【加载服务】读取cluster.conf集群配置文件出错：{}", e);
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
			return null;
		}
	}

}
