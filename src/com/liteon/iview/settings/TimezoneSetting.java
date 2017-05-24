package com.liteon.iview.settings;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liteon.iview.MainActivity;
import com.liteon.iview.Preview;
import com.liteon.iview.R;
import com.liteon.iview.Records;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.ExtendNumberPicker;

public class TimezoneSetting extends Fragment {
    private Map<String , String> mTimeZoneList;
    private String mTimeZone;
    private String mTimeZoneTitle;
    private String mNTPServer;
    private String mNTPSyncValue;
    private EditText mEdTextNtpServer;
    private ViewGroup mPicker;
    private String currentTimeZone;
    private String currentNTPServer;
    private ImageView mConfirm;
    private ExtendNumberPicker mNumPicker;
    private IntentFilter mIntenFilter;
    private View mProgressView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_time_zone, container, false);
		findViews(view);
        mEdTextNtpServer.addTextChangedListener(mTextWatcher);
		return view;
	}
	
    private void findViews(View rootView) {
        mEdTextNtpServer = (EditText) rootView.findViewById(R.id.edit_ntp_server);
        mPicker = (ViewGroup) rootView.findViewById(R.id.picker_container);
        mConfirm = (ImageView) getActivity().findViewById(R.id.confirm_icon);
        mProgressView = (View) getActivity().findViewById(R.id.progress_view);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastRecevier();
        Intent intent = new Intent(Def.ACTION_GET_ADMIN);
        intent.setClass(getActivity(), DvrInfoService.class);
        getActivity().startService(intent);
        mProgressView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	unregisterBroadcastReceiver();
    }
    
    private void setupPicker() {
    	List<String> list = new ArrayList(mTimeZoneList.keySet());
        mNumPicker = new ExtendNumberPicker(getActivity(), null);
        mNumPicker.setMinValue(0);
        mNumPicker.setMaxValue(list.size() - 1);
        mNumPicker.setDisplayedValues(list.toArray(new String[0]));
        mNumPicker.setValue(list.indexOf(mTimeZoneTitle));
        mNumPicker.setWrapSelectorWheel(false);
        mNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mNumPicker.setOnValueChangedListener(mOnValueChangeListener);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        mPicker.addView(mNumPicker, layoutParams);
    }
    
    private android.widget.NumberPicker.OnValueChangeListener mOnValueChangeListener = new android.widget.NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
        	String timezone = picker.getDisplayedValues()[newVal];
        	Log.i("TimeZone", timezone);
            currentTimeZone = mTimeZoneList.get(timezone);
            isSettingChanged();
        }
    };
    
    private void isSettingChanged() {
        if (TextUtils.equals(currentNTPServer, mNTPServer) &&
                TextUtils.equals(mTimeZone, currentTimeZone)) {
            mConfirm.setEnabled(false);
        } else {
            mConfirm.setEnabled(true);
        }
    }
    
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentNTPServer = s.toString();
            isSettingChanged();
        }
    };

    public String getCurrentTimeZone() {
        return currentTimeZone;
    }

    public String getNTPServer() {
        return currentNTPServer;
    }

    private void registerBroadcastRecevier() {
    	Activity activity = getActivity();
    	if (mIntenFilter == null) {
    		mIntenFilter = new IntentFilter(Def.ACTION_GET_ADMIN);
    	}
    	activity.registerReceiver(mBroadcastReceiver, mIntenFilter);
    }
    
    private void unregisterBroadcastReceiver() {
    	Activity activity = getActivity();
    	activity.unregisterReceiver(mBroadcastReceiver);
    }
    
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
        public void onReceive(Context context, Intent intent) {
	        mProgressView.setVisibility(View.GONE);

	        SharedPreferences sp = getActivity().getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
	        String json = sp.getString(Def.SP_TIMEZONE_LIST, "");
	        Type typeOfHashMap = new TypeToken<Map<String, String>>() { }.getType();
	        Gson gson = new GsonBuilder().create();
	        mTimeZoneList = gson.fromJson(json, typeOfHashMap);
	        if (mTimeZoneList == null) {
	            mTimeZoneList = new HashMap<String,String>();
	            mTimeZoneList.put("UTC_0","UTC_0");
	            mTimeZoneList.put("UTC_1","UTC_1");
	            mTimeZoneList.put("UTC_2","UTC_2");
	        }
	        mTimeZone = sp.getString(Def.SP_TIMEZONE, "UTC_0");
	        mNTPServer = sp.getString(Def.SP_NTPSERVER, "");
	        mNTPSyncValue = sp.getString(Def.SP_NTP_SYNC_VALUE, "");
	        mTimeZoneTitle = "";
	        for(Map.Entry entry: mTimeZoneList.entrySet()){
	            if(mTimeZone.equals(entry.getValue())){
	                mTimeZoneTitle = (String)entry.getKey();
	                break;
	            }
	        }
	        mEdTextNtpServer.setText(mNTPServer);
	        setupPicker();
	        mConfirm.setEnabled(false);
        }
    };
}
