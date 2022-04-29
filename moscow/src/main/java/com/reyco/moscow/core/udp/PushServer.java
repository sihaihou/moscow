package com.reyco.moscow.core.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.reyco.moscow.commons.ToString;
import com.reyco.moscow.commons.constans.Constants;
import com.reyco.moscow.commons.net.UdpRequest;
import com.reyco.moscow.core.Cluster;
import com.reyco.moscow.core.Service;
import com.reyco.moscow.core.domain.Instance;
import com.reyco.moscow.core.domain.ServiceInfo;
import com.reyco.moscow.core.event.ServiceChangeEvent;
import com.reyco.moscow.core.service.ServiceManager;

/**
 * @author reyco
 * @date 2022.04.11
 * @version v1.0.1
 */
@Component
public class PushServer implements ApplicationContextAware, ApplicationListener<ServiceChangeEvent> {

	public static final Logger logger = LoggerFactory.getLogger(PushServer.class);

	private static DatagramSocket udpSocket;
	/**
	 * key:host+"##"+port
	 */
	private static volatile ConcurrentMap<String, Receiver.PacketEntry> packetEntryMap = new ConcurrentHashMap<>();
	/**
	 * key1:namespace+"##"+serviceName key2:host+"##"+port
	 */
	private static ConcurrentMap<String, ConcurrentMap<String, Clienter>> clienterMapMap = new ConcurrentHashMap<>();

	private static Map<String, Future> futureMap = new ConcurrentHashMap<>();

	@Autowired
	private ServiceManager serviceManager;

	private ApplicationContext applicationContext;

