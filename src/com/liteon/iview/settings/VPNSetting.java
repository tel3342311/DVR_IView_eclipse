package com.liteon.iview.settings;

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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liteon.iview.R;
import com.liteon.iview.Settings;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.StatusDialog;

public class VPNSetting extends Fragment implements OnConfirmListener {
	
	private String mPPTPServer;
    private String mPPTPUsername;
    private String mPPTPPassword;
    private String mPPTPClientIP;

    private EditText mEditTextPPTPServer;
    private EditText mEditTextPPTPUsername;
    private EditText mEditTextPPTPPassword;
    private EditText mEditTextPPTPClientIP;
    private ImageView mConfirm;
    private String mCurrentServer;
    private String mCurrentUsername;
    private String mCurrentPassword;
    private String mCurrentClientIP;
    private IntentFilter mIntenFilter;
    private View mProgressView;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private final static long PROGRESS_TIME = 1500;
	private Handler mHandlerTime;
	private long mStartTime;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_vpn_setting, container, false);
        findViews(view);
        setListeners();
        mHandlerTime = new Handler();
        return view;
	}
	
    private void setListeners() {
        mEditTextPPTPServer.addTextChangedListener(mTextWatcherServer);
        mEditTextPPTPUsername.addTextChangedListener(mTextWatcherUsername);
        mEditTextPPTPPassword.addTextChangedListener(mTextWatcherPassword);
        mEditTextPPTPClientIP.addTextChangedListener(mTextWatcherClientIP);
    }

    private void findViews(View view) {
        mEditTextPPTPServer = (EditText) view.findViewById(R.id.edit_server_ip);
        mEditTextPPTPUsername = (EditText) view.findViewById(R.id.edit_username);
        mEditTextPPTPPassword = (EditText) view.findViewById(R.id.edit_password);
        mEditTextPPTPClientIP = (EditText) view.findViewById(R.id.edit_client_ip);
        mConfirm = (ImageView) getActivity().findViewById(R.id.confirm_icon);
        mProgressView = (View) getActivity().findViewById(R.id.progress_view);
        mProgressBar = (ProgressBar) mProgressView.findViewById(R.id.progressBar);
        mProgressText = (TextView) mProgressView.findViewById(R.id.progress_text);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastRecevier();
        Intent intent = new Intent(Def.ACTION_GET_NETWORKING);
        intent.setClass(getActivity(), DvrInfoService.class);
        getActivity().startService(intent);
    }

    @Override
    public void onPause() {
    	super.onPause();
    	unregisterBroadcastReceiver();
    	mHandlerTime.removeCallbacks(UpdateProgress);
    }
    
    private TextWatcher mTextWatcherServer = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mCurrentServer = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mTextWatcherUsername = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mCurrentUsername = s.toString();
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
            mCurrentPassword = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mTextWatcherClientIP = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mCurrentClientIP = s.toString();
            isSettingChanged();
        }
    };

    private void isSettingChanged() {
        if (TextUtils.equals(mCurrentServer, mPPTPServer) &&
                TextUtils.equals(mCurrentUsername, mPPTPUsername) &&
                TextUtils.equals(mCurrentPassword, mPPTPPassword) &&
                TextUtils.equals(mCurrentClientIP, mPPTPClientIP)) {
            mConfirm.setEnabled(false);
        } else {
            mConfirm.setEnabled(true);
        }
    }
	
    public String getCurrentServer() {
        return mCurrentServer;
    }

    public String getCurrentUsername() {
        return mCurrentUsername;
    }

    public String getCurrentPassword() {
        return mCurrentPassword;
    }

    public String getCurrentClientIP() {
        return mCurrentClientIP;
    }
    
    private void registerBroadcastRecevier() {
    	Activity activity = getActivity();
    	if (mIntenFilter == null) {
    		mIntenFilter = new IntentFilter(Def.ACTION_GET_NETWORKING);
    		mIntenFilter.addAction(Def.ACTION_SET_VPN);
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
	        if (TextUtils.equals(intent.getAction(), Def.ACTION_GET_NETWORKING)) {
	        	String message = intent.getStringExtra(Def.EXTRA_ERROR);
		        StatusDialog dialog;
		        if (!TextUtils.isEmpty(message)) {
		        	dialog = new StatusDialog(getActivity(), message, false);
		        	dialog.setOnDismissListener(mOnDismissListener);
		        	dialog.show();
		        }
				SharedPreferences sp = getActivity().getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
				mPPTPServer = sp.getString(Def.SP_PPTPSERVER, "serverIP");
				mPPTPUsername = sp.getString(Def.SP_PPTPUSER, "User name");
				mPPTPPassword = sp.getString(Def.SP_PPTPPASS, "Password");
				// mPPTPClientIP = sp.getString(Def.SP_PPTPCLIENT, "clientIP");
				mPPTPClientIP = sp.getString(Def.SP_VPN_IP, Def.DVR_VPN_IP);
				// Toast.makeText(getActivity(), "serverIP " + mPPTPServer + ",
				// User name " + mPPTPUsername + ", PPTP Password " +
				// mPPTPPassword, Toast.LENGTH_LONG).show();
				mEditTextPPTPServer.setText(mPPTPServer);
				mEditTextPPTPUsername.setText(mPPTPUsername);
				mEditTextPPTPPassword.setText(mPPTPPassword);
				mEditTextPPTPClientIP.setText(mPPTPClientIP);
				mConfirm.setEnabled(false);
	        } else if (TextUtils.equals(intent.getAction(), Def.ACTION_SET_VPN)) {
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
		String pPTPServer = getCurrentServer();
        String pPTPUsername = getCurrentUsername();
        String pPTPPassword = getCurrentPassword();
        String pPTPClientIp = getCurrentClientIP();
        intent.setAction(Def.ACTION_SET_VPN);
        intent.putExtra(Def.EXTRA_PPTP_SERVER, pPTPServer);
        intent.putExtra(Def.EXTRA_PPTP_USERNAME,pPTPUsername);
        intent.putExtra(Def.EXTRA_PPTP_PASSWORD,pPTPPassword);
        intent.putExtra(Def.EXTRA_PPTP_CLIENT_IP, pPTPClientIp);
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
