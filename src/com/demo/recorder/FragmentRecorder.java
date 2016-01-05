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
 * @version 创建时间：Dec 13, 2015 7:24:21 PM
 * 类说明
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class FragmentRecorder extends Fragment implements SurfaceHolder.Callback{
    private Button mVideoStartBtn,mVideoStopBtn;
    public SurfaceView mSurfaceview;         // 显示视频的控件
    private MediaRecorder mMediaRecorder;     // MediaRecorder对象，
    private SurfaceHolder mSurfaceHolder;    // 
    private File mRecVedioPath;                // 录制文件路徑
    private File mRecAudioFile;                // 录制文件
	private MyApplication app;

	private static final String TAG = "VideoCamera";
	SharedPreferences sharedPreferences;
	private final String mediaShare = "media";
			
	//初始化LocalServerSocket LocalSocket
    LocalServerSocket lss;
    LocalSocket receiver, sender;
	protected AbstractPacketizer packetizer = null;
	protected boolean streaming = false, modeDefaultWasUsed = false;	
	
	// Baidu Map
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Context context;
	// 定位相关
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
        // 获取全局变量
        app = (MyApplication) getActivity().getApplication();       
        
        // init the mapView
        //SDKInitializer.initialize(getActivity().getApplicationContext());
        //this.context = getActivity();
        //mMapView = (MapView) view.findViewById(R.id.id_bmapView);
        //initMapView();
        
        // TODO Auto-generated method stub
        SurfaceHolder holder = mSurfaceview.getHolder();        // 取得holder
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);                 // holder加入回调接口
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//setType必须设置        
       	
        /* 检测是否存在SD卡 */
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
        	String savedPath = Environment.getExternalStorageDirectory() + "/RecorderData/";
            mRecVedioPath = new File(savedPath);
            if (!mRecVedioPath.exists()) {
				mRecVedioPath.mkdir();
			}
        } else {
            Toast.makeText(getActivity(), "没有SD卡", Toast.LENGTH_LONG).show();
        } 
        
        // 按钮状态
        mVideoStartBtn.setEnabled(true);
        mVideoStopBtn.setEnabled(false);

        mVideoStartBtn.setOnClickListener(new Button.OnClickListener() {        	
            @SuppressLint("ShowToast")
			public void onClick(View v) {   
            	app.setRecording(true);
            	
                /* ①Initial：实例化MediaRecorder对象 */
                if(mMediaRecorder == null)
                    mMediaRecorder = new MediaRecorder();
                else
                    mMediaRecorder.reset();

                mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                /* ②setAudioSource/setVedioSource */
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 设置Camera(相机)
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                /* ②编码：AAC/AMR_NB/AMR_MB/Default */
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                /* ②设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错 */
                mMediaRecorder.setVideoSize(640, 480);
                /* ②设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错 */
                mMediaRecorder.setVideoFrameRate(24);                
                /* ②设置输出文件的路径 */                
                try {
                	Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。  
                	t.setToNow(); 
                	
                	mRecAudioFile = File.createTempFile("Vedio" + t.year + "-" + t.month + "-"
                			+ t.monthDay + "-" + t.hour + "-" + t.minute 
                			+ "-" + t.second,".mp4",mRecVedioPath);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                
                // 选择本地存储还是实时流上传
                if (app.isLiveEnabled()) {

					// 通知服务器后台该终端上线
                	new RegisterUtil(app).uploadMSG("ON", app.getUserName() + ":" + (new GetIp(getActivity()).getLocalIP()));
                	Toast.makeText(getActivity(), "本机正在直播...", Toast.LENGTH_SHORT).show();
                	
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
                                             
                    /* 按钮状态 */
                    mVideoStartBtn.setEnabled(false);
                    mVideoStopBtn.setEnabled(true);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        // 結束
        mVideoStopBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {                
                if (mMediaRecorder != null) {
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                    
                    /* 按钮状态 */
                    mVideoStartBtn.setEnabled(true);
                    mVideoStopBtn.setEnabled(false);                                        
                    app.setRecording(false);
                    
                    // 通知服务器下线
                	if (app.isLiveEnabled()) {
                		new RegisterUtil(app).uploadMSG("OFF", app.getUserName());
                		Toast.makeText(getActivity(), "本机退出直播...", Toast.LENGTH_SHORT).show();
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
		
		// 开启定位
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
	
	//开始录像，启动线程
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