	private static ScheduledExecutorService executorService = Executors
			.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setDaemon(true);
					t.setName("com.reyco.moscow.udp.updRemover");
					return t;
				}
			});
	private static ScheduledExecutorService udpExecutorService = Executors
			.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				t.setName("com.reyco.moscow.udp.udpSender");
				return t;
			}
	});
	static {
		try {
			udpSocket = new DatagramSocket();
			// 接收客户端的响应
			new Thread(new Receiver(), "com.reyco.moscow.udp.receiver").start();
			// 移除不可用的客户端
			executorService.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					removeClientIfNecessary();
				}
			}, 0, 15, TimeUnit.SECONDS);

		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void serviceChanged(Service service) {
		if (futureMap.containsKey(service.getNamespaceId() + Constants.DOUBLE_POUND + service.getName())) {
			return;
		}
		this.applicationContext.publishEvent(new ServiceChangeEvent(this, service));
	}

	public static void removeClientIfNecessary() {
		for (Map.Entry<String, ConcurrentMap<String, Clienter>> clienterMapMapEntry : clienterMapMap.entrySet()) {
			ConcurrentMap<String, Clienter> clienterMap = clienterMapMapEntry.getValue();
			for (Map.Entry<String, Clienter> clienterEntry : clienterMap.entrySet()) {
				Clienter clienter = clienterEntry.getValue();
				if (System.currentTimeMillis() - clienter.getLastSendTime() > 30000) {
					logger.debug("【upd异常客户端移除通知】host：{},port:{}",clienter.getSocketAddr().getAddress().getHostAddress(),clienter.getSocketAddr().getPort());
					clienterMap.remove(getKey(clienter.getSocketAddr().getAddress().getHostAddress(),
							clienter.getSocketAddr().getPort()));
				}
			}
		}
	}

	@Override
	public void onApplicationEvent(ServiceChangeEvent serviceChangeEvent) {
		Service service = serviceChangeEvent.getService();
		String serviceName = service.getName();
		String namespaceId = service.getNamespaceId();
		futureMap.put(getFutureKey(serviceName, namespaceId), udpExecutorService.schedule(new Runnable() {
			@Override
			public void run() {
				ConcurrentMap<String, Clienter> clienterMap = clienterMapMap
						.get(getFutureKey(namespaceId, serviceName));
				if (CollectionUtils.isEmpty(clienterMap)) {
					return;
				}
				for (Clienter clienter : clienterMap.values()) {
					if (System.currentTimeMillis() - clienter.lastSendTime > 30) {
						clienterMap.remove(getKey(clienter.getSocketAddr().getAddress().getHostAddress(),
								clienter.getSocketAddr().getPort()));
					}
					Receiver.PacketEntry packetEntry = buildPacketEntry(clienter);
					send(packetEntry);
				}
			}

		}, 1000, TimeUnit.MILLISECONDS));
	}

	private Receiver.PacketEntry buildPacketEntry(Clienter clienter) {
		Service service = serviceManager.getService(clienter.getNamespace(), clienter.getServiceName());
		Map<String, Cluster> clusterMap = service.getClusterMap();
		Cluster cluster = clusterMap.get(clienter.getClusterName());
		Set<Instance> ephemeralInstances = cluster.getEphemeralInstances();
		ephemeralInstances.addAll(cluster.getPersistentInstances());
		List<Instance> instances = new ArrayList<>(ephemeralInstances);
		ServiceInfo serviceInfo = new ServiceInfo(clienter.getServiceName(), clienter.getClusterName());
		serviceInfo.setInstances(instances);
		String jsonData = JSON.toJSONString(serviceInfo);
		byte[] dataBytes = jsonData.getBytes(StandardCharsets.UTF_8);
		DatagramPacket outputPacket = new DatagramPacket(dataBytes, dataBytes.length, clienter.getSocketAddr());
		Receiver.PacketEntry packetEntry = new Receiver.PacketEntry(
				getKey(clienter.getSocketAddr().getAddress().getHostAddress(), clienter.getSocketAddr().getPort()),
				outputPacket);
		return packetEntry;
	}

	private void send(Receiver.PacketEntry packetEntry) {
		try {
			if (packetEntry == null) {
				return;
			}
			packetEntryMap.put(packetEntry.getKey(), packetEntry);
			byte[] dataBuffer = packetEntry.getOutputPacket().getData();
			String data = new String(dataBuffer, 0, dataBuffer.length);
			String localHost = InetAddress.getLocalHost().getHostAddress();
			int localPort = udpSocket.getLocalPort();
			String local = getKey(localHost, localPort);
			logger.debug("【UPD服务端推送消息通知】发送消息：local:{},remote:{},data:{}", local, packetEntry.getKey(), data);
			udpSocket.send(packetEntry.getOutputPacket());
		} catch (IOException e) {
			packetEntryMap.remove(packetEntry.getKey());
		}

	}

	public void addClient(String namespace, String serviceName, String clusterName, InetSocketAddress socketAddr) {
		Clienter clienter = new Clienter();
		clienter.setNamespace(namespace);
		clienter.setServiceName(serviceName);
		clienter.setClusterName(clusterName);
		clienter.setSocketAddr(socketAddr);
		clienter.setLastSendTime(System.currentTimeMillis());
		addClienter(clienter);
	}

	private void addClienter(Clienter clienter) {
		String serviceKey = getFutureKey(clienter.getNamespace(), clienter.getServiceName());
		ConcurrentMap<String, Clienter> clienterMap = clienterMapMap.get(serviceKey);
		if (clienterMap == null) {
			clienterMap = new ConcurrentHashMap<>(1024);
			clienterMapMap.put(serviceKey, clienterMap);
		}
		String hostKey = getKey(clienter.getSocketAddr().getAddress().getHostAddress(),
				clienter.getSocketAddr().getPort());
		Clienter oldClienter = clienterMap.get(hostKey);
		if (oldClienter != null) {
			oldClienter.refresh();
		} else {
			logger.debug("【upd添加客户端通知】host：{},port:{}",clienter.getSocketAddr().getAddress().getHostAddress(),clienter.getSocketAddr().getPort());
			clienterMap.put(hostKey, clienter);
		}
	}

	/**
	 * 客户端
	 * 
	 * @author reyco
	 * @date 2022.04.11
	 * @version v1.0.1
	 */
	public static class Clienter extends ToString{
		private String namespace;
		private String clusterName;
		private String serviceName;
		private InetSocketAddress socketAddr;
		private volatile Long lastSendTime;

		public String getNamespace() {
			return namespace;
		}

		public void setNamespace(String namespace) {
			this.namespace = namespace;
		}

		public String getClusterName() {
			return clusterName;
		}

		public void setClusterName(String clusterName) {
			this.clusterName = clusterName;
		}

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public InetSocketAddress getSocketAddr() {
			return socketAddr;
		}

		public void setSocketAddr(InetSocketAddress socketAddr) {
			this.socketAddr = socketAddr;
		}

		public Long getLastSendTime() {
			return lastSendTime;
		}

		public void setLastSendTime(Long lastSendTime) {
			this.lastSendTime = lastSendTime;
		}
		public void refresh() {
			this.lastSendTime = System.currentTimeMillis();
		}
	}

	/**
	 * 接收者
	 * 
	 * @author reyco
	 * @date 2022.04.11
	 * @version v1.0.1
	 */
	public static class Receiver implements Runnable {
		@Override
		public void run() {
			byte[] buffer;
			DatagramPacket inputPacket;
			InetSocketAddress socketAddress;
			while (true) {
				buffer = new byte[1024 * 4];
				inputPacket = new DatagramPacket(buffer, buffer.length);
				try {
					udpSocket.receive(inputPacket);
					socketAddress = (InetSocketAddress) inputPacket.getSocketAddress();
					String key = getKey(socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
					byte[] dataBuffer = inputPacket.getData();
					String data = new String(dataBuffer, 0, dataBuffer.length);
					UdpRequest request = JSON.parseObject(data,UdpRequest.class);
					String localHost = InetAddress.getLocalHost().getHostAddress();
					int localPort = udpSocket.getLocalPort();
					String local = getKey(localHost, localPort);
					logger.debug("【UDP服务端接收消息通知】接收到客户端的响应,local:{},remote:{},data:{}", local, key, data);
					packetEntryMap.remove(key);
				} catch (IOException e) {
					logger.error("udp接收消息出错", e);
				}
			}
		}

		/**
		 * 
		 * @author reyco
		 * @date 2022.04.11
		 * @version v1.0.1
		 */
		public static class PacketEntry {
			private String key;
			private DatagramPacket outputPacket;
			private Map<String, Object> data;

			public PacketEntry(String key, DatagramPacket outputPacket) {
				this.key = key;
				this.outputPacket = outputPacket;
			}

			public String getKey() {
				return key;
			}

			public void setKey(String key) {
				this.key = key;
			}

			public DatagramPacket getOutputPacket() {
				return outputPacket;
			}

			public void setOutputPacket(DatagramPacket outputPacket) {
				this.outputPacket = outputPacket;
			}

			public Map<String, Object> getData() {
				return data;
			}

			public void setData(Map<String, Object> data) {
				this.data = data;
			}
		}
	}

	public static String getKey(String host, int port) {
		return host + Constants.DOUBLE_POUND + port;
	}

	public static String getFutureKey(String namespaceId, String serviceName) {
		return namespaceId + Constants.DOUBLE_POUND + serviceName;
	}
}
