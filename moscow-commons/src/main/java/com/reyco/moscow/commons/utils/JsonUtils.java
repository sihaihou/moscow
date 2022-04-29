package com.reyco.moscow.commons.utils;

import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public class JsonUtils {
	/**
	 * 
	 * 对象转json字符串
	 * @param t
	 * @return
	 */
	public static <V> String objToJson(V t) {
		return JSON.toJSONString(t);
	}
	/**
	 * json字符串转目标对象
	 * @param <V>
	 * @param json
	 * @param targetClazz
	 * @return
	 */
	public static <V> V jsonToObj(String json, Class<V> targetClazz) {
		V v = JSONArray.parseObject(json, targetClazz);
		return v;
	}
	/**
	 * jsonList字符串转目标对象list
	 * @param
	 * @param
	 * @return
	 */
	public static <V> List<V> jsonListToObjList(String jsonList, Class<V> targetClazz) {
		List<V> list = JSONArray.parseArray(jsonList, targetClazz);
		return list;
	}
	
	/**
	 * 对象转JSONObject
	 * @param obj
	 * @return
	 */
	public static JSONObject objToJSONObject(Object obj) {
		return (JSONObject)JSONObject.toJSON(obj);
	}
	/**
	 * JSONObject转目标对象
	 * @param jsonObject
	 * @param
	 * @return
	 */
	public static <V> V JSONObjectToTarget(JSONObject jsonObject,Class<V> tragetClass) {
		return JSONObject.toJavaObject(jsonObject, tragetClass);
	}
	
	public static void main(String[] args) {

	}


}
