package com.demo.recorder;


import java.io.File;

import com.demo.util.GetIp;
import com.demo.util.RegisterUtil;
import com.demo.util.UploadUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

/**
 * @author Tong
 * @version 创建时间：Dec 13, 2015 7:36:26 PM
 * 类说明
 */
public class FragmentSetting extends Fragment{
    private Switch uploadSwitch, liveSwitch;
    private TextView ipTextView, usernameTextView;
    private EditText ipEditText, usernameEditText;
    private Button exitButton;
    private static String requestURL;
    private File mRecVedioPath; // 录制文件路徑
    
    // 全局变量
    private MyApplication app;
    
    // 上传线程
    private UploadUtil upload;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_setting, container, false);
		uploadSwitch = (Switch) view.findViewById(R.id.switch_upload);
		liveSwitch = (Switch) view.findViewById(R.id.switch_live);
		ipTextView = (TextView) view.findViewById(R.id.text_ip);
		usernameTextView = (TextView) view.findViewById(R.id.text_username);
		exitButton = (Button) view.findViewById(R.id.tb_register);
		exitButton.setVisibility(View.INVISIBLE);
		// 获取全局变量
		app = (MyApplication) getActivity().getApplication();		
		
		// 设置文件路径
		String savedPath = Environment.getExternalStorageDirectory() + "/RecorderData/";
        mRecVedioPath = new File(savedPath);
        if (!mRecVedioPath.exists()) {
			mRecVedioPath.mkdir();
		}
		
		uploadSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// 从设置里面获取网络地址
				requestURL = "http://" + app.getIpAddress() + ":8080/recorderServer?flag=" + app.getUserName();
				
				// TODO Auto-generated method stub
				if (app.isRegisted()) {
					if (isChecked) {
						// 设置全局变量 uploadEnabled
						app.setUploadEnabled(true);
						
						// 创建上传线程
						upload = new UploadUtil(requestURL, mRecVedioPath, true, app);
						upload.start();
					} else {
						app.setUploadEnabled(false);
						if (upload != null) {
							// 销毁上传线程
							upload.setFlag(false);
						}
					}
				} else {
					uploadSwitch.setChecked(false);
					Toast.makeText(getActivity(), "请先设置昵称！", Toast.LENGTH_SHORT).show();
				}
			}
		});;
		
		liveSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (app.isRegisted()) {
					if (isChecked) {
						app.setLiveEnabled(true);
						if (app.isRecording()) {
							new RegisterUtil(app).uploadMSG("ON", app.getUserName() + ":" + (new GetIp(getActivity()).getLocalIP()));
							Toast.makeText(getActivity(), "本机正在直播...", Toast.LENGTH_SHORT).show();
						}
					} else {
						app.setLiveEnabled(false);
						// 通知服务器下线
	                	new RegisterUtil(app).uploadMSG("OFF", app.getUserName());
					}
				} else {
					liveSwitch.setChecked(false);
					Toast.makeText(getActivity(), "请先设置昵称！", Toast.LENGTH_SHORT).show();
				}
				
			}
		});		
		
		ipTextView.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ipEditText = new EditText(getActivity()); 
				ipEditText.setText(app.getIpAddress());
				
				new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_ip).setView(ipEditText)
				.setPositiveButton(R.string.dialog_confirm, 
						new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								app.setIpAddress(ipEditText.getText().toString().trim());
								Toast.makeText(getActivity(), " 设置成功！", Toast.LENGTH_SHORT).show();;
							}
						}).setNegativeButton(R.string.dialog_cancel, null).show();
			}
		});
		
		usernameTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (app.getUserName().equals("nullnull")) {
					usernameEditText = new EditText(getActivity());
					
					new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_username).setView(usernameEditText)
					.setPositiveButton(R.string.dialog_confirm, 
							new DialogInterface.OnClickListener() {
								
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									String tempName = usernameEditText.getText().toString().trim();
									
									String result = new RegisterUtil(app).getOnlineList("REG", tempName);
									
									if (result.equals("YES")) {
										app.setUserName(tempName);
										app.setRegisted(true);
										usernameTextView.setText("用户 " + app.getUserName() + " 已登录！");
										Toast.makeText(getActivity(), "设置成功！", Toast.LENGTH_SHORT).show();
										exitButton.setVisibility(View.VISIBLE);
									} else {
										Toast.makeText(getActivity(), "已经有人注册，请重新输入！", Toast.LENGTH_SHORT).show();
										usernameEditText.setError("已经有人注册！");
									}					
								}
							}).setNegativeButton(R.string.dialog_cancel, null).show();
				}
			}
		});
		
		exitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				app.setRegisted(false);
				usernameTextView.setText("点击设置昵称");
				exitButton.setVisibility(View.INVISIBLE);
				liveSwitch.setChecked(false);
				uploadSwitch.setChecked(false);
				new RegisterUtil(app).uploadMSG("DES", app.getUserName());
				app.setUserName("nullnull");
			}
		});
		
		return view;
	}
}
