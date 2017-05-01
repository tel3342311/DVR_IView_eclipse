package com.liteon.iview;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.RecordingItem;
import com.liteon.iview.util.StatusDialog;
import com.liteon.iview.util.UsbUtil;

public class VideoPlay extends Activity {

	private final static String TAG = VideoPlay.class.getName();
    private View mToolbar;
    private View mBottomBar;
    private ImageView mRecordings;
    private ImageView mSettings;
    private ImageView mPreview;
    private View mSelectAll;
    private View mSaveToPhone;
    private View mSaveToOTG;
    private View mDelete;
    private VideoView mVideoView;
    private ViewGroup mViewControlGroup;
    private ImageView mPause;
    private ImageView mRewind;
    private ImageView mForward;
    private ImageView mSnapshot;
    private ImageView mThumbnail;
    private SeekBar   mSeekBar;
    private TextView  mVideoEndTime;
    private int mDuration;
    private Timer mUpdateUITimer;
    private boolean isComplete;
    private List<RecordingItem> mDataList;
    private boolean mShowingMenu;
    private Handler mHandlerTime;
    private ImageView mBackToRecords;
    private TextView mTitleView;
    private View mProgressView;
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_play);
		findViews();
		setListeners();
		mHandlerTime = new Handler();
		mDataList = new ArrayList<RecordingItem>();
	}
	
	private void registerOTGEvent() {
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Def.ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(Def.ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		if (UsbUtil.discoverDevice(getApplicationContext(), getIntent())) {
			mSaveToOTG.setEnabled(true);
		}
		
	}
	
	private void unregisterOTGEvent() {
		unregisterReceiver(mUsbReceiver);
	}
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Def.ACTION_USB_PERMISSION)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            Log.v(TAG, usbDevice.getDeviceName());
                        }
                        mSaveToOTG.setEnabled(true);
                    } else {
                        Log.v(TAG, "usb permission is denied");
                        mSaveToOTG.setEnabled(false);
                    }
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    //close connection
                    Log.v(TAG,"Device disconnected");
                    Toast.makeText(VideoPlay.this, "USB device disonnected!! getDeviceProtocol " + usbDevice.getDeviceProtocol() + "", Toast.LENGTH_LONG).show();
        			mSaveToOTG.setEnabled(false);
                }
            }else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Log.v(TAG,"Device connected");
                Toast.makeText(VideoPlay.this, "USB device Connected!! getDeviceProtocol " + usbDevice.getDeviceProtocol() + "", Toast.LENGTH_LONG).show();
                mSaveToOTG.setEnabled(true);
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    };
    
	private void setListeners() {
		mPreview.setOnClickListener(mOnPreviewClickListener);
		mSettings.setOnClickListener(mOnSettingsClickListener);
		mSaveToOTG.setOnClickListener(mOnSaveToOTGClickListener);
		mSaveToPhone.setOnClickListener(mOnSaveToPhoneClickListener);
		mBackToRecords.setOnClickListener(mOnBackClickListener);
		//video controls
        mVideoView.setOnTouchListener(mOnVideoViewTouchListener);
        mPause.setOnClickListener(mOnPlayPauseClickListener);
        mRewind.setOnClickListener(mOnRewindClickListener);
        mForward.setOnClickListener(mOnForwardClickListener);
        mSnapshot.setOnClickListener(mOnSnapShotClickListener);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

	}

	
    private View.OnTouchListener mOnVideoViewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                toggleMenu();
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener mOnBackClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mHandlerTime.removeCallbacks(HideUIControl);
			finish();
		}
	};

    private View.OnClickListener mOnPlayPauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mHandlerTime.removeCallbacks(HideUIControl);
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            } else {
                mVideoView.start();
            }
            mHandlerTime.postDelayed(HideUIControl, 1500);
        }
    };

    private View.OnClickListener mOnRewindClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mHandlerTime.removeCallbacks(HideUIControl);
            previousVideo();
            mHandlerTime.postDelayed(HideUIControl, 1500);
        }
    };

    private View.OnClickListener mOnForwardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mHandlerTime.removeCallbacks(HideUIControl);
            nextVideo();
            mHandlerTime.postDelayed(HideUIControl, 1500);
        }
    };

    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                stopUITimer();
                int position = mDuration * progress / 100;
                mVideoView.seekTo(position);
                updateUI();
                startUITimer();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        	mHandlerTime.removeCallbacks(HideUIControl);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        	mHandlerTime.postDelayed(HideUIControl, 1500);
        }
    };

    private View.OnClickListener mOnSnapShotClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

	private void findViews() {
		mToolbar = findViewById(R.id.toolbar_preview);
        mBottomBar = findViewById(R.id.bottombar);
        mPreview = (ImageView) findViewById(R.id.preview_icon);
        mRecordings = (ImageView) findViewById(R.id.recordings_icon);
        mSettings = (ImageView) findViewById(R.id.setting_icon);
        mSaveToOTG = findViewById(R.id.save_to_otg);
        mSaveToPhone = findViewById(R.id.save_to_phone);
        mSelectAll = findViewById(R.id.select_all);
        mDelete = findViewById(R.id.delete);
        mBackToRecords = (ImageView) findViewById(R.id.video_back);
		mToolbar = findViewById(R.id.toolbar_recordings);
        mBottomBar = findViewById(R.id.bottombar);
        mTitleView = (TextView) findViewById(R.id.toolbar_title);
        mProgressView = findViewById(R.id.progress_view);
        //video controls
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mViewControlGroup = (ViewGroup) findViewById(R.id.video_control);
        mPause = (ImageView) findViewById(R.id.play_pause);
        mRewind = (ImageView) findViewById(R.id.rewind);
        mForward = (ImageView) findViewById(R.id.forward);
        mSnapshot = (ImageView) findViewById(R.id.snapshot);
        mThumbnail = (ImageView) findViewById(R.id.thumbnail);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mVideoEndTime = (TextView) findViewById(R.id.end_time);
	}
	
	public View.OnClickListener mOnPreviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mRecordings.setSelected(true);
        	mPreview.setSelected(true);
        	Intent intent = new Intent(getApplicationContext(), Preview.class);
    		startActivity(intent);
    		finish();
    	}
    };
    
	public View.OnClickListener mOnSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mRecordings.setSelected(false);
        	mSettings.setSelected(true);
        	Intent intent = new Intent(getApplicationContext(), Settings.class);
    		startActivity(intent);        
    	}
    };
    
	private OnClickListener mOnSaveToOTGClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mProgressView.setVisibility(View.VISIBLE);
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            }
			RecordingItem item = getCurrentVideoItem();
			Intent intent = new Intent();
			intent.setAction(Def.ACTION_SAVE_TO_OTG);
			intent.putExtra(Def.EXTRA_VIDEO_ITEM_ID, new String[]{ Integer.toString(mDataList.indexOf(item))});
			intent.putExtra(Def.EXTRA_SAVE_ITEM_URL, new String[]{item.getUrl()});
			intent.putExtra(Def.EXTRA_SAVE_ITEM_NAME, new String[]{item.getName()});
			intent.setClass(getApplicationContext(), DvrInfoService.class);
			startService(intent);
		}
	};
	private OnClickListener mOnSaveToPhoneClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mProgressView.setVisibility(View.VISIBLE);
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            }
			RecordingItem item = getCurrentVideoItem();
			Intent intent = new Intent();
			intent.setAction(Def.ACTION_SAVE_TO_PHONE);
			intent.putExtra(Def.EXTRA_VIDEO_ITEM_ID, new String[]{ Integer.toString(mDataList.indexOf(item))});
			intent.putExtra(Def.EXTRA_SAVE_ITEM_URL, new String[]{item.getUrl()});
			intent.putExtra(Def.EXTRA_SAVE_ITEM_NAME, new String[]{item.getName()});
			intent.setClass(getApplicationContext(), DvrInfoService.class);
			startService(intent);
		}
	};
	private RecordingItem mCurrentVideoItem;
    
    private void startUITimer() {
        mUpdateUITimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        };
        mUpdateUITimer.schedule(task, 0, 50);
    }

    private void setDuration(){
        mDuration = mVideoView.getDuration();
        mVideoEndTime.setText(getProgressString(mDuration));
    }

    private String getProgressString(int duration) {
        duration /= 1000;
        int minutes = (duration / 60);
        int seconds = duration - (minutes * 60) ;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void updateUI() {
        int current = mVideoView.getCurrentPosition();
        int progress = current * 100 / mDuration;
        mSeekBar.setProgress(progress);
        //mVideoStartTime.setText(getProgressString(current));
    }

    private void setupVideoView() {
    	stopUITimer();
        RecordingItem item = getCurrentVideoItem();
        Uri uri = Uri.parse(item.getUrl());
        //Uri uri = Uri.parse("android.resource://"+getActivity().getPackageName()+"/"+R.raw.rtrs);
        mVideoView.setVideoURI(uri);
        mVideoView.start();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                setDuration();
                startUITimer();
                mProgressView.setVisibility(View.GONE);
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                isComplete = true;
                mSeekBar.setProgress(100);
                stopUITimer();
            }

        });
    }

    private void stopUITimer() {
    	if (mUpdateUITimer != null) {
    		mUpdateUITimer.cancel();
    	}
    }

    private void showVideoControl(boolean isShow) {
        if (isShow) {
            mViewControlGroup.setVisibility(View.VISIBLE);
        } else {
            mViewControlGroup.setVisibility(View.INVISIBLE);
        }
    }


    private void nextVideo() {
    	mProgressView.setVisibility(View.VISIBLE);
        RecordingItem item = getCurrentVideoItem();
        int idx = mDataList.indexOf(item);
        if (idx == mDataList.size() -1) {
        	idx = 0;
        } else {
        	idx++;
        }
        item = mDataList.get(idx);
        setCurrentVideoItem(item);
        setupVideoView();
    }

    private void previousVideo() {
    	mProgressView.setVisibility(View.VISIBLE);
        RecordingItem item = getCurrentVideoItem();
        int idx = mDataList.indexOf(item);
        if (idx == 0) {
            idx = mDataList.size() -1;
        } else {
            idx--;
        }
        item = mDataList.get(idx);
        setCurrentVideoItem(item);
        setupVideoView();
    }
    
    private RecordingItem getCurrentVideoItem() {
    	return mCurrentVideoItem;
    }
    private void setCurrentVideoItem(RecordingItem item) {
    	mCurrentVideoItem = item;
	}

	private void getRecordingList() {
        SharedPreferences sp = getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
        String json = sp.getString(Def.SP_RECORDING_LIST, "");
        Type typeOfList = new TypeToken<List<RecordingItem>>() { }.getType();
        Gson gson = new GsonBuilder().create();
        List<RecordingItem> tempList = gson.fromJson(json, typeOfList);
        if (tempList != null) {
        	mDataList.clear();
        	mDataList.addAll(tempList);
        } else {
        	RecordingItem item = new RecordingItem("http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_30mb.mp4",
        											"big_buck_bunny_720p_30mb.mp4",
        											"2017/04/27", 
        											"30mb");
        	mDataList.add(item);
        											
        }
    }

    @Override
	protected void onResume() {
		super.onResume();
		mBackToRecords.setVisibility(View.VISIBLE);
		mRecordings.setSelected(true);
		mSelectAll.setVisibility(View.GONE);
        mSaveToOTG.setEnabled(false);
        mDelete.setEnabled(false);
    	mShowingMenu = true;
    	setMenuVisible(true);
    	getRecordingList();
    	Intent intent = getIntent();
    	int position = intent.getIntExtra(Def.EXTRA_VIDEO_ITEM_ID, 0);
    	setCurrentVideoItem(mDataList.get(position));
    	setupVideoView();
        IntentFilter intentFilter = new IntentFilter(Def.ACTION_SAVE_TO_PHONE_STATUS);
        intentFilter.addAction(Def.ACTION_SAVE_TO_OTG_STATUS);
        registerReceiver(mBroadcastReceiver, intentFilter);
		registerOTGEvent();
	}

    @Override
    protected void onStart() {
    	super.onStart();
    	mHandlerTime.postDelayed(HideUIControl, 1500);
    	//showDialog(true,true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        unregisterOTGEvent();
    }
    private void setMenuVisible(boolean show) {
    	if (show) {
    		mToolbar.setVisibility(View.VISIBLE);
    		mBottomBar.setVisibility(View.VISIBLE);
    	} else {
    		mToolbar.setVisibility(View.INVISIBLE);
    		mBottomBar.setVisibility(View.INVISIBLE);
    	}
    }
    private void toggleMenu() {
    	if (mShowingMenu) {
    		setMenuVisible(false);
    		showVideoControl(false);
    		mHandlerTime.removeCallbacks(HideUIControl);
    	} else {
    		setMenuVisible(true);
    		showVideoControl(true);
    		mHandlerTime.postDelayed(HideUIControl, 1500);
    	}
    	mShowingMenu = !mShowingMenu;
    }
    
    public Runnable HideUIControl = new Runnable()
    {
        public void run() {

            if (mShowingMenu) {
                mBottomBar.setVisibility(View.GONE);

                Animation animToolbar = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toolbar_hide);
                animToolbar.setAnimationListener(mToolbarAnimation);
                mToolbar.startAnimation(animToolbar);


                Animation animBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_hide);
                animBottom.setAnimationListener(mBottomBarAnimation);
                mBottomBar.startAnimation(animBottom);                
            }
        }
    };
    
    private Animation.AnimationListener mToolbarAnimation = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        	mToolbar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mBottomBarAnimation = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
        	mBottomBar.setVisibility(View.INVISIBLE);
        	mShowingMenu = false;
        	showVideoControl(false);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
        public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			boolean isSaveToPhone = false;
			if (TextUtils.equals(action, Def.ACTION_SAVE_TO_PHONE_STATUS)) {
				isSaveToPhone = true;
			}
			if (TextUtils.equals(action, Def.ACTION_SAVE_TO_PHONE_STATUS) ||
					TextUtils.equals(action, Def.ACTION_SAVE_TO_PHONE_STATUS)) {
				mProgressView.setVisibility(View.GONE);
				String [] ids = intent.getStringArrayExtra(Def.EXTRA_VIDEO_ITEM_ID);
				String [] path = intent.getStringArrayExtra(Def.EXTRA_SAVE_STATUS_FILE_PATH);
				boolean isSuccess = intent.getBooleanExtra(Def.EXTRA_SAVE_STATUS, false);
				showDialog(isSuccess, isSaveToPhone);
				boolean [] status = intent.getBooleanArrayExtra(Def.EXTRA_SAVE_STATUS_ARY);
				for (int i = 0; i < status.length; i++) {
					if (status[i]) {
						RecordingItem item = mDataList.get(Integer.parseInt(ids[i]));
						if (TextUtils.equals(action, Def.ACTION_SAVE_TO_PHONE_STATUS)) {
							item.setLocalPath(path[i]);
						} else {
							item.setOtgPath(path[i]);
						}
					}
				}
			}
        }
    };
    
    private void showDialog(boolean isSuccess, boolean isSaveToPhone) {
    	StatusDialog dialog;
		if (isSuccess) {
			if (isSaveToPhone)
				dialog = new StatusDialog(VideoPlay.this, "Save it to Phone", isSuccess);
			else 
				dialog = new StatusDialog(VideoPlay.this, "Save it to OTG", isSuccess);
		} else {
			dialog = new StatusDialog(VideoPlay.this, "Save Failed!", isSuccess);
		}
		dialog.show();
    }
}
