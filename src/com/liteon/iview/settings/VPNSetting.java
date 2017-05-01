package com.liteon.iview.settings;

import android.app.Fragment;
import android.content.Context;
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

import com.liteon.iview.R;
import com.liteon.iview.util.Def;

public class VPNSetting extends Fragment {
	
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
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_vpn_setting, container, false);
        findViews(view);
        setListeners();
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
    }
    
    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sp = getActivity().getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
        mPPTPServer = sp.getString(Def.SP_PPTPSERVER, "serverIP");
        mPPTPUsername = sp.getString(Def.SP_PPTPUSER, "User name");
        mPPTPPassword = sp.getString(Def.SP_PPTPPASS, "Password");
        mPPTPClientIP = sp.getString(Def.SP_PPTPCLIENT, "clientIP");
        //Toast.makeText(getActivity(), "serverIP " + mPPTPServer + ", User name " + mPPTPUsername + ", PPTP Password " + mPPTPPassword, Toast.LENGTH_LONG).show();
        mEditTextPPTPServer.setText(mPPTPServer);
        mEditTextPPTPUsername.setText(mPPTPUsername);
        mEditTextPPTPPassword.setText(mPPTPPassword);
        mEditTextPPTPClientIP.setText(mPPTPClientIP);
        mConfirm.setEnabled(false);

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
}
