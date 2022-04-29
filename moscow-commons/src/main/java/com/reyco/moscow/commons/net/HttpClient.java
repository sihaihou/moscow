package com.reyco.moscow.commons.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author reyco
 * @date 2022.04.01
 * @version v1.0.1
 */
public class HttpClient {

	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

	public static final String HTTP = "http://";

	public static final String HTTPS = "https://";

	public static final int CON_TIME_OUT_MILLIS = 5000;

	public static final int TIME_OUT_MILLIS = 5000;

	public static final String UTF_8 = "UTF-8";

	public static final String POST = "POST";
	public static final String GET = "GET";
	public static final String PUT = "PUT";

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("pageNo", "1");
		map.put("keywords", "周星驰");
		HttpResult request = httpGet("http://www.housihai.com/video/solrPage.do", new ArrayList<String>(), map,
				"UTF-8");
		System.out.println(request.getContent());
	}
	
	public static String getPrefix() {
		return HTTP;
	}

	public static HttpResult httpPost(String url, Map<String, String> param) {
		return httpPost(url, null, param,null);
	}

	public static HttpResult httpPost(String url, List<String> headers, Map<String, String> param,String body) {
		return httpPost(url, headers, param, body, UTF_8);
	}

	public static HttpResult httpPost(String url, List<String> headers, Map<String, String> param,String body, String encoding) {
		return request(url, headers, param,body, POST, encoding);
	}

	public static HttpResult httpGet(String url, Map<String, String> param) {
		return httpGet(url, null, param, UTF_8);
	}

	public static HttpResult httpGet(String url, List<String> headers, Map<String, String> param) {
		return httpGet(url, headers, param, UTF_8);
	}

	public static HttpResult httpGet(String url, List<String> headers, Map<String, String> param, String encoding) {
		return request(url, headers, param,null, GET, encoding);
	}

	public static HttpResult request(String url, List<String> headers, Map<String, String> param, String body, String method,
			String encoding) {
		return request(url, headers, param, body, method, encoding, null, null);
	}

	public static HttpResult request(String url, List<String> headers, Map<String, String> param,String body, String method,
			String encoding, String proxyHost, Integer ProxyPort) {
		HttpURLConnection conn = null;
		try {
			String encodedContent = processParams(param, encoding);
			url += StringUtils.isBlank(encodedContent) ? "" : ("?" + encodedContent);
			logger.debug("【请求地址】url:{}",url);
			URL u = new URL(url);
			if (StringUtils.isNotBlank(proxyHost)) {
				InetSocketAddress inetSocketAddress = new InetSocketAddress(proxyHost, ProxyPort);
				conn = (HttpURLConnection) u.openConnection(new Proxy(Proxy.Type.HTTP, inetSocketAddress));
			} else {
				conn = (HttpURLConnection) u.openConnection();
			}
			setHeaders(conn, headers, encoding);
			conn.setConnectTimeout(CON_TIME_OUT_MILLIS);
			conn.setReadTimeout(TIME_OUT_MILLIS);
			conn.setRequestMethod(method);
			conn.setDoOutput(true);
			if (StringUtils.isNotBlank(body)) {
				byte[] buffer = body.getBytes();
				conn.setRequestProperty("Content-Length", String.valueOf(buffer.length));
				conn.getOutputStream().write(buffer, 0, buffer.length);
				conn.getOutputStream().flush();
				conn.getOutputStream().close();
			}
			conn.connect();
			return processResponse(conn);
		} catch (Exception e) {
			try {
				if (conn != null) {
					logger.warn("【请求失败 】"+conn.getURL()+" from "+InetAddress.getByName(conn.getURL().getHost()).getHostAddress());
				}
			} catch (Exception e1) {
				logger.error("【请求失败 】,e1: ", e1);
			}
			logger.error("【请求失败 】 ,e:", e);
			return new HttpResult(500, e.toString(), Collections.<String, String>emptyMap());
		} finally {
			conn.disconnect();
		}
	}

	/**
	 * 处理响应信息
	 * 
	 * @param conn
	 * @return
	 * @throws IOException
	 */
	private static HttpResult processResponse(HttpURLConnection conn) throws IOException {
		int respCode = conn.getResponseCode();
		InputStream is;
		if (HttpURLConnection.HTTP_OK == respCode || HttpURLConnection.HTTP_NOT_MODIFIED == respCode) {
			is = conn.getInputStream();
		} else {
			is = conn.getErrorStream();
		}
		Map<String, String> respHeaders = new HashMap<String, String>(conn.getHeaderFields().size());
		for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
			respHeaders.put(entry.getKey(), entry.getValue().get(0));
		}
		String encodingGzip = "gzip";
		if (encodingGzip.equals(respHeaders.get("Content-Encoding"))) {
			is = new GZIPInputStream(new BufferedInputStream(is));
		}
		byte[] buffer = new byte[1 << 10];
		int lenth = 0;
		String content = "";
		while ((lenth = is.read(buffer)) != -1) {
			content += new String(buffer, 0, lenth, "utf-8");
		}
		return new HttpResult(respCode, content, respHeaders);
	}

	/**
	 * 设置请求头
	 * 
	 * @param conn
	 * @param headers
	 * @param encoding
	 */
	private static void setHeaders(HttpURLConnection conn, List<String> headers, String encoding) {
		if (null != headers) {
			for (Iterator<String> iter = headers.iterator(); iter.hasNext();) {
				conn.addRequestProperty(iter.next(), iter.next());
			}
		}
		conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);
		conn.addRequestProperty("Accept-Charset", encoding);
	}

	/**
	 * 处理参数
	 * 
	 * @param params
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String processParams(Map<String, String> params, String encoding)
			throws UnsupportedEncodingException {
		if (null == params || params.isEmpty()) {
			return "";
		}
		params.put("encoding", encoding);
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (StringUtils.isEmpty(entry.getValue())) {
				continue;
			}
			sb.append(entry.getKey()).append("=");
			sb.append(URLEncoder.encode(entry.getValue(), encoding));
			sb.append("&");
		}
		if (sb.length() > 0) {
			sb = sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * 
	 * @author reyco
	 * @date 2022.04.02
	 * @version v1.0.1
	 */
	public static class HttpResult implements Serializable {
		private static final long serialVersionUID = 3548516003610179934L;
		public int code;
		public final String content;
		private final Map<String, String> responseHeaders;

		public HttpResult(int code, String content, Map<String, String> responseHeaders) {
			super();
			this.code = code;
			this.content = content;
			this.responseHeaders = responseHeaders;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getContent() {
			return content;
		}

		public Map<String, String> getResponseHeaders() {
			return responseHeaders;
		}

		@Override
		public String toString() {
			return "HttpResult [code=" + code + ", content=" + content + ", responseHeaders=" + responseHeaders + "]";
		}
	}
}
