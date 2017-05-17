package com.liteon.iview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.settings.InternetSetting;
import com.liteon.iview.settings.MainSetting;
import com.liteon.iview.settings.MainSetting.OnSettingPageSelectedListener;
import com.liteon.iview.settings.RecordSetting;
import com.liteon.iview.settings.TimezoneSetting;
import com.liteon.iview.settings.VPNSetting;
import com.liteon.iview.settings.WifiSetting;
import com.liteon.iview.util.Def;

public class Settings extends Activity implements OnSettingPageSelectedListener{

    private View mToolbar;
    private View mBottomBar;
    private ImageView mRecordings;
    private ImageView mSettings;
    private ImageView mPreview;
    private TextView mTitleView;
    private ImageView mConfirm;
    private ImageView mCancel;
    private MainSetting mMainSetting;
    private TimezoneSetting mTimezoneSetting;
    private RecordSetting mRecordSetting;
    private InternetSetting mInternetSetting;
    private VPNSetting mVpnSetting;
    private WifiSetting mWifiSetting;
    
    private static final int PAGE_COUNT = 6;
    private static final int SETTING_MAIN = 0;
    private static final int SETTING_TIMEZONE = 1;
    private static final int SETTING_RECORDINGS = 2;
    private static final int SETTING_INTERNET = 3;
    private static final int SETTING_VPN = 4;
    private static final int SETTING_WIFI = 5;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		findViews();
		setListeners();
	}

	@Override
	protected void onResume(){
		super.onResume();
		onSettingSelected(SETTING_MAIN);
		mSettings.setSelected(true);
	}
	
	private void setListeners() {

		mCancel.setOnClickListener(mOnCancelClickListener);
		mConfirm.setOnClickListener(mOnConfirmClickListener);
		mPreview.setOnClickListener(mOnPreviewClickListener);
		mRecordings.setOnClickListener(mOnRecordingsClickListener);
	}

	private void findViews() {
        mToolbar = findViewById(R.id.toolbar_preview);
        mBottomBar = findViewById(R.id.bottombar);
        mPreview = (ImageView) findViewById(R.id.preview_icon);
        mRecordings = (ImageView) findViewById(R.id.recordings_icon);
        mSettings = (ImageView) findViewById(R.id.setting_icon);
        mTitleView = (TextView) findViewById(R.id.toolbar_title);
        mConfirm = (ImageView) findViewById(R.id.confirm_icon);
        mCancel = (ImageView) findViewById(R.id.cancel_icon);
	}

	@Override
	public void onSettingSelected(int position) {
		String title = "";
		Fragment fragment = mMainSetting;
		showTitleAction(true);
		switch (position) {
			case SETTING_MAIN:
				title = getResources().getString(R.string.title_activity_settings);
				if (mMainSetting == null) {
					mMainSetting = new MainSetting();
					mMainSetting.setOnSettingPageSelectedListener(Settings.this);
				}
				fragment = mMainSetting;
				showTitleAction(false);
				break;
			case SETTING_TIMEZONE:
				title = getResources().getString(R.string.title_setting_date);
				if (mTimezoneSetting == null) {
					mTimezoneSetting = new TimezoneSetting();
				}
				fragment = mTimezoneSetting;
				break;
			case SETTING_RECORDINGS:
				title = getResources().getString(R.string.title_setting_recording);
				if (mRecordSetting == null) {
					mRecordSetting = new RecordSetting();
				}
				fragment = mRecordSetting;
				break;
			case SETTING_INTERNET:
				title = getResources().getString(R.string.title_setting_internet);
				if (mInternetSetting == null) {
					mInternetSetting = new InternetSetting();
				}
				fragment = mInternetSetting;
				break;
			case SETTING_VPN:
				title = getResources().getString(R.string.title_setting_vpn_routing);
				if (mVpnSetting == null) {
					mVpnSetting = new VPNSetting();
				}
				fragment = mVpnSetting;
				break;
			case SETTING_WIFI:
				title = getResources().getString(R.string.title_setting_wifi_setting);
				if (mWifiSetting == null) {
					mWifiSetting = new WifiSetting();
				}
				fragment = mWifiSetting;
				break;
		};
		updateTitle(title);
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.frag_container, fragment);
		transaction.commit();
	}
	
	private View.OnClickListener mOnCancelClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			onSettingSelected(SETTING_MAIN);
		}
	};
	
	private View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			Fragment fragment = getFragmentManager().findFragmentById(R.id.frag_container);
			if (fragment instanceof TimezoneSetting) {
                String timezone = ((TimezoneSetting)fragment).getCurrentTimeZone();
                String ntpServer = ((TimezoneSetting)fragment).getNTPServer();
                intent.setAction(Def.ACTION_SET_TIMEZONE);
                intent.putExtra(Def.EXTRA_TIMEZONE, timezone);
                intent.putExtra(Def.EXTRA_NTP_SERVER, ntpServer);
            } else if (fragment instanceof RecordSetting) {
                String recordingLength = ((RecordSetting)fragment).getRecordingLength();
                String recordingChannel = ((RecordSetting)fragment).getRecordingChannel();
                intent.setAction(Def.ACTION_SET_RECORDINGS);
                intent.putExtra(Def.EXTRA_RECORDING_LENGTH, recordingLength);
                intent.putExtra(Def.EXTRA_RECORDING_CHANNEL, recordingChannel);
            } else if (fragment instanceof InternetSetting) {
                String apn = ((InternetSetting)fragment).getCurrentAPN();
                String pin = ((InternetSetting)fragment).getCurrentPIN();
                String dial_Num = ((InternetSetting)fragment).getCurrentDialNum();
                String username = ((InternetSetting)fragment).getCurrentUsername();
                String password = ((InternetSetting)fragment).getCurrentPassword();
                String modem = ((InternetSetting)fragment).getCurrentModem();
                intent.setAction(Def.ACTION_SET_INTERNET);
                intent.putExtra(Def.EXTRA_APN, apn);
                intent.putExtra(Def.EXTRA_PIN, pin);
                intent.putExtra(Def.EXTRA_DIAL_NUM, dial_Num);
                intent.putExtra(Def.EXTRA_USERNAME_3G, username);
                intent.putExtra(Def.EXTRA_PASSWORD_3G, password);
                intent.putExtra(Def.EXTRA_MODEM, modem);
            } else if (fragment instanceof VPNSetting) {
                String pPTPServer = ((VPNSetting)fragment).getCurrentServer();
                String pPTPUsername = ((VPNSetting)fragment).getCurrentUsername();
                String pPTPPassword = ((VPNSetting)fragment).getCurrentPassword();
                intent.setAction(Def.ACTION_SET_VPN);
                intent.putExtra(Def.EXTRA_PPTP_SERVER, pPTPServer);
                intent.putExtra(Def.EXTRA_PPTP_USERNAME,pPTPUsername);
                intent.putExtra(Def.EXTRA_PPTP_PASSWORD,pPTPPassword);
            } else if (fragment instanceof WifiSetting) {
                String ssid = ((WifiSetting)fragment).getCurrentSsid();
                String securityMode = ((WifiSetting)fragment).getCurrentSecurityMode();
                String encryptType = ((WifiSetting)fragment).getCurrentEncryptType();
                String passPhase = ((WifiSetting)fragment).getCurrentPassPhase();
                intent.setAction(Def.ACTION_SET_WIFI);
                intent.putExtra(Def.EXTRA_SSID, ssid);
                intent.putExtra(Def.EXTRA_SECURITYMODE, securityMode);
                intent.putExtra(Def.EXTRA_ENCRYPTTYPE, encryptType);
                intent.putExtra(Def.EXTRA_PASSPHASE, passPhase);
            }
            intent.setClass(Settings.this, DvrInfoService.class);
            startService(intent);
            
			onSettingSelected(SETTING_MAIN);
		}
	};
	
	public View.OnClickListener mOnPreviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mSettings.setSelected(false);
        	mPreview.setSelected(true);
        	Intent intent = new Intent();
        	if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
        		intent.setClass(Settings.this, PreviewEx.class);
        	} else {
        	    intent.setClass(Settings.this, Preview.class);
        	}
    		startActivity(intent);
    		finish();
    	}
    };
    
	public View.OnClickListener mOnRecordingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mSettings.setSelected(false);
        	mRecordings.setSelected(true);
        	Intent intent = new Intent(getApplicationContext(), Records.class);
    		startActivity(intent);
    		finish();
    	}
    };
    
	private void updateTitle(String title) {
		mTitleView.setText(title);
	}
	
	private void showTitleAction(boolean show){
		if (show) {
			//default disable confirm btn
			mConfirm.setEnabled(false);
			mConfirm.setVisibility(View.VISIBLE);
			mCancel.setVisibility(View.VISIBLE);
		} else {
			mConfirm.setVisibility(View.INVISIBLE);
			mCancel.setVisibility(View.INVISIBLE);
		}
	}
}
