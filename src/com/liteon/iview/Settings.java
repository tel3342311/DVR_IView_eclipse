package com.liteon.iview;

import com.liteon.iview.settings.InternetSetting;
import com.liteon.iview.settings.MainSetting;
import com.liteon.iview.settings.MainSetting.OnSettingPageSelectedListener;
import com.liteon.iview.settings.RecordSetting;
import com.liteon.iview.settings.TimezoneSetting;
import com.liteon.iview.settings.VPNSetting;
import com.liteon.iview.settings.WifiSetting;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

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
		mMainSetting = new MainSetting();

		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.frag_container, mMainSetting);
		transaction.commit();
	}

	private void setListeners() {

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

	private View.OnClickListener mOnSettingClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
				case R.id.time_zone:
					
				break;
			}
		}
	};

	@Override
	public void onSettingSelected(int position) {
		
		Fragment fragment;
		switch (position) {
			case SETTING_MAIN:
				if (mMainSetting == null) {
					mMainSetting = new MainSetting();
				}
				fragment = mMainSetting;
				break;
			case SETTING_TIMEZONE:
				if (mTimezoneSetting == null) {
					mTimezoneSetting = new TimezoneSetting();
				}
				fragment = mTimezoneSetting;
				break;
			case SETTING_INTERNET:
				if (mInternetSetting == null) {
					mInternetSetting = new InternetSetting();
				}
				fragment = mInternetSetting;
				break;
			case SETTING_VPN:
				if (mVpnSetting == null) {
					mVpnSetting = new VPNSetting();
				}
				fragment = mVpnSetting;
				break;
			case SETTING_WIFI:
				if (mWifiSetting == null) {
					mWifiSetting = new WifiSetting();
				}
				fragment = mWifiSetting;
				break;
		};
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.frag_container, mMainSetting);
		transaction.commit();
	}
}
