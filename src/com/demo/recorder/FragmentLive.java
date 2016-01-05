package com.demo.recorder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.demo.util.RegisterUtil;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;  
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * @author Tong
 * @version 创建时间：Dec 13, 2015 7:51:40 PM
 * 类说明
 */
public class FragmentLive extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
	// 刷新相关
	private static final int REFRESH_COMPLETE = 0X110;  
    private SwipeRefreshLayout mSwipeLayout;  
    private ListView mListView;  
    public VideoView mVideoView;
    private TextView mTextView;
    private ArrayAdapter<String> mAdapter;  
    private List<String> mDatas = new ArrayList<String>(Arrays.asList("以下终端正在直播...(下拉刷新)"));    
    
    // LocalSocket
    private String SOCKETADDRESS = "test1";
    
    // 获取全局变量
    private MyApplication app;
    private RegisterUtil registerUtil;
    
    private Handler mHandler = new Handler (){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case REFRESH_COMPLETE:					
					mAdapter.notifyDataSetChanged();  
	                mSwipeLayout.setRefreshing(false);
				break;

			default:
				break;
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_live, container, false);
		mListView = (ListView) view.findViewById(R.id.id_listview);  
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.id_swipe_ly);  
        
        mTextView = (TextView) view.findViewById(R.id.text_live);
        
        mVideoView = (VideoView) view.findViewById(R.id.videoView1);
        mVideoView.setZOrderMediaOverlay(true);
        final MediaController mediaController = new MediaController(getActivity(), true);
        mediaController.setAnchorView(mVideoView);
        mediaController.setKeepScreenOn(true);
        mediaController.setVisibility(View.VISIBLE);
        
        mSwipeLayout.setOnRefreshListener(this);  
        mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,  
                android.R.color.holo_orange_light, android.R.color.holo_red_light);  
        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mDatas);  
        mListView.setAdapter(mAdapter);  
        
        app = (MyApplication) getActivity().getApplication();
        registerUtil = new RegisterUtil(app);
        
        //new SocketListener(mHandler, notificationRunnable).start();  
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				//Toast.makeText(getActivity(), position, Toast.LENGTH_SHORT).show();;
				Log.i("LiveClick", "heihei" + position);
				Uri video;
				if (position == 0) {
					//Uri video = Uri.parse("rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp"); //link to web video . showing "sorry this video can not be played"
			        //Uri video = Uri.parse("rtsp://v5.cache1.c.youtube.com/CjYLENy73wIaLQnhycnrJQ8qmRMYESARFEIJbXYtZ29vZ2xlSARSBXdhdGNoYPj_hYjnq6uUTQw=/0/0/0/video.3gp");// can be applyed on machine  
					
					//Uri video = Uri.parse("rtsp://" + app.getIpAddress() + "/test.sdp");
					video = Uri.parse("rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp");	
					mTextView.setText("直播测试");
				} else {
					video = Uri.parse("rtsp://" + app.getIpAddress() + "/" + mDatas.get(position) + ".sdp");
					Log.i("PLAY", video.toString());
					mTextView.setText("正在收看 " + mDatas.get(position));
				}
				
				mVideoView.setMediaController(mediaController);
				mVideoView.setVideoURI(video);
				mVideoView.start();
			}
		});        
        
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Uri video = Uri.parse("rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp");
				if (position == 0) {
					video = Uri.parse("rtsp://172.18.63.180/tianti.mp4");	
					mTextView.setText("彩蛋！");
				}
				
				mVideoView.setMediaController(mediaController);
				mVideoView.setVideoURI(video);
				mVideoView.start();
				
				return true;
			}
		});
		return view;
	}

	private void getOnlineList() {
		// TODO Auto-generated method stub
		//Toast.makeText(getActivity(), registerUtil.getOnlineList("GET", app.getIpAddress()).toString(), Toast.LENGTH_SHORT);
		mDatas.clear();
		mDatas.add("以下终端正在直播...(下拉刷新)");
		
		String temp = registerUtil.getOnlineList("GET", app.getIpAddress());

		if (!temp.equals("")) {
			String [] tempStrings = temp.split("/");
			if (tempStrings.length > 0) {
				for (int i = 1; i < tempStrings.length; i++) {
					if (tempStrings[i].equals(app.getUserName())) {
						mDatas.add(tempStrings[i]);
					} else {
						mDatas.add(tempStrings[i]);
					}
				}
			}
		}
	}

	public void onRefresh() {
		// TODO Auto-generated method stub
		if (!app.getUserName().equals("nullnull")) {
			getOnlineList();
		} else {
			Toast.makeText(getActivity(), "请先设置昵称！", Toast.LENGTH_SHORT).show();
		}
		mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000); 
	}
	
	public class NotificationRunnable implements Runnable {
        private String message = null;
        
        public void run() {
            if (message != null && message.length() > 0) {
                showNotification(message);
            }
        }
       
        /**
        * @param message the message to set
        */
        public void setMessage(String message) {
            this.message = message;
        }
    }
   
    // post this to the Handler when the background thread notifies
    private final NotificationRunnable notificationRunnable = new NotificationRunnable();
   
    public void showNotification(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
   
    class SocketListener extends Thread {
        private Handler handler;
        private NotificationRunnable runnable = null;
       
        public SocketListener(Handler handler, NotificationRunnable runnable) {
            this.handler = handler;
            this.runnable = runnable;
            this.handler.post(this.runnable);
        }
       
        /**
        * Show UI notification.
        * @param message
        */
        private void showMessage(String message) {
            this.runnable.setMessage(message);
            this.handler.post(this.runnable);
        }
       
        @Override
        public void run() {
            showMessage("DEMO: SocketListener started!");
            try {
                LocalServerSocket server = new LocalServerSocket(SOCKETADDRESS);
                while (true) {
                    LocalSocket receiver = server.accept();
                    if (receiver != null) {
                        InputStream input = receiver.getInputStream();
                       
                        // simply for java.util.ArrayList
                        int readed = input.read();
                        int size = 0;
                        int capacity = 0;
                        byte[] bytes = new byte[capacity];
                       
                        // reading
                        while (readed != -1) {
                            // java.util.ArrayList.Add(E e);
                            capacity = (capacity * 3)/2 + 1;
                            //bytes = Arrays.copyOf(bytes, capacity);
                            byte[] copy = new byte[capacity];
                            System.arraycopy(bytes, 0, copy, 0, bytes.length);
                            bytes = copy;
                            bytes[size++] = (byte)readed;
                           
                            // read next byte
                            readed = input.read();
                        } 
                       
                        showMessage(new String(bytes, 0, size));
                        mDatas.add(new String(bytes, 0, size));
                        
                    } else {
                    	Log.d("Socket Error", "unconnected");
                    }
                }
            } catch (IOException e) {
                Log.e(getClass().getName(), e.getMessage());
            }
        }
    }
}
