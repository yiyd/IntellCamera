package com.demo.recorder;

import android.app.Application;

/**
 * @author Tong
 * @version 创建时间：Dec 14, 2015 10:01:21 AM
 * 类说明
 */
public class MyApplication extends Application{
	private boolean uploadEnabled;
	private boolean liveEnabled;
	private boolean isRecording;
	private boolean isRegisted;
	private boolean isConnected;
	private String ipAddress;
	private String userName;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		setUploadEnabled(false);
		setLiveEnabled(false);
		setRecording(false);
		setIpAddress("172.18.63.180");
		setUserName("nullnull");
		setRegisted(false);
		setConnected(false);
	}

	public boolean isUploadEnabled() {
		return uploadEnabled;
	}

	public void setUploadEnabled(boolean uploadEnabled) {
		this.uploadEnabled = uploadEnabled;
	}

	public boolean isLiveEnabled() {
		return liveEnabled;
	}

	public void setLiveEnabled(boolean liveEnabled) {
		this.liveEnabled = liveEnabled;
	}
	
	public boolean isRecording() {
		return isRecording;
	}

	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isRegisted() {
		return isRegisted;
	}

	public void setRegisted(boolean isRegisted) {
		this.isRegisted = isRegisted;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	
}
