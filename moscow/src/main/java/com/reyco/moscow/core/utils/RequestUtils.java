package com.reyco.moscow.core.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
/**
 * @author reyco
 * @date 2022.03.15
 * @version v1.0.1
 */
public class RequestUtils {

	/**
	 * 必须参数
	 * @param req
	 * @param paramName
	 * @return
	 */
	public static String required(HttpServletRequest request, String paramName) {
		String value = request.getParameter(paramName);
		if (StringUtils.isBlank(value)) {
			throw new IllegalArgumentException("Param '" + paramName + "' is required.");
		}
		String encoding = request.getParameter("encoding");
		if (!StringUtils.isBlank(encoding)) {
			try {
				value = new String(value.getBytes(StandardCharsets.UTF_8), encoding);
			} catch (UnsupportedEncodingException ignore) {
			}
		}
		return value.trim();
	}

	/**
	 * 可选参数
	 * @param req
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String optional(HttpServletRequest request, String key, String defaultValue) {
		if (!request.getParameterMap().containsKey(key) || request.getParameterMap().get(key)[0] == null) {
			return defaultValue;
		}
		String value = request.getParameter(key);
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		String encoding = request.getParameter("encoding");
		if (!StringUtils.isBlank(encoding)) {
			try {
				value = new String(value.getBytes(StandardCharsets.UTF_8), encoding);
			} catch (UnsupportedEncodingException ignore) {
			}
		}
		return value.trim();
	}
	/**
	 * Accept-Charset
	 * @param req
	 * @return
	 */
	public static String getAcceptEncoding(HttpServletRequest request) {
		String encode = StringUtils.defaultIfEmpty(request.getHeader("Accept-Charset"), "UTF-8");
		encode = encode.contains(",") ? encode.substring(0, encode.indexOf(",")) : encode;
		return encode.contains(";") ? encode.substring(0, encode.indexOf(";")) : encode;
	}

	/**
	 * User-Agent
	 * @param request
	 * @return
	 */
	public static String getUserAgent(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (StringUtils.isEmpty(userAgent)) {
			userAgent = StringUtils.defaultIfEmpty(request.getHeader("Client-Version"),StringUtils.EMPTY);
		}
		return userAgent;
	}
}
