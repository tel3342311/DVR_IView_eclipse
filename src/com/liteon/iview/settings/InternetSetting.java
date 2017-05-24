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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liteon.iview.R;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.ExtendNumberPicker;

public class InternetSetting extends Fragment {
	private String mAPN;
    private String mPIN;
    private String mDial_Num;
    private String mUsername;
    private String mPassword;
    private String mModem;
    private String mModemTitle;
    private Map<String, String> mModemList;

    private TextView mTextViewAPN;
    private TextView mTextViewPIN;
    private TextView mTextViewDialNum;
    private TextView mTextViewUserName;
    private TextView mTextViewPassword;
    private ViewGroup mPicker;
    private ImageView mConfirm;
    private View mRootView;
    private String currentAPN;
    private String currentPIN;
    private String currentDialNum;
    private String currentUsername;
    private String currentPassword;
    private String currentModem;
    private ExtendNumberPicker mNumPicker;
    private IntentFilter mIntenFilter;
    private View mProgressView;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_internet, container, false);
		findViews(view);
        setListeners();
        return view;
    }
	
	private void setListeners() {
        mTextViewAPN.addTextChangedListener(mTextWatcherAPN);
        mTextViewPIN.addTextChangedListener(mTextWatcherPIN);
        mTextViewDialNum.addTextChangedListener(mTextWatcherDialNum);
        mTextViewUserName.addTextChangedListener(mTextWatcherUserName);
        mTextViewPassword.addTextChangedListener(mTextWatcherPassword);
    }
	
	private void setupPicker() {
        mNumPicker = new ExtendNumberPicker(getActivity(), null);
    	List<String> list = new ArrayList(mModemList.keySet());
        mNumPicker.setMinValue(0);
        mNumPicker.setMaxValue(list.size() - 1);
        mNumPicker.setDisplayedValues(list.toArray(new String[0]));
        mNumPicker.setValue(list.indexOf(mModemTitle));
        mNumPicker.setWrapSelectorWheel(false);
        mNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mNumPicker.setOnValueChangedListener(mOnValueChangeListener);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        mPicker.addView(mNumPicker);
    }
	
	private void isSettingChanged() {
        if (TextUtils.equals(currentAPN, mAPN) &&
                TextUtils.equals(currentPIN, mPIN) &&
                TextUtils.equals(currentDialNum, mDial_Num) &&
                TextUtils.equals(currentUsername, mUsername) &&
                TextUtils.equals(currentPassword, mPassword) &&
                TextUtils.equals(mModem, currentModem)) {
            mConfirm.setEnabled(false);
        } else {
            mConfirm.setEnabled(true);
        }
    }

    private void findViews(View view) {
        mTextViewAPN = (TextView) view.findViewById(R.id.edit_apn_server);
        mTextViewPIN = (TextView) view.findViewById(R.id.edit_pin);
        mTextViewDialNum = (TextView) view.findViewById(R.id.edit_dial_num_server);
        mTextViewUserName = (TextView) view.findViewById(R.id.edit_username);
        mTextViewPassword = (TextView) view.findViewById(R.id.edit_password);
        mPicker = (ViewGroup) view.findViewById(R.id.picker_container);
        mConfirm = (ImageView) getActivity().findViewById(R.id.confirm_icon);
        mProgressView = (View) getActivity().findViewById(R.id.progress_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastRecevier();
        Intent intent = new Intent(Def.ACTION_GET_NETWORKING);
        intent.setClass(getActivity(), DvrInfoService.class);
        getActivity().startService(intent);
        mProgressView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	unregisterBroadcastReceiver();
    }

    private TextWatcher mTextWatcherAPN = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentAPN = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mTextWatcherPIN = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentPIN = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mTextWatcherDialNum = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentDialNum = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mTextWatcherUserName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentUsername = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mTextWatcherPassword = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentPassword = s.toString();
            isSettingChanged();
        }
    };



    private android.widget.NumberPicker.OnValueChangeListener mOnValueChangeListener = new android.widget.NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
            String modem = picker.getDisplayedValues()[newVal];
            Log.i("Modem", modem);

            currentModem = mModemList.get(modem);
            isSettingChanged();
        }
    };

    public String getCurrentAPN() {
        return currentAPN;
    }

    public String getCurrentPIN() {
        return currentPIN;
    }

    public String getCurrentDialNum() {
        return currentDialNum;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getCurrentModem() {
        return currentModem;
    }

    private void registerBroadcastRecevier() {
    	Activity activity = getActivity();
    	if (mIntenFilter == null) {
    		mIntenFilter = new IntentFilter(Def.ACTION_GET_NETWORKING);
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
	        mAPN = sp.getString(Def.SP_APN3G, "APN");
	        mPIN = sp.getString(Def.SP_PIN3G, "PIN");
	        mDial_Num = sp.getString(Def.SP_USER3G, "User name");
	        mUsername = sp.getString(Def.SP_PASSWORD3G, "Password");
	        mPassword = sp.getString(Def.SP_DIAL3G, "Dial number");
	        mModem = sp.getString(Def.SP_MODEM_NAME, "AUTO");
	        String json = sp.getString(Def.SP_MODEM_LIST_JSON, "");
	        Type typeOfHashMap = new TypeToken<Map<String, String>>() { }.getType();
	        Gson gson = new GsonBuilder().create();
	        mModemList = gson.fromJson(json, typeOfHashMap);
	        if (mModemList == null) {
	            mModemList = new HashMap<>();
	            mModemList.put("AUTO","AUTO");
	            mModemList.put("HTC","HTC");
	            mModemList.put("CHT","CHT");
	        }
	        mModemTitle = "";
	        for(Map.Entry entry: mModemList.entrySet()){
	            if(mModem.equals(entry.getValue())){
	                mModemTitle = (String)entry.getKey();
	                break;
	            }
	        }
	        setupPicker();
	        //Toast.makeText(getContext(), "APN " + mAPN + ", PIN " + mPIN + ", Dial_Num " + mDial_Num + ", User Name " + mUsername + ", Password " + mPassword + " modem list " + mModemList.toString(), Toast.LENGTH_LONG).show();
	        //Set Default value
	        mTextViewAPN.setText(mAPN);
	        mTextViewPIN.setText(mPIN);
	        mTextViewDialNum.setText(mDial_Num);
	        mTextViewUserName.setText(mUsername);
	        mTextViewPassword.setText(mPassword);
        }
    };
}
