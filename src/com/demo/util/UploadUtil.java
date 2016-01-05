package com.demo.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.demo.recorder.MyApplication;
import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

/**
 * @author Tong
 * @version ����ʱ�䣺Nov 29, 2015 6:29:25 PM
 * ��˵��
 */
public class UploadUtil extends Thread{
    private static final String TAG = "uploadVideo";
    private String requestURL;
    private File mRecVedioPath; // ¼���ļ�·��
    private boolean flag;  
    private MyApplication app;
	 
	public UploadUtil(String requestURL, File mRecVedioPath, boolean flag, MyApplication app) {
		super();
		this.requestURL = requestURL;
		this.mRecVedioPath = mRecVedioPath;
		this.flag = flag;
		this.app = app;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (isFlag()) {
			File [] files = mRecVedioPath.listFiles();
			for (File file : files) {		
				Log.d(TAG, file.getName());
				//Toast.makeText(getActivity(), "UPLOADING " + file.getName(), Toast.LENGTH_LONG).show();
				
				if (!app.isRecording()) {
					uploadFile(file);
					file.delete();
				}	
			}		
		}
		
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	@SuppressLint("NewApi")
	private void uploadFile(File file) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        
        try {
        	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());      	
            //requestURL = requestURL + "?flag=" + app.getUserName();
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
 
            DataOutputStream ds = new DataOutputStream(con.getOutputStream());
            
            // ����
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; "
                    + "name=\"file1\";filename=\"" + file.getName() + "\"" + end);
            ds.writeBytes(end);
 
            // ȡ���ļ���FileInputStream
            FileInputStream fStream = new FileInputStream(file);
            /* ����ÿ��д��1024bytes */
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            /* ���ļ���ȡ������������ */
            while ((length = fStream.read(buffer)) != -1) {
                /* ������д��DataOutputStream�� */
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
 
            fStream.close();
            ds.flush();
            /* ȡ��Response���� */
            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }

            //Toast.makeText(this.getActivity(), "Upload Success", Toast.LENGTH_SHORT).show();
            
            /* �ر�DataOutputStream */
            ds.close();
            
            //file.delete();
        } catch (Exception e) {
        	Log.d("Upload Fail", e.getMessage());
        	//Toast.makeText(this.getActivity(), "Upload Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
