package com.liteon.iview.settings;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.liteon.iview.R;
import com.liteon.iview.Settings;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.StatusDialog;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract.CalendarAlerts;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RecordSetting extends Fragment implements OnConfirmListener {

	private String mRecordingLength;
    private String mRecordingChannel;
    private String mPreviewChannel;
    private String mRecordingOutput;
    private TextView mTextView2m;
    private TextView mTextView3m;
    private TextView mTextView5m;
    private TextView mTextViewFront;
    private TextView mTextViewRear;
    private TextView mTextViewFrontRear;
    private ImageView mConfirm;
    private String currentRecordingLength;
    private String currentRecordingChannel;
    private String currentRecordingOutput;
    private Map<String, TextView> lenght_map = new HashMap<String, TextView>();
    private Map<String, TextView> camera_map = new HashMap<String, TextView>();
    private Map<String, TextView> videoOut_map = new HashMap<String, TextView>();
    private IntentFilter mIntenFilter;
    private View mProgressView;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    //For Demo
    private TextView mTextHdmi;
    private TextView mTextUvc;
    private TextView mTextBoth;
    private final static long PROGRESS_TIME = 4000;
	private Handler mHandlerTime;
	private long mStartTime;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_recording_setting, container, false);
        findViews(view);
        setListeners();
		mHandlerTime = new Handler();
        return view;
	}
	
    private void findViews(View rootView) {
        mConfirm = (ImageView) getActivity().findViewById(R.id.confirm_icon);
        mProgressView = (View) getActivity().findViewById(R.id.progress_view);
        mTextView2m = (TextView) rootView.findViewById(R.id.two_min);
        mTextView3m = (TextView) rootView.findViewById(R.id.three_min);
        mTextView5m = (TextView) rootView.findViewById(R.id.five_min);
        lenght_map.put("2m", mTextView2m);
        lenght_map.put("3m", mTextView3m);
        lenght_map.put("5m", mTextView5m);

        mTextViewFront = (TextView) rootView.findViewById(R.id.front_cam);
        mTextViewRear = (TextView) rootView.findViewById(R.id.rear_cam);
        mTextViewFrontRear = (TextView) rootView.findViewById(R.id.front_rear_cam);
        camera_map.put("cha", mTextViewFront);
        camera_map.put("chb", mTextViewRear);
        camera_map.put("chab", mTextViewFrontRear);

        mTextHdmi = (TextView) rootView.findViewById(R.id.hdmi_out);
        mTextUvc = (TextView) rootView.findViewById(R.id.uvc_out);
        mTextBoth = (TextView) rootView.findViewById(R.id.both_out);
        videoOut_map.put("hdmi", mTextHdmi);
        videoOut_map.put("uvc", mTextUvc);
        videoOut_map.put("both", mTextBoth);
        mProgressBar = (ProgressBar) mProgressView.findViewById(R.id.progressBar);
        mProgressText = (TextView) mProgressView.findViewById(R.id.progress_text);
    }

    private void setListeners() {
        mTextView2m.setOnClickListener(mOnRecordingLenghtClickListener);
        mTextView3m.setOnClickListener(mOnRecordingLenghtClickListener);
        mTextView5m.setOnClickListener(mOnRecordingLenghtClickListener);

        mTextViewFront.setOnClickListener(mOnCameraClickListener);
        mTextViewRear.setOnClickListener(mOnCameraClickListener);
        mTextViewFrontRear.setOnClickListener(mOnCameraClickListener);
        
        mTextHdmi.setOnClickListener(mOnVideoOutClickListener);
        mTextUvc.setOnClickListener(mOnVideoOutClickListener);
        mTextBoth.setOnClickListener(mOnVideoOutClickListener);
        
    }
    
    private View.OnClickListener mOnRecordingLenghtClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            for (TextView tv : lenght_map.values()) {
                tv.setSelected(false);
            }

            v.setSelected(true);

            switch (v.getId()) {
                case R.id.two_min:
                    currentRecordingLength = "2m";
                    break;
                case R.id.three_min:
                    currentRecordingLength = "3m";
                    break;
                case R.id.five_min:
                    currentRecordingLength = "5m";
                    break;
            }
            isSettingchanged();
        }
    };

    private View.OnClickListener mOnCameraClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            for (TextView tv : camera_map.values()) {
                tv.setSelected(false);
            }

            v.setSelected(true);

            switch (v.getId()) {
                case R.id.front_cam:
                    currentRecordingChannel = "cha";
                    break;
                case R.id.rear_cam:
                    currentRecordingChannel = "chb";
                    break;
                case R.id.front_rear_cam:
                    currentRecordingChannel = "chab";
                    break;
            }
            isSettingchanged();
        }
    };

    private View.OnClickListener mOnVideoOutClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            for (TextView tv : videoOut_map.values()) {
                tv.setSelected(false);
            }

            v.setSelected(true);

            switch (v.getId()) {
                case R.id.hdmi_out:
                	currentRecordingOutput = "hdmi";
                    break;
                case R.id.uvc_out:
                	currentRecordingOutput = "uvc";
                    break;
                case R.id.both_out:
                	currentRecordingOutput = "both";
                    break;
            }
            isSettingchanged();
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastRecevier();
        Intent intent = new Intent(Def.ACTION_GET_RECORDINGS);
        intent.setClass(getActivity(), DvrInfoService.class);
        getActivity().startService(intent);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	unregisterBroadcastReceiver();
    	mHandlerTime.removeCallbacks(UpdateProgress);
    }

    private void isSettingchanged() {
        if (camera_map.get(mRecordingChannel).isSelected() &&
                lenght_map.get(mRecordingLength).isSelected() && 
                videoOut_map.get(mRecordingOutput).isSelected()) {
            mConfirm.setEnabled(false);
        } else {
            mConfirm.setEnabled(true);
        }
    }

    public String getRecordingLength() {
        return currentRecordingLength;
    }

    public String getRecordingChannel() {
        return currentRecordingChannel;
    }
    
    public String getRecordingOutput() {
    	return currentRecordingOutput;
    }
    
    private void registerBroadcastRecevier() {
    	Activity activity = getActivity();
    	if (mIntenFilter == null) {
    		mIntenFilter = new IntentFilter(Def.ACTION_GET_RECORDINGS);
    		mIntenFilter.addAction(Def.ACTION_SET_RECORDINGS);
    	}
    	activity.registerReceiver(mBroadcastReceiver, mIntenFilter);
    }
    
    private void unregisterBroadcastReceiver() {
    	Activity activity = getActivity();
    	activity.unregisterReceiver(mBroadcastReceiver);
    }
    
    @Override
    public void onConfirmSetting() {
    	Intent intent = new Intent();
    	String recordingLength = getRecordingLength();
        String recordingChannel = getRecordingChannel();
        String recordingOutput = getRecordingOutput();
        intent.setAction(Def.ACTION_SET_RECORDINGS);
        intent.putExtra(Def.EXTRA_RECORDING_LENGTH, recordingLength);
        intent.putExtra(Def.EXTRA_RECORDING_CHANNEL, recordingChannel);
        intent.putExtra(Def.EXTRA_RECORDING_OUTPUT, recordingOutput);
        intent.setClass(getActivity(), DvrInfoService.class);
        getActivity().startService(intent);
        mStartTime = System.currentTimeMillis();
        mProgressBar.setProgress(0);
        mProgressView.setVisibility(View.VISIBLE);
        mHandlerTime.postDelayed(UpdateProgress, 300);
        mProgressText.setText("Update Settings...");
    }
    
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
        public void onReceive(Context context, Intent intent) {
			mHandlerTime.removeCallbacks(UpdateProgress);
			mProgressBar.setProgress(100);
	        mProgressView.setVisibility(View.GONE);
	        if (TextUtils.equals(intent.getAction(), Def.ACTION_GET_RECORDINGS)) {
		        String message = intent.getStringExtra(Def.EXTRA_ERROR);
		        StatusDialog dialog;
		        if (!TextUtils.isEmpty(message)) {
		        	dialog = new StatusDialog(getActivity(), message, false);
		        	dialog.setOnDismissListener(mOnDismissListener);
		        	dialog.show();
		        }
		        SharedPreferences sp = getActivity().getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
		        mRecordingLength = sp.getString(Def.SP_RECORDING_LENGTH, "");//"2m");
		        mRecordingChannel = sp.getString(Def.SP_RECORDING_CAMERA, "");//"chab");
		        mPreviewChannel = sp.getString(Def.SP_PREVIEW_CAMERA, "");//"cha");
		        mRecordingOutput = sp.getString(Def.SP_RECORDING_OUTPUT, "");//"hdmi");
		        //Toast.makeText(getContext(), "mRecordingLength " + mRecordingLength + ", mRecordingChannel " + mRecordingChannel + ", mPreviewChannel " + mPreviewChannel, Toast.LENGTH_LONG).show();
		        
		        if (TextUtils.isEmpty(mRecordingChannel) || 
		        		TextUtils.isEmpty(mRecordingLength) || 
		        		TextUtils.isEmpty(mPreviewChannel) || 
		        		TextUtils.isEmpty(mRecordingOutput)) {
		        	dialog = new StatusDialog(getActivity(), "Failed to get current setting", false);
		        	dialog.show();
		        	return;
		        }
		        //setup default value
		        lenght_map.get(mRecordingLength).setSelected(true);
		        camera_map.get(mRecordingChannel).setSelected(true);
		        videoOut_map.get(mRecordingOutput).setSelected(true);
		        currentRecordingLength = mRecordingLength;
		        currentRecordingChannel = mRecordingChannel;
		        currentRecordingOutput = mRecordingOutput;
		        mConfirm.setEnabled(false);
	        } else if (TextUtils.equals(intent.getAction(), Def.ACTION_SET_RECORDINGS)) {
	        	String message = intent.getStringExtra(Def.EXTRA_ERROR);
		        StatusDialog dialog;
		        if (!TextUtils.isEmpty(message)) {
		        	dialog = new StatusDialog(getActivity(), message, false);
		        	dialog.setOnDismissListener(mOnDismissListener);
		        	dialog.show();
		        } else {
		        	((Settings)getActivity()).onSettingSelected(0);
		        }
	        }
        }
    };
    
    private OnDismissListener mOnDismissListener = new OnDismissListener() {
		
		@Override
		public void onDismiss(DialogInterface dialog) {
        	((Settings)getActivity()).onSettingSelected(0);			
		}
	};
	
	public Runnable UpdateProgress = new Runnable()
    {
        public void run() {
        	long time_elapsed = System.currentTimeMillis() - mStartTime;
        	if (time_elapsed >= PROGRESS_TIME) {
        		mProgressBar.setProgress(100);
        	} else {
        		int progress =  (int) (((float)time_elapsed / PROGRESS_TIME) * 100);
        		mProgressBar.setProgress(progress);
        		Log.d("UpdateProgress", time_elapsed + ",  " + progress + "!");
        		mHandlerTime.postDelayed(UpdateProgress, 30);
        	}
        }
    };
}
