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
 * @version ����ʱ�䣺Dec 13, 2015 7:36:26 PM
 * ��˵��
 */
public class FragmentSetting extends Fragment{
    private Switch uploadSwitch, liveSwitch;
    private TextView ipTextView, usernameTextView;
    private EditText ipEditText, usernameEditText;
    private Button exitButton;
    private static String requestURL;
    private File mRecVedioPath; // ¼���ļ�·��
    
    // ȫ�ֱ���
    private MyApplication app;
    
    // �ϴ��߳�
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
		// ��ȡȫ�ֱ���
		app = (MyApplication) getActivity().getApplication();		
		
		// �����ļ�·��
		String savedPath = Environment.getExternalStorageDirectory() + "/RecorderData/";
        mRecVedioPath = new File(savedPath);
        if (!mRecVedioPath.exists()) {
			mRecVedioPath.mkdir();
		}
		
		uploadSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// �����������ȡ�����ַ
				requestURL = "http://" + app.getIpAddress() + ":8080/recorderServer?flag=" + app.getUserName();
				
				// TODO Auto-generated method stub
				if (app.isRegisted()) {
					if (isChecked) {
						// ����ȫ�ֱ��� uploadEnabled
						app.setUploadEnabled(true);
						
						// �����ϴ��߳�
						upload = new UploadUtil(requestURL, mRecVedioPath, true, app);
						upload.start();
					} else {
						app.setUploadEnabled(false);
						if (upload != null) {
							// �����ϴ��߳�
							upload.setFlag(false);
						}
					}
				} else {
					uploadSwitch.setChecked(false);
					Toast.makeText(getActivity(), "���������ǳƣ�", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(getActivity(), "��������ֱ��...", Toast.LENGTH_SHORT).show();
						}
					} else {
						app.setLiveEnabled(false);
						// ֪ͨ����������
	                	new RegisterUtil(app).uploadMSG("OFF", app.getUserName());
					}
				} else {
					liveSwitch.setChecked(false);
					Toast.makeText(getActivity(), "���������ǳƣ�", Toast.LENGTH_SHORT).show();
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
								Toast.makeText(getActivity(), " ���óɹ���", Toast.LENGTH_SHORT).show();;
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
										usernameTextView.setText("�û� " + app.getUserName() + " �ѵ�¼��");
										Toast.makeText(getActivity(), "���óɹ���", Toast.LENGTH_SHORT).show();
										exitButton.setVisibility(View.VISIBLE);
									} else {
										Toast.makeText(getActivity(), "�Ѿ�����ע�ᣬ���������룡", Toast.LENGTH_SHORT).show();
										usernameEditText.setError("�Ѿ�����ע�ᣡ");
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
				usernameTextView.setText("��������ǳ�");
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
