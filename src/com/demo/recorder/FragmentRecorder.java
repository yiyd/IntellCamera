package com.demo.recorder;


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import net.config.Config;
import net.majorkernelpanic.streaming.rtp.AbstractPacketizer;
import net.majorkernelpanic.streaming.rtp.H263Packetizer;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.demo.util.GetIp;
import com.demo.util.RegisterUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Tong
 * @version ����ʱ�䣺Dec 13, 2015 7:24:21 PM
 * ��˵��
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class FragmentRecorder extends Fragment implements SurfaceHolder.Callback{
    private Button mVideoStartBtn,mVideoStopBtn;
    public SurfaceView mSurfaceview;         // ��ʾ��Ƶ�Ŀؼ�
    private MediaRecorder mMediaRecorder;     // MediaRecorder����
    private SurfaceHolder mSurfaceHolder;    // 
    private File mRecVedioPath;                // ¼���ļ�·��
    private File mRecAudioFile;                // ¼���ļ�
	private MyApplication app;

	private static final String TAG = "VideoCamera";
	SharedPreferences sharedPreferences;
	private final String mediaShare = "media";
			
	//��ʼ��LocalServerSocket LocalSocket
    LocalServerSocket lss;
    LocalSocket receiver, sender;
	protected AbstractPacketizer packetizer = null;
	protected boolean streaming = false, modeDefaultWasUsed = false;	
	
	// Baidu Map
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Context context;
	// ��λ���
	private LocationClient mLocationClient;
	private boolean isFirstIn = true;
	private double mLatitude;
	private double mLongtitude;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//return super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_recorder, container, false);
		//view.bringToFront();
		mVideoStartBtn = (Button) view.findViewById(R.id.start);
        mVideoStopBtn = (Button) view.findViewById(R.id.stop);
        mSurfaceview = (SurfaceView) view.findViewById(R.id.surfaceView1);
        mSurfaceview.setZOrderMediaOverlay(true);
        // ��ȡȫ�ֱ���
        app = (MyApplication) getActivity().getApplication();       
        
        // init the mapView
        //SDKInitializer.initialize(getActivity().getApplicationContext());
        //this.context = getActivity();
        //mMapView = (MapView) view.findViewById(R.id.id_bmapView);
        //initMapView();
        
        // TODO Auto-generated method stub
        SurfaceHolder holder = mSurfaceview.getHolder();        // ȡ��holder
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);                 // holder����ص��ӿ�
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//setType��������        
       	
        /* ����Ƿ����SD�� */
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
        	String savedPath = Environment.getExternalStorageDirectory() + "/RecorderData/";
            mRecVedioPath = new File(savedPath);
            if (!mRecVedioPath.exists()) {
				mRecVedioPath.mkdir();
			}
        } else {
            Toast.makeText(getActivity(), "û��SD��", Toast.LENGTH_LONG).show();
        } 
        
        // ��ť״̬
        mVideoStartBtn.setEnabled(true);
        mVideoStopBtn.setEnabled(false);

        mVideoStartBtn.setOnClickListener(new Button.OnClickListener() {        	
            @SuppressLint("ShowToast")
			public void onClick(View v) {   
            	app.setRecording(true);
            	
                /* ��Initial��ʵ����MediaRecorder���� */
                if(mMediaRecorder == null)
                    mMediaRecorder = new MediaRecorder();
                else
                    mMediaRecorder.reset();

                mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                /* ��setAudioSource/setVedioSource */
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// ����Camera(���)
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                /* �ڱ��룺AAC/AMR_NB/AMR_MB/Default */
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                /* ��������Ƶ¼�Ƶķֱ��ʡ�����������ñ���͸�ʽ�ĺ��棬���򱨴� */
                mMediaRecorder.setVideoSize(640, 480);
                /* ������¼�Ƶ���Ƶ֡�ʡ�����������ñ���͸�ʽ�ĺ��棬���򱨴� */
                mMediaRecorder.setVideoFrameRate(24);                
                /* ����������ļ���·�� */                
                try {
                	Time t=new Time(); // or Time t=new Time("GMT+8"); ����Time Zone���ϡ�  
                	t.setToNow(); 
                	
                	mRecAudioFile = File.createTempFile("Vedio" + t.year + "-" + t.month + "-"
                			+ t.monthDay + "-" + t.hour + "-" + t.minute 
                			+ "-" + t.second,".mp4",mRecVedioPath);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                
                // ѡ�񱾵ش洢����ʵʱ���ϴ�
                if (app.isLiveEnabled()) {

					// ֪ͨ��������̨���ն�����
                	new RegisterUtil(app).uploadMSG("ON", app.getUserName() + ":" + (new GetIp(getActivity()).getLocalIP()));
                	Toast.makeText(getActivity(), "��������ֱ��...", Toast.LENGTH_SHORT).show();
                	
                	InitMediaSharePreference();
        	        InitAbstractPacketizer();
                	InitLocalSocket();
                	startVideoRecording();
                	mMediaRecorder.setOutputFile(sender.getFileDescriptor());
                	
                	//SessionBuilder.getInstance().setSurfaceView((net.majorkernelpanic.streaming.gl.SurfaceView) mSurfaceview);

                } else {
                	mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
                }                
                
                try {     
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                                             
                    /* ��ť״̬ */
                    mVideoStartBtn.setEnabled(false);
                    mVideoStopBtn.setEnabled(true);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        // �Y��
        mVideoStopBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {                
                if (mMediaRecorder != null) {
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                    
                    /* ��ť״̬ */
                    mVideoStartBtn.setEnabled(true);
                    mVideoStopBtn.setEnabled(false);                                        
                    app.setRecording(false);
                    
                    // ֪ͨ����������
                	if (app.isLiveEnabled()) {
                		new RegisterUtil(app).uploadMSG("OFF", app.getUserName());
                		Toast.makeText(getActivity(), "�����˳�ֱ��...", Toast.LENGTH_SHORT).show();
					}
                }
            }
        }); 
        
		return view;
	}	
    
	private void initMapView() {
		// TODO Auto-generated method stub		
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
		
		// ������λ
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted())
			mLocationClient.start();
	}

	private void InitAbstractPacketizer() {		  
		try {
			packetizer = new H263Packetizer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void InitMediaSharePreference() {
		sharedPreferences = getActivity().getSharedPreferences(mediaShare, getActivity().MODE_PRIVATE);		
	}
	
	private void InitLocalSocket(){
		try {
			lss = new LocalServerSocket(app.getUserName());
			receiver = new LocalSocket();
			
			receiver.connect(new LocalSocketAddress(app.getUserName()));
			receiver.setReceiveBufferSize(500000);			
			sender = lss.accept();			
			sender.setSendBufferSize(500000);
			
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			//this.finish();
			return;
		}
		
	}
	
	//��ʼ¼�������߳�
	private void startVideoRecording() {
		  try {
			setDestination(InetAddress.getByName(Config.host),Config.video_port);
		    packetizer.setInputStream(receiver.getInputStream());
			packetizer.start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/** The stream will be sent to the address specified by this function **/
	public void setDestination(InetAddress dest, int dport) {
		this.packetizer.setDestination(dest, dport);
	}
	
	/** Set the Time To Live of the underlying RtpSocket 
	 * @throws IOException **/
	public void setTimeToLive(int ttl) throws IOException {
		this.packetizer.setTimeToLive(ttl);
	}
	
	public int getDestinationPort() {
		return this.packetizer.getRtpSocket().getPort();
	}
	
	public int getLocalPort() {
		return this.packetizer.getRtpSocket().getLocalPort();
	}
	
	public void setMode(int mode) throws IllegalStateException {
		if (!streaming) {
		modeDefaultWasUsed = true;
		}
		else {
			throw new IllegalStateException("You can't call setMode() while streaming !");
		}
	}
	
	public AbstractPacketizer getPacketizer() { 
		return packetizer;
	}
	
    public void surfaceCreated(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
        mSurfaceHolder = holder;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        // TODO Auto-generated method stub
        mSurfaceHolder = holder;
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
        mSurfaceview = null;
        mSurfaceHolder = null;
        mMediaRecorder = null;
    }
}
