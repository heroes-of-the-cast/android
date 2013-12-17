package com.timvdalen.hotc.data;

public class Session{
	private String key;
	
	private String device;
	
	private String ip;

	public Session(String key, String device, String ip){
		this.key = key;
		this.device = device;
		this.ip = ip;
	}

	public String getKey(){
		return key;
	}

	public String getDevice(){
		return device;
	}

	public String getIp(){
		return ip;
	}
}
