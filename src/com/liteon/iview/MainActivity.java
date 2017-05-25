package com.liteon.iview;

import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.StatusDialog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private String mSystemMode;
	private StatusDialog dialog;
	private ProgressBar mProgressBar;
	private Handler mHandlerTime;
	private int mProgressStep;
	private TextView mVersionText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
		mHandlerTime = new Handler();
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;
			mVersionText.setText("V" + version); 
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}


	private void findViews() {
		mProgressBar = (ProgressBar) findViewById(R.id.loading_progress);
		mVersionText = (TextView) findViewById(R.id.version_info);
	}
	
	public Runnable UpdateProgress = new Runnable()
    {
        public void run() {
        	if (mProgressStep >= 10) {
        		mProgressBar.setProgress(100);
        	} else {
        		mProgressStep++;
        		int progress =  mProgressStep * 100 / 10;
        		mProgressBar.setProgress(progress);
        		mHandlerTime.postDelayed(UpdateProgress, 500);
        	}
        }
    };
    
	@Override
	protected void onResume() {
		super.onResume();
		mHandlerTime.postDelayed(UpdateProgress, 500);
        IntentFilter intentFilter = new IntentFilter(Def.ACTION_GET_ALL_INFO);
        registerReceiver(mBroadcastReceiver, intentFilter);
        //Get DVR info
		Intent intent = new Intent(getApplicationContext(), DvrInfoService.class);
		intent.setAction(Def.ACTION_GET_ALL_INFO);
		startService(intent);
		mProgressBar.setProgress(0);
		mProgressStep = 0;
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        mHandlerTime.removeCallbacks(UpdateProgress);
    }
	
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
        public void onReceive(Context context, Intent intent) {

            
            Intent intentActivity = new Intent();
            boolean isDVRReachable = intent.getBooleanExtra(Def.EXTRA_IS_DVR_REACHABLE, false);
            mProgressStep = 10;
            mProgressBar.setProgress(100);
            if (isDVRReachable) {
            	String mode = intent.getStringExtra(Def.EXTRA_GET_ALL_INFO);
                mSystemMode = mode;
	            if (mSystemMode.equals(Def.RECORDING_MODE)) {
	            	intentActivity.setClass(MainActivity.this, Preview.class);
	            } else if (mSystemMode.equals(Def.STORAGE_MODE)) {
	            	intentActivity.setClass(MainActivity.this, Records.class);
	            } 
            } else {
            	showVpnDialog();
            	return;
            }
            startActivity(intentActivity);
            finish();
        }
    };
    
    private void showVpnDialog() {
    	dialog = new StatusDialog(MainActivity.this, "iView is unable to establish a connection.", false, mOnClickListener);
    	dialog.setRetryMessage("Please Setup VPN again");
    	dialog.setOnDismissListener(mOnDismissListener);
    	dialog.show();
		
    }
    private OnDismissListener mOnDismissListener = new OnDismissListener() {
		
		@Override
		public void onDismiss(DialogInterface dialog) {
			finish();
		}
	};
	
	private android.view.View.OnClickListener mOnClickListener = new android.view.View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intentActivity = new Intent();
			intentActivity.setAction("android.net.vpn.SETTINGS");
        	intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(intentActivity);
        	dialog.dismiss();
		}
	};
}