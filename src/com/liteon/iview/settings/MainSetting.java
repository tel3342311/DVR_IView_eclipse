package com.liteon.iview.settings;

import com.liteon.iview.R;
import com.liteon.iview.Settings;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MainSetting extends Fragment {
	
	//Container Activity must implement this interface
    public interface OnSettingPageSelectedListener {
        public void onSettingSelected(int position);
    }
    
	OnSettingPageSelectedListener mCallback;
	
    private ImageView mTimeZone;
    private ImageView mRecording;
    private ImageView mInternet;
    private ImageView mVpn;
    private ImageView mWifi;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setting, container, false);
		findViews(view);
		setListener();
        return view;
    }
	
	public void setOnSettingPageSelectedListener(OnSettingPageSelectedListener callback) {
		mCallback = callback;
	}
	
	void findViews(View rootView) {
        mTimeZone = (ImageView) rootView.findViewById(R.id.time_zone);
        mRecording = (ImageView) rootView.findViewById(R.id.recording);
        mInternet = (ImageView) rootView.findViewById(R.id.internet);
        mVpn = (ImageView) rootView.findViewById(R.id.vpn);
        mWifi = (ImageView) rootView.findViewById(R.id.wifi);
	}
	
	void setListener() {
        mTimeZone.setOnClickListener(mOnSettingClickListener);
        mRecording.setOnClickListener(mOnSettingClickListener);
        mInternet.setOnClickListener(mOnSettingClickListener);
        mVpn.setOnClickListener(mOnSettingClickListener);
        mWifi.setOnClickListener(mOnSettingClickListener);
	}
	private View.OnClickListener mOnSettingClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.time_zone:
                    mCallback.onSettingSelected(1);
                    break;
                case R.id.recording:
                    mCallback.onSettingSelected(2);
                    break;
                case R.id.internet:
                    mCallback.onSettingSelected(3);
                    break;
                case R.id.vpn:
                    mCallback.onSettingSelected(4);
                    break;
                case R.id.wifi:
                    mCallback.onSettingSelected(5);
                    break;
            }
        }
    };
}
