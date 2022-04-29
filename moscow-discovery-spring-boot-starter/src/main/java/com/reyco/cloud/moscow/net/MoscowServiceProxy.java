package com.reyco.cloud.moscow.net;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.reyco.cloud.moscow.domain.Instance;
import com.reyco.moscow.commons.domain.BeatInfo;
import com.reyco.moscow.commons.execption.MoscowException;
import com.reyco.moscow.commons.net.HttpClient;
import com.reyco.moscow.commons.net.HttpMethod;

/**
 * @author reyco
 * @date 2022.04.02
 * @version v1.0.1
 */
public class MoscowServiceProxy {

	private static final Logger logger = LoggerFactory.getLogger(MoscowServiceProxy.class);

	public final static String VERSION = "v1.0.1";

	private int serverPort = 8999;

	private String namespaceId;

	private String nacosDomain;

	private List<String> serverList;

	private List<String> serversFromEndpoint = new ArrayList<String>();

	public MoscowServiceProxy(String namespaceId, String serverList) {
		super();
		this.namespaceId = namespaceId;
		if (StringUtils.isNotBlank(serverList)) {
			this.serverList = Arrays.asList(serverList.split(","));
			if (this.serverList.size() == 1) {
				this.nacosDomain = serverList;
			}
		}
	}

	public void registerService(String serviceName, String groupName, Instance instance) throws MoscowException {
		logger.debug("【注册服务】namespaceId:{},serviceName:{},instance:{}", namespaceId, serviceName, instance);
		final Map<String, String> params = new HashMap<String, String>(9);
		params.put("namespaceId", namespaceId);
		params.put("serviceName", serviceName);
		params.put("groupName", groupName);
		params.put("clusterName", instance.getClusterName());
		params.put("host", instance.getHost());
		params.put("port", String.valueOf(instance.getPort()));
		params.put("weight", String.valueOf(instance.getWeight()));
		params.put("enable", String.valueOf(instance.isEnabled()));
		params.put("healthy", String.valueOf(instance.isHealthy()));
		params.put("ephemeral", String.valueOf(instance.isEphemeral()));
		sendApi("/moscow/v1/moscow/instance", params, "", getServerList(), HttpMethod.POST);
	}

	public String sendBeat(BeatInfo beatInfo) throws MoscowException {
		logger.debug("【发送心跳】namespaceId:{},serviceName:{},beatInfo:{}", namespaceId, beatInfo.getServiceName(),beatInfo);
		Map<String, String> params = new HashMap<String, String>(8);
		String body = StringUtils.EMPTY;
		params.put("namespaceId", namespaceId);
		params.put("serviceName", beatInfo.getServiceName());
		params.put("groupName", beatInfo.getGroupName());
		params.put("clusterName", beatInfo.getClusterName());
		params.put("host", beatInfo.getHost());
		params.put("port", String.valueOf(beatInfo.getPort()));
		try {
			body = "beat=" + URLEncoder.encode(JSON.toJSONString(beatInfo), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new MoscowException(500, "encode beatInfo error" + e);
		}
		return sendApi("/moscow/v1/moscow/instance/beat", params, body, getServerList(), HttpMethod.PUT);
	}

	public List<String> getServiceList(String groupName) throws MoscowException {
		Map<String, String> params = new HashMap<String, String>(4);
		params.put("namespaceId", namespaceId);
		params.put("groupName", groupName);
		String result = sendApi("/moscow/v1/moscow/service/list", params, "", getServerList(), HttpMethod.GET);

		JSONObject resultJson = JSON.parseObject(result);
		JSONObject dataJson = resultJson.getJSONObject("data");
		JSONArray jsonArray = dataJson.getJSONArray("info");
		List<String> list = new ArrayList<String>();
		for (Object obj : jsonArray) {
			list.add((String) obj);
		}
		return list;
	}

	public String queryList(String serviceName, String clusterName,String host,int udpPort, boolean healthyOnly) throws MoscowException {
		Map<String, String> params = new HashMap<String, String>(8);
		params.put("namespaceId", namespaceId);
		params.put("serviceName", serviceName);
		params.put("clusterName", clusterName);
		params.put("host", host);
		params.put("udpPort", String.valueOf(udpPort));
		params.put("healthy", String.valueOf(healthyOnly));
		return sendApi("/moscow/v1/moscow/instance/list", params,"",getServerList(), HttpMethod.GET);
	}

	private String sendApi(String api, Map<String, String> params, String body, List<String> servers, String method)
			throws MoscowException {
		if (CollectionUtils.isEmpty(servers)) {
			throw new MoscowException(500, "no server available");
		}
		Random random = new Random(System.currentTimeMillis());
		int index = random.nextInt(servers.size());
		for (int i = 0; i < servers.size(); i++) {
			String server = servers.get(index);
			try {
				return callServer(api, params, body, server, method);
			} catch (MoscowException e) {
				logger.debug("request {} failed.", server, e);
				e.printStackTrace();
			}
			index = (index + 1) % servers.size();
		}
		return "";
	}

	public String callServer(String api, Map<String, String> params, String body, String curServer, String method)
			throws MoscowException {
		long start = System.currentTimeMillis();
		List<String> headers = builderHeaders();
		String url = "";
		if (curServer.startsWith(HttpClient.HTTPS) || curServer.startsWith(HttpClient.HTTP)) {
			url = curServer + api;
		} else {
			if (!curServer.contains(":")) {
				curServer = curServer + ":" + serverPort;
			}
			url = HttpClient.getPrefix() + curServer + api;
		}
		HttpClient.HttpResult result = HttpClient.request(url, headers, params, body, method, HttpClient.UTF_8);
		long end = System.currentTimeMillis();
		logger.debug("【请求耗时】,url:{},end-start:{}", url, (end - start));
		if (HttpURLConnection.HTTP_OK == result.code) {
			return result.content;
		}
		if (HttpURLConnection.HTTP_NOT_MODIFIED == result.code) {
			return StringUtils.EMPTY;
		}
		throw new MoscowException(result.code, result.content);
	}

	private List<String> getServerList() {
		List<String> snapshot = serversFromEndpoint;
		if (!CollectionUtils.isEmpty(serverList)) {
			snapshot = serverList;
		}
		return snapshot;
	}

	public List<String> builderHeaders() {
		List<String> headers = Arrays.asList("Client-Version", VERSION, "User-Agent", "Nacos-Java-Client:" + VERSION,
				"Accept-Encoding", "gzip,deflate,sdch", "Connection", "Keep-Alive", "RequestId",
				UUID.randomUUID().toString(), "Request-Module", "Naming");
		return headers;
	}
}
