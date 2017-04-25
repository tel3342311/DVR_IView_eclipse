package com.liteon.iview;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.RecordingItem;
import com.liteon.iview.util.VideoItemAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class Records extends Activity {

    private View mToolbar;
    private View mBottomBar;
    private ImageView mRecordings;
    private ImageView mSettings;
    private ImageView mPreview;
    private ListView mListView;
    private VideoItemAdapter mVideoItemAdapter;
    private List<RecordingItem> mDataList;
    private View mSelectAll;
    private View mSaveToPhone;
    private View mSaveToOTG;
    private View mDelete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_records);
		findViews();
		setListeners();
		setupListView();
	}
	
	private void findViews() {
		mToolbar = findViewById(R.id.toolbar_preview);
        mBottomBar = findViewById(R.id.bottombar);
        mPreview = (ImageView) findViewById(R.id.preview_icon);
        mRecordings = (ImageView) findViewById(R.id.recordings_icon);
        mSettings = (ImageView) findViewById(R.id.setting_icon);
        mListView = (ListView) findViewById(R.id.listview);
        mSaveToOTG = findViewById(R.id.save_to_otg);
        mSaveToPhone = findViewById(R.id.save_to_phone);
        mSelectAll = findViewById(R.id.select_all);
    }
	
	private void setListeners() {
		mPreview.setOnClickListener(mOnPreviewClickListener);
		mSettings.setOnClickListener(mOnSettingsClickListener);
		mSelectAll.setOnClickListener(mOnSelectAllClickListener);
		mSaveToOTG.setOnClickListener(mOnSaveToOTGClickListener);
		mSaveToPhone.setOnClickListener(mOnSaveToPhoneClickListener);
	}
	
	private void setupListView() {
		mVideoItemAdapter = new VideoItemAdapter(this);
		mDataList = new ArrayList<RecordingItem>();
		mVideoItemAdapter.setDataList(mDataList);
		mListView.setAdapter(mVideoItemAdapter);
	}
	
	public View.OnClickListener mOnPreviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	Intent intent = new Intent(getApplicationContext(), Preview.class);
    		startActivity(intent);        
    	}
    };
    
	public View.OnClickListener mOnSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	Intent intent = new Intent(getApplicationContext(), Settings.class);
    		startActivity(intent);        
    	}
    };
    
    @Override
	protected void onResume() {
		super.onResume();
		updateList();
        IntentFilter intentFilter = new IntentFilter(Def.ACTION_GET_RECORDING_LIST);
        registerReceiver(mBroadcastReceiver, intentFilter);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }
	
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
        public void onReceive(Context context, Intent intent) {
			updateList();
        }
    };
    
    private void updateList() {
    	SharedPreferences sp = getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
        String json = sp.getString(Def.SP_RECORDING_LIST, "");
        Type typeOfList = new TypeToken<List<RecordingItem>>(){}.getType();
        Gson gson = new GsonBuilder().create();
        List<RecordingItem> tempList = gson.fromJson(json, typeOfList);
        if (tempList != null) {
        	mDataList.clear();
        	mDataList.addAll(tempList);
        }
        mVideoItemAdapter.notifyDataSetChanged();
    }
    
    
    private View.OnClickListener mOnSelectAllClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			boolean isSelectAll;
			if (mSelectAll.isSelected()) {
				mSelectAll.setSelected(false);
				isSelectAll = false;
				mSaveToOTG.setEnabled(false);
				mSaveToPhone.setEnabled(false);
			} else {
				mSelectAll.setSelected(true);
				isSelectAll = true;
				mSaveToOTG.setEnabled(true);
				mSaveToPhone.setEnabled(true);
			}
			
			for (RecordingItem item : mDataList) {
				item.setSelected(isSelectAll);
			}
			mVideoItemAdapter.notifyDataSetChanged();
		}
	};
	private OnClickListener mOnSaveToOTGClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
		}
	};
	private OnClickListener mOnSaveToPhoneClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
		}
	};
}
