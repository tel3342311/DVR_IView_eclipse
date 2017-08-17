package com.liteon.iview.settings;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liteon.iview.R;
import com.liteon.iview.Settings;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.StatusDialog;

public class WifiSetting extends Fragment implements OnConfirmListener {
	
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
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private final static long PROGRESS_TIME = 1500;
	private Handler mHandlerTime;
	private long mStartTime;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_wifi_setting, container, false);
        findViews(view);
        setupListeners();
        mHandlerTime = new Handler();
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
        mProgressBar = (ProgressBar) mProgressView.findViewById(R.id.progressBar);
        mProgressText = (TextView) mProgressView.findViewById(R.id.progress_text);
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
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	unregisterBroadcastReceiver();
    	mHandlerTime.removeCallbacks(UpdateProgress);
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
    		mIntenFilter.addAction(Def.ACTION_SET_WIFI);
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
	        if (TextUtils.equals(intent.getAction(), Def.ACTION_GET_SECURITY) ||
	        		TextUtils.equals(intent.getAction(), Def.ACTION_GET_WIRELESS)) {
	        	String message = intent.getStringExtra(Def.EXTRA_ERROR);
		        StatusDialog dialog;
		        if (!TextUtils.isEmpty(message)) {
		        	dialog = new StatusDialog(getActivity(), message, false);
		        	dialog.setOnDismissListener(mOnDismissListener);
		        	dialog.show();
		        }
				SharedPreferences sp = getActivity().getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
				mSsid = sp.getString(Def.SP_SSID, "SSID");
				mBssid = sp.getString(Def.SP_BSSID, "BSSID");
				mSecurityMode = sp.getString(Def.SP_SECURITY, "OPEN");
				mEncryptType = sp.getString(Def.SP_ENCRYPTTYPE, "NONE");
				mPassPhase = sp.getString(Def.SP_PASSPHASE, "");
				// Toast.makeText(getActivity(), "SSID " + mSsid + ", BSSID " +
				// mBssid + ", Security mode " + mSecurityMode + ", Encrypt Type
				// " + mEncryptType, Toast.LENGTH_LONG).show();
				mEditTextSSid.setText(mSsid);
				mEditTextBssid.setText(mBssid);
				mEditTextPassPhase.setText(mPassPhase);
				TextView encrypt = security_map.get(mEncryptType);
				if (encrypt != null) {
					encrypt.setSelected(true);
				}
				mConfirm.setEnabled(false);
	        } else if (TextUtils.equals(intent.getAction(), Def.ACTION_SET_WIFI)) {
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

	@Override
	public void onConfirmSetting() {
		Intent intent = new Intent();
		String ssid = getCurrentSsid();
        String securityMode = getCurrentSecurityMode();
        String encryptType = getCurrentEncryptType();
        String passPhase = getCurrentPassPhase();
        intent.setAction(Def.ACTION_SET_WIFI);
        intent.putExtra(Def.EXTRA_SSID, ssid);
        intent.putExtra(Def.EXTRA_SECURITYMODE, securityMode);
        intent.putExtra(Def.EXTRA_ENCRYPTTYPE, encryptType);
        intent.putExtra(Def.EXTRA_PASSPHASE, passPhase);	
        intent.setClass(getActivity(), DvrInfoService.class);
        getActivity().startService(intent);		
        mStartTime = System.currentTimeMillis();
        mProgressBar.setProgress(0);
        mProgressView.setVisibility(View.VISIBLE);
        mHandlerTime.postDelayed(UpdateProgress, 300);
        mProgressText.setText("Update Settings...");
	}
    
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
