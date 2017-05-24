package com.liteon.iview.settings;

import java.util.HashMap;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.liteon.iview.R;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;

public class WifiSetting extends Fragment {
	
	private String mSsid;
    private String mBssid;
    private String mSecurityMode;
    private String mEncryptType;
    private String mPassPhase;
    private String currentSsid;
    private String currentSecurityMode;
    private String currentEncryptType;
    private String currentPassPhase;
    private Map<String, TextView> security_map = new HashMap<String, TextView>();

    private EditText mEditTextSSid;
    private EditText mEditTextBssid;
    private EditText mEditTextPassPhase;
    private TextView mTextViewTKIP;
    private TextView mTextViewAES;
    private TextView mTextViewTKIPAES;
    private ImageView mConfirm;
    private IntentFilter mIntenFilter;
    private View mProgressView;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_wifi_setting, container, false);
        findViews(view);
        setupListeners();
		return view;
	}
	
	private void setupListeners() {
        mEditTextSSid.addTextChangedListener(mSSIDTextWater);
        mEditTextPassPhase.addTextChangedListener(mPassPhaseTextWater);
        mTextViewTKIP.setOnClickListener(mOnSecurityOptionClick);
        mTextViewAES.setOnClickListener(mOnSecurityOptionClick);
        mTextViewTKIPAES.setOnClickListener(mOnSecurityOptionClick);
    }
    private void findViews(View view) {
        mEditTextSSid = (EditText) view.findViewById(R.id.edit_ssid);
        mEditTextBssid = (EditText) view.findViewById(R.id.edit_bssid);
        mEditTextPassPhase = (EditText) view.findViewById(R.id.edit_pass_phase);
        mTextViewTKIP = (TextView) view.findViewById(R.id.tkip);
        mTextViewAES = (TextView) view.findViewById(R.id.aes);
        mTextViewTKIPAES = (TextView) view.findViewById(R.id.tkip_aes);
        security_map.put("TKIP", mTextViewTKIP);
        security_map.put("AES", mTextViewAES);
        security_map.put("TKIPAES", mTextViewTKIPAES);
        mConfirm = (ImageView) getActivity().findViewById(R.id.confirm_icon);
        mProgressView = (View) getActivity().findViewById(R.id.progress_view);
    }

    private TextWatcher mSSIDTextWater = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentSsid = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mPassPhaseTextWater = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentPassPhase = s.toString();
            isSettingChanged();
        }
    };

    private View.OnClickListener mOnSecurityOptionClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.isSelected()) {
                v.setSelected(false);
                currentSecurityMode = "OPEN";
                currentEncryptType = "NONE";
            } else {
                for (TextView tv : security_map.values()) {
                    tv.setSelected(false);
                }
                v.setSelected(true);
                currentSecurityMode = "WPAPSK";
                switch (v.getId()) {
                    case R.id.tkip:
                        currentEncryptType = "TKIP";
                        break;
                    case R.id.aes:
                        currentEncryptType = "AES";
                        break;
                    case R.id.tkip_aes:
                        currentEncryptType = "TKIPAES";
                        break;
                }
            }

            isSettingChanged();
        }
    };

    private void isSettingChanged() {
        if (TextUtils.equals(mSsid,currentSsid) &&
                TextUtils.equals(mPassPhase,currentPassPhase) &&
                TextUtils.equals(mSecurityMode,currentSecurityMode) &&
                TextUtils.equals(mEncryptType, currentEncryptType)) {
            mConfirm.setEnabled(false);
        } else {
            mConfirm.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastRecevier();
        //get basic wifi setting
        Intent intent = new Intent(Def.ACTION_GET_SECURITY);
        intent.setClass(getActivity(), DvrInfoService.class);
        getActivity().startService(intent);
        //get security wifi setting
        intent = new Intent(Def.ACTION_GET_WIRELESS);
        intent.setClass(getActivity(), DvrInfoService.class);
        getActivity().startService(intent);
        mProgressView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	unregisterBroadcastReceiver();
    }
	public String getCurrentSsid() {
        return currentSsid;
    }

    public String getCurrentSecurityMode() {
        return currentSecurityMode;
    }

    public String getCurrentEncryptType() {
        return currentEncryptType;
    }

    public String getCurrentPassPhase() {
        return currentPassPhase;
    }

    private void registerBroadcastRecevier() {
    	Activity activity = getActivity();
    	if (mIntenFilter == null) {
    		mIntenFilter = new IntentFilter(Def.ACTION_GET_SECURITY);
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
	        mSsid = sp.getString(Def.SP_SSID, "SSID");
	        mBssid = sp.getString(Def.SP_BSSID, "BSSID");
	        mSecurityMode = sp.getString(Def.SP_SECURITY, "OPEN");
	        mEncryptType = sp.getString(Def.SP_ENCRYPTTYPE, "NONE");
	        mPassPhase = sp.getString(Def.SP_PASSPHASE, "");
	        //Toast.makeText(getActivity(), "SSID " + mSsid + ", BSSID " + mBssid + ", Security mode " + mSecurityMode + ", Encrypt Type " + mEncryptType, Toast.LENGTH_LONG).show();
	        mEditTextSSid.setText(mSsid);
	        mEditTextBssid.setText(mBssid);
	        mEditTextPassPhase.setText(mPassPhase);
	        TextView encrypt = security_map.get(mEncryptType);
	        if (encrypt != null) {
	            encrypt.setSelected(true);
	        }
	        mConfirm.setEnabled(false);
        }
    };
    
}
