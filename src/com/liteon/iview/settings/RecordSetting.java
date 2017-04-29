package com.liteon.iview.settings;

import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.liteon.iview.R;
import com.liteon.iview.util.Def;

public class RecordSetting extends Fragment {

	private String mRecordingLength;
    private String mRecordingChannel;
    private String mPreviewChannel;
    private TextView mTextView2m;
    private TextView mTextView3m;
    private TextView mTextView5m;
    private TextView mTextViewFront;
    private TextView mTextViewRear;
    private TextView mTextViewFrontRear;
    private ImageView mConfirm;
    private String currentRecordingLength;
    private String currentRecordingChannel;
    private Map<String, TextView> lenght_map = new HashMap<String, TextView>();
    private Map<String, TextView> camera_map = new HashMap<String, TextView>();
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_recording_setting, container, false);
        findViews(view);
        setListeners();
        return view;
	}
	
    private void findViews(View rootView) {
        mConfirm = (ImageView) getActivity().findViewById(R.id.confirm_icon);
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

    }

    private void setListeners() {
        mTextView2m.setOnClickListener(mOnRecordingLenghtClickListener);
        mTextView3m.setOnClickListener(mOnRecordingLenghtClickListener);
        mTextView5m.setOnClickListener(mOnRecordingLenghtClickListener);

        mTextViewFront.setOnClickListener(mOnCameraClickListener);
        mTextViewRear.setOnClickListener(mOnCameraClickListener);
        mTextViewFrontRear.setOnClickListener(mOnCameraClickListener);
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

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getActivity().getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
        mRecordingLength = sp.getString(Def.SP_RECORDING_LENGTH, "2m");
        mRecordingChannel = sp.getString(Def.SP_RECORDING_CAMERA, "chab");
        mPreviewChannel = sp.getString(Def.SP_PREVIEW_CAMERA, "cha");
        //Toast.makeText(getContext(), "mRecordingLength " + mRecordingLength + ", mRecordingChannel " + mRecordingChannel + ", mPreviewChannel " + mPreviewChannel, Toast.LENGTH_LONG).show();
        //setup default value
        lenght_map.get(mRecordingLength).setSelected(true);
        camera_map.get(mRecordingChannel).setSelected(true);
        currentRecordingLength = mRecordingLength;
        currentRecordingChannel = mRecordingChannel;
    }

    private void isSettingchanged() {
        if (camera_map.get(mRecordingChannel).isSelected() &&
                lenght_map.get(mRecordingLength).isSelected()) {
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

}
