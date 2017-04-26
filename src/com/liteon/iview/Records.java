package com.liteon.iview;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
    public static final int UPDATE_TOOL_BAR = 1;
    public static final int ARG_UNSELECT = 1;
    public static final int ARG_SELECT = 2;

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
        mDelete = findViewById(R.id.delete);
    }
	
	private void setListeners() {
		mPreview.setOnClickListener(mOnPreviewClickListener);
		mSettings.setOnClickListener(mOnSettingsClickListener);
		mSelectAll.setOnClickListener(mOnSelectAllClickListener);
		mSaveToOTG.setOnClickListener(mOnSaveToOTGClickListener);
		mSaveToPhone.setOnClickListener(mOnSaveToPhoneClickListener);
		mListView.setOnItemClickListener(mOnItemClickListener);
	}
	
	private void setupListView() {
		mVideoItemAdapter = new VideoItemAdapter(this);
		mDataList = new ArrayList<RecordingItem>();
		mVideoItemAdapter.setDataList(mDataList);
		mVideoItemAdapter.setUiHandler(mHandler);
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
        mSaveToOTG.setEnabled(false);
        mSaveToPhone.setEnabled(false);
        mDelete.setEnabled(false);
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
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			resetSelectState();
			Intent intent = new Intent();
			intent.putExtra(Def.EXTRA_VIDEO_ITEM_ID, position);
			intent.setClass(getApplicationContext(), VideoPlay.class);
			startActivity(intent);
		}
		
	};
	
	private void resetSelectState(){
		mSelectAll.setSelected(false);
		mSaveToOTG.setEnabled(false);
		mSaveToPhone.setEnabled(false);
		for (RecordingItem item : mDataList) {
			item.setSelected(false);
		}
		mVideoItemAdapter.notifyDataSetChanged();
	}
	
	private boolean hasSelectedState(){
		boolean isSelected = false;
		for (RecordingItem item : mDataList) {
			if (item.isSelected()) {
				isSelected = true;
				break;
			}
		}
		return isSelected;
	}
	
	private boolean isSelectedAllState(){
		boolean isSelectAll = true;
		for (RecordingItem item : mDataList) {
			if (!item.isSelected()) {
				isSelectAll = false;
				break;
			}
		}
		return isSelectAll;
	}
	
	private Handler mHandler = new Handler() {
		int unselect = 1;
		int select = 2;
		@Override
        public void handleMessage(Message msg) {
            switch(msg.what){
            case UPDATE_TOOL_BAR:
            	if (ARG_UNSELECT == msg.arg1) {
            		if (!hasSelectedState()) {
            			mSaveToOTG.setEnabled(false);
            			mSaveToPhone.setEnabled(false);
            		}
        			mSelectAll.setSelected(false);
            	} else if (ARG_SELECT == msg.arg1) {
            		mSaveToOTG.setEnabled(true);
        			mSaveToPhone.setEnabled(true);
        			if (isSelectedAllState()) {
            			mSelectAll.setSelected(true);
        			}
            	}
                break;
            default:
                break;
            }
        }
	};
}
