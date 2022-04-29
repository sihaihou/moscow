package com.reyco.moscow.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/** 
 * @author  reyco
 * @date    2022.04.06
 * @version v1.0.1 
 */
public class ToString implements Serializable {
	private static final Collection<String> fieldNames = new ArrayList<String>();

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static String toString(Object obj) {
		if (fieldNames.size() == 0) {
			return ToStringBuilder.reflectionToString(obj, ToStringStyle.SHORT_PREFIX_STYLE);
		} else {
			return new ReflectionToStringBuilder(obj, ToStringStyle.SHORT_PREFIX_STYLE)
					.setExcludeFieldNames(fieldNames.toArray(new String[fieldNames.size()])).toString();
		}
	}

	/**
	 *
	 * @param fieldName
	 */
	public static void addFilterField(String fieldName) {
		fieldNames.add(fieldName);
	}

	@Override
	public String toString() {
		return toString(this);
	}
}