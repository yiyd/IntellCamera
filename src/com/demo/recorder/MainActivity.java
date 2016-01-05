package com.demo.recorder;

import com.demo.util.RegisterUtil;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements OnClickListener{
	//����3��Fragment�Ķ���
    private FragmentRecorder fg1;
    private FragmentLive fg2;
    private FragmentSetting fg3;
    //֡���ֶ���,�����������Fragment������
    private FrameLayout flayout;
    //����ײ�����������������
    private RelativeLayout main_recorder_layout;
    private RelativeLayout main_live_layout;
    private RelativeLayout main_setting_layout;
    //����ײ��������е�ImageView��TextView
    private ImageView recorder_image;
    private ImageView live_image;
    private ImageView setting_image;
    private TextView recorder_text;
    private TextView setting_text;
    private TextView live_text;
    //����Ҫ�õ���ɫֵ
    private int whirt = 0xFFFFFFFF;
    private int gray = 0xFF7597B3;
    private int blue =0xFF0AB2FB;
    //����FragmentManager����
    private FragmentManager fManager;
    
    private MyApplication app;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);      
    	
        if (savedInstanceState == null) {
        	/* ȫ��Activity���� */
            // ��ȥTitle����������֣�
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // �O��ȫ������ȥ��ص�ͼ���һ�����β��֣�״̬�����֣�
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // ѡ��֧�ְ�͸��ģʽ,����surfaceview��activity��ʹ�á�        
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            setContentView(R.layout.activity_main);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            fManager = this.getSupportFragmentManager();     
            
            // ��ȡȫ�ֱ���
            app = (MyApplication) getApplication(); 
            intiView();
            setChioceItem(2);
		}   
    }
     
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		new RegisterUtil(app).uploadMSG("DES", app.getUserName());
		new RegisterUtil(app).uploadMSG("OFF", app.getUserName());
		app.setRegisted(false);
		app.setLiveEnabled(false);
		app.setUploadEnabled(false);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		new RegisterUtil(app).uploadMSG("DES", app.getUserName());
		new RegisterUtil(app).uploadMSG("OFF", app.getUserName());
		app.setRegisted(false);
		app.setLiveEnabled(false);
		app.setUploadEnabled(false);
		super.onStop();
	}

	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onResumeFragments() {
		// TODO Auto-generated method stub
		super.onResumeFragments();
	}

	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	
	private void intiView() {
		// TODO Auto-generated method stub
    
        recorder_image = (ImageView) findViewById(R.id.recorder_image);
        live_image = (ImageView) findViewById(R.id.live_image);
        setting_image = (ImageView) findViewById(R.id.setting_image);
        recorder_text = (TextView) findViewById(R.id.recorder_text);
        live_text = (TextView) findViewById(R.id.live_text);
        setting_text = (TextView) findViewById(R.id.setting_text);
        main_recorder_layout = (RelativeLayout) findViewById(R.id.main_recorder_layout);
        main_live_layout = (RelativeLayout) findViewById(R.id.main_live_layout);
        main_setting_layout = (RelativeLayout) findViewById(R.id.main_setting_layout);
        main_recorder_layout.setOnClickListener((OnClickListener) this);;
        main_live_layout.setOnClickListener((OnClickListener) this); 
        main_setting_layout.setOnClickListener((OnClickListener) this);
                     
        /*
        //�󶨼�����
        mCheckBoxUpload.setOnCheckedChangeListener(new OnCheckedChangeListener() {                 
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					File [] files = mRecVedioPath.listFiles();
					for (File file : files) {										
						Toast.makeText(MainActivity.this, "UPLOADING " + file.getName(), Toast.LENGTH_LONG).show();
						//UploadUtil.uploadFile(file, requestURL);
						uploadFile(file);
						file.delete();
					}
				}
			}
        });
        
		*/
	}
        
    //����һ��ѡ��һ��item��Ĵ���
    public void setChioceItem(int index)
    {
        //����ѡ��+��������Fragment
        FragmentTransaction transaction = fManager.beginTransaction();  
        clearChioce();
        hideFragments(transaction);
        switch (index) {
        case 0:
            //recorder_image.setImageResource(R.drawable.ic_tabbar_course_pressed);  
            recorder_text.setTextColor(blue);
            main_recorder_layout.setBackgroundResource(R.drawable.ic_tabbar_bg_click);
            if (fg1 == null) {  
                // ���fg1Ϊ�գ��򴴽�һ������ӵ�������  
                fg1 = new FragmentRecorder();  
                transaction.add(R.id.content, fg1);                
            } else {  
                // ���MessageFragment��Ϊ�գ���ֱ�ӽ�����ʾ����         
            	fg1.mSurfaceview.setZOrderMediaOverlay(true);
                transaction.show(fg1);
            }  
            break;  
 
        case 1:
            //live_image.setImageResource(R.drawable.ic_tabbar_found_pressed);  
            live_text.setTextColor(blue);
            main_live_layout.setBackgroundResource(R.drawable.ic_tabbar_bg_click);
            if (fg2 == null) {  
                // ���fg1Ϊ�գ��򴴽�һ������ӵ�������  
                fg2 = new FragmentLive(); 
                transaction.add(R.id.content, fg2);
            } else {  
                // ���MessageFragment��Ϊ�գ���ֱ�ӽ�����ʾ����         
            	fg2.mVideoView.setZOrderMediaOverlay(true);
                transaction.show(fg2); 
            }  
            break;      
         
         case 2:
            //setting_image.setImageResource(R.drawable.ic_tabbar_settings_pressed);  
            setting_text.setTextColor(blue);
            main_setting_layout.setBackgroundResource(R.drawable.ic_tabbar_bg_click);
            if (fg3 == null) {  
                // ���fg1Ϊ�գ��򴴽�һ������ӵ�������  
                fg3 = new FragmentSetting();  
                transaction.add(R.id.content, fg3);  
            } else {  
                // ���MessageFragment��Ϊ�գ���ֱ�ӽ�����ʾ����  
                transaction.show(fg3);  
            }  
            break;                 
        }
        transaction.commit();
    }
     
    //�������е�Fragment,����fragment����
    private void hideFragments(FragmentTransaction transaction) {  
        if (fg1 != null) {  
            transaction.hide(fg1);  
        }  
        if (fg2 != null) {  
            transaction.hide(fg2);  
        }  
        if (fg3 != null) {  
            transaction.hide(fg3);  
        }  
    }  
         
    //����һ����������ѡ��ķ���
    public void clearChioce()
    {
        recorder_image.setImageResource(R.drawable.video);
        main_recorder_layout.setBackgroundColor(whirt);
        recorder_text.setTextColor(gray);
        live_image.setImageResource(R.drawable.view);
        main_live_layout.setBackgroundColor(whirt);
        live_text.setTextColor(gray);
        setting_image.setImageResource(R.drawable.setting);
        main_setting_layout.setBackgroundColor(whirt);
        setting_text.setTextColor(gray);
    }
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
        case R.id.main_recorder_layout:
            setChioceItem(0);
            break;
        case R.id.main_live_layout:
            setChioceItem(1);
            break;
        case R.id.main_setting_layout:
            setChioceItem(2);
            break;
        default:
            break;
        }
	}

//	@Override
//	public void onAttachFragment(Fragment fragment) {
//		// TODO Auto-generated method stub
//		super.onAttachFragment(fragment);
//		Log.d("OnAttach","onAttachFragment");
//		
//		if (fg1 == null && fragment instanceof FragmentRecorder) {  
//            fg1 = (FragmentRecorder) fragment;  
//        }else if (fg2 == null && fragment instanceof FragmentLive) {  
//            fg2 = (FragmentLive)fragment;  
//        }else if (fg3 == null && fragment instanceof FragmentSetting) {  
//            fg3 = (FragmentSetting)fragment;  
//        }
//	}
}