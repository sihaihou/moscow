package com.reyco.moscow.core.utils;

/**
 * @author reyco
 * @date 2022.03.23
 * @version v1.0.1
 */
public class KeyBuilder {
	public static final String NAMESPACE_KEY_CONNECTOR = "##";

	private static final String EPHEMERAL_KEY_PREFIX = "ephemeral.";

	public static final String INSTANCE_LIST_KEY_PREFIX = "com.reyco.moscow.iplist.";
	
	public static final String LISTENER_KEY_PREFIX = "com.reyco.moscow.listener.";

	private static String buildEphemeralInstanceListKey(String namespaceId, String serviceName) {
		return INSTANCE_LIST_KEY_PREFIX + EPHEMERAL_KEY_PREFIX + namespaceId + NAMESPACE_KEY_CONNECTOR + serviceName;
	}

	private static String buildPersistentInstanceListKey(String namespaceId, String serviceName) {
		return INSTANCE_LIST_KEY_PREFIX + namespaceId + NAMESPACE_KEY_CONNECTOR + serviceName;
	}

	public static String buildInstanceListKey(String namespaceId, String serviceName, boolean ephemeral) {
		return ephemeral ? buildEphemeralInstanceListKey(namespaceId, serviceName)
				: buildPersistentInstanceListKey(namespaceId, serviceName);
	}

	public static boolean matchEphemeralInstanceListKey(String key) {
		return key.startsWith(INSTANCE_LIST_KEY_PREFIX + EPHEMERAL_KEY_PREFIX);
	}
}
