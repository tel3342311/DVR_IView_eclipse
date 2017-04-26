package com.liteon.iview;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.RecordingItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.VideoView;

public class VideoPlay extends Activity {

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
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_play);
		findViews();
		setListeners();
		mHandlerTime = new Handler();
		mDataList = new ArrayList<RecordingItem>();
	}
	
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
			onBackPressed();
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
        	Intent intent = new Intent(getApplicationContext(), Preview.class);
    		startActivity(intent);        
    	}
    };
    
	public View.OnClickListener mOnSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	Intent intent = new Intent(getApplicationContext(), Settings.class);
    		startActivity(intent);        
    	}
    };
    
	private OnClickListener mOnSaveToOTGClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mProgressView.setVisibility(View.VISIBLE);
		}
	};
	private OnClickListener mOnSaveToPhoneClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mProgressView.setVisibility(View.VISIBLE);
			RecordingItem item = getCurrentVideoItem();
			Intent intent = new Intent();
			intent.setAction(Def.ACTION_SAVE_TO_PHONE);
			intent.putExtra(Def.EXTRA_VIDEO_ITEM_ID, mDataList.indexOf(item));
			intent.putExtra(Def.EXTRA_SAVE_ITEM_URL, item.getUrl());
			intent.putExtra(Def.EXTRA_SAVE_ITEM_NAME, item.getName());
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
    	mShowingMenu = true;
    	setMenuVisible(true);
    	getRecordingList();
    	Intent intent = getIntent();
    	int position = intent.getIntExtra(Def.EXTRA_VIDEO_ITEM_ID, 0);
    	setCurrentVideoItem(mDataList.get(position));
    	setupVideoView();
	}
    
    @Override
    protected void onStart() {
    	super.onStart();
    	mHandlerTime.postDelayed(HideUIControl, 1500);
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
}
