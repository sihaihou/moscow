package com.reyco.cloud.moscow.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reyco.moscow.commons.constans.Constants;

/**
 * @author reyco
 * @date 2022.04.12
 * @version v1.0.1
 */
public class PushClient {

	public static final Logger logger = LoggerFactory.getLogger(PushClient.class);

	private static DatagramSocket udpSocket;
	
	private HostService hostService;
	/**
	 * @throws SocketException 
	* 
	*/
	public PushClient(HostService hostService){
		this.hostService = hostService;
		try {
			udpSocket = new DatagramSocket();
		} catch (SocketException e) {
		}
		new Thread(new Receiver(this.hostService)).start();
	}
	public int getUdpPort(){
		return udpSocket.getLocalPort();
	}
	public String getHost(){
		String hostAddress = udpSocket.getInetAddress().getHostAddress();
		return hostAddress;
	}
	public static class Receiver implements Runnable {
		private HostService hostService;
		
		public Receiver(HostService hostService) {
			super();
			this.hostService = hostService;
		}
		@Override
		public void run() {
			byte[] buffer;
			DatagramPacket inputPacket;
			while (true) {
				try {
					buffer = new byte[1024 * 4];
					inputPacket = new DatagramPacket(buffer, buffer.length);
					udpSocket.receive(inputPacket);
					String jsonData = new String(inputPacket.getData(),0,inputPacket.getLength());
					hostService.proccessServiceJson(jsonData);
					byte[] ack = "ack".getBytes();
					InetSocketAddress inetSocketAddress =(InetSocketAddress)inputPacket.getSocketAddress();
					String localHost = InetAddress.getLocalHost().getHostAddress();
					int localPort = udpSocket.getLocalPort();
					String local = getKey(localHost, localPort);
					String remoteHost = inetSocketAddress.getAddress().getHostAddress();
					int remotePort = inetSocketAddress.getPort();
					String remote = getKey(remoteHost, remotePort);
					logger.debug("【UPD客户端接收者】接收到服务端的推送消息：local:{},remote:{},data:{}",local,remote,jsonData);
					udpSocket.send(new DatagramPacket(ack, ack.length, inputPacket.getSocketAddress()));
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
}
