package com.demo.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * @author Tong
 * @version 创建时间：Dec 14, 2015 11:03:06 AM
 * 类说明
 */
public class GetIp {
	private Context context;
	
	public GetIp(Context context) {
		this.context = context;
	}

	public String getLocalIP(){  
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);    
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();    
        int ipAddress = wifiInfo.getIpAddress();   
        if(ipAddress==0)return null;  
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
    }	
}
