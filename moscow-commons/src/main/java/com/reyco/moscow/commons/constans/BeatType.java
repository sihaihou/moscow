package com.reyco.moscow.commons.constans;

/**
 * @author reyco
 * @date 2022.04.13
 * @version v1.0.1
 */
public enum BeatType {
	
	SERVICE_BEAT_INTERVAL(5000, "服务心跳间隔时间"),
	
	SERVICE_BEAT_TIMEOUT_UNHEALTHY(15000, "服务不健康心跳时间"),
	
	SERVICE_BEAT_TIMEOUT_DELETE(30000, "服务宕机心跳时间");
	
	private long time;
	private String name;
	
	BeatType(Integer time, String name) {
        this.time = time;
        this.name = name;
    }

    public static BeatType getStatusType(Integer time) {
        for (BeatType type : BeatType.values()) {
            if (type.time == time) {
                return type;
            }
        }
        return null;
    }
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
