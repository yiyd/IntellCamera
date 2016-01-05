package com.demo.util;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.demo.recorder.MyApplication;
import android.os.StrictMode;


/**
 * @author Tong
 * @version ����ʱ�䣺Dec 16, 2015 3:08:51 PM
 * ��˵��
 */
public class RegisterUtil {
	private String requestURL;
	
	public RegisterUtil(MyApplication app) {
		this.requestURL = "http://" + app.getIpAddress() + ":8080/registerServlet";
	}

	public void uploadMSG(String TAG, String msg) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "****";
        
        try {
        	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());      	
            
        	//Ҫ���ݵ�����
            String query = "?flag=" + TAG + "&msg=" + msg;
        	requestURL += query;
        	
        	URL url = new URL(requestURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            /* ����Input��Output����ʹ��Cache */
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
 
            // ����http��������
            con.setRequestMethod("POST");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
 
            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            
            con.disconnect();
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
	
	public String getOnlineList (String TAG, String msg) {
		String result = "";
		
        //Ҫ���ݵ�����
		String query = "?flag=" + TAG + "&msg=" + msg;;
        requestURL += query;
        try{
        	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
	        URL url =new URL(requestURL);
	        //�������
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        
	        conn.setDoInput(true);
	        StringBuilder response = new StringBuilder();
	        Scanner in = new Scanner(conn.getInputStream());
			while (in.hasNextLine()) {
				response.append(in.nextLine());
			}
			
			in.close();
	        conn.disconnect();
	        result += response.toString();
	        
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return result;
	}
}
