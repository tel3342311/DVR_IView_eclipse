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
import com.liteon.iview.util.StatusDialog;
import com.liteon.iview.util.UsbUtil;
import com.liteon.iview.util.VideoItemAdapter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import junit.runner.Version;

public class Records extends Activity {

	private final static String TAG = Records.class.getName();
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
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mIsOTGReady;
	private View mMenuLoadingIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_records);
		findViews();
		setListeners();
		setupListView();
	}
	
    private void checkSystemMode() {
    	SharedPreferences sp = getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
    	String system_mode = sp.getString(Def.SP_SYSTEM_MODE, Def.STORAGE_MODE);
        if (!TextUtils.equals(system_mode, Def.STORAGE_MODE)) {
        	mMenuLoadingIndicator.setVisibility(View.VISIBLE);
    		Intent intent = new Intent(getApplicationContext(), DvrInfoService.class);
    		intent.setAction(Def.ACTION_SET_SYS_MODE);
    		intent.putExtra(Def.EXTRA_SET_SYS_MODE, Def.STORAGE_MODE);
    		startService(intent);
        }
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
        mMenuLoadingIndicator = (View) findViewById(R.id.progress);
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
		View footerView = ((LayoutInflater)(getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))).inflate(R.layout.video_item_footer, null, false);
		//Call this before calling setAdapter
		mListView.addFooterView(footerView);
		mListView.setAdapter(mVideoItemAdapter);
	}
	
	public View.OnClickListener mOnPreviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mRecordings.setSelected(false);
        	mPreview.setSelected(true);
        	Intent intent = new Intent(getApplicationContext(), Preview.class);
    		startActivity(intent);
    		finish();
    	}
    };
    
	public View.OnClickListener mOnSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mRecordings.setSelected(false);
        	mSettings.setSelected(true);
        	Intent intent = new Intent(getApplicationContext(), Settings.class);
    		startActivity(intent);
    		finish();
    	}
    };
    
    @Override
	protected void onResume() {
		super.onResume();
		updateList();
        IntentFilter intentFilter = new IntentFilter(Def.ACTION_GET_RECORDING_LIST);
        intentFilter.addAction(Def.ACTION_GET_SYS_MODE);
        intentFilter.addAction(Def.ACTION_SAVE_TO_PROGRESS);
        intentFilter.addAction(Def.ACTION_SAVE_TO_PHONE_STATUS);
        intentFilter.addAction(Def.ACTION_SAVE_TO_OTG_STATUS);
        registerReceiver(mBroadcastReceiver, intentFilter);
		registerOTGEvent();
        mSaveToOTG.setEnabled(false);
        mSaveToPhone.setEnabled(false);
        mDelete.setEnabled(false);
        mRecordings.setSelected(true);
        checkSystemMode();
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        unregisterOTGEvent();
    }
	
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
        public void onReceive(Context context, Intent intent) {
			if (TextUtils.equals(Def.ACTION_GET_RECORDING_LIST, intent.getAction())) {
				updateList();
			} else if (TextUtils.equals(Def.ACTION_GET_SYS_MODE, intent.getAction())) { 
	        	mMenuLoadingIndicator.setVisibility(View.INVISIBLE);
				updateList();
			} else if (TextUtils.equals(Def.ACTION_SAVE_TO_PROGRESS, intent.getAction())) {
				mMenuLoadingIndicator.setVisibility(View.VISIBLE);
				int progress = intent.getIntExtra(Def.EXTRA_SAVE_TO_PROGRESS, 0);
				int idx = intent.getIntExtra(Def.EXTRA_SAVE_TO_IDX, 1);
				int count = intent.getIntExtra(Def.EXTRA_SAVE_TO_COUNT, 1);
				if (count == 1) {
					Toast.makeText(Records.this, "Download Progress is "+progress + "%", Toast.LENGTH_LONG).show();
				} else if (count > 1) {
					Toast.makeText(Records.this, "Download Progress is "+progress + "% (" + idx + "/" + count + ")", Toast.LENGTH_LONG).show();
				}
			} else {
				boolean isSaveToPhone = false;
				if (TextUtils.equals(intent.getAction(), Def.ACTION_SAVE_TO_PHONE_STATUS)) {
					isSaveToPhone = true;
				}
				if (TextUtils.equals(intent.getAction(), Def.ACTION_SAVE_TO_PHONE_STATUS) ||
						TextUtils.equals(intent.getAction(), Def.ACTION_SAVE_TO_OTG_STATUS)) {
					mMenuLoadingIndicator.setVisibility(View.GONE);
					String [] ids = intent.getStringArrayExtra(Def.EXTRA_VIDEO_ITEM_ID);
					String [] path = intent.getStringArrayExtra(Def.EXTRA_SAVE_STATUS_FILE_PATH);
					boolean isSuccess = intent.getBooleanExtra(Def.EXTRA_SAVE_STATUS, false);
					showDialog(isSuccess, isSaveToPhone);
					boolean [] status = intent.getBooleanArrayExtra(Def.EXTRA_SAVE_STATUS_ARY);
					for (int i = 0; i < status.length; i++) {
						if (status[i]) {
							RecordingItem item = mDataList.get(Integer.parseInt(ids[i]));
							if (TextUtils.equals(intent.getAction(), Def.ACTION_SAVE_TO_PHONE_STATUS)) {
								item.setLocalPath(path[i]);
							} else {
								item.setOtgPath(path[i]);
							}
						}
					}
				}
			}
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
        } else {
        	RecordingItem item = new RecordingItem("http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_30mb.mp4",
        											"big_buck_bunny_720p_30mb.mp4",
        											"2017/04/27", 
        											"30mb");
        	mDataList.add(item);
        											
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
				if (mIsOTGReady) {
					mSaveToOTG.setEnabled(true);
				}
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
			mMenuLoadingIndicator.setVisibility(View.VISIBLE);
			List<RecordingItem> selecteditemList = getSelectedList();
			String [] ids = new String[selecteditemList.size()];
			String [] urls = new String[selecteditemList.size()];
			String [] names = new String[selecteditemList.size()];
			int idx = 0;
			for (RecordingItem item : selecteditemList) {
				ids[idx] = Integer.toString(mDataList.indexOf(item));
				urls[idx] = item.getUrl();
				names[idx] = item.getName();
				idx++;
			}
			Intent intent = new Intent();
			intent.setAction(Def.ACTION_SAVE_TO_OTG);
			intent.putExtra(Def.EXTRA_VIDEO_ITEM_ID, ids);
			intent.putExtra(Def.EXTRA_SAVE_ITEM_URL, urls);
			intent.putExtra(Def.EXTRA_SAVE_ITEM_NAME, names);
			intent.setClass(getApplicationContext(), DvrInfoService.class);
			startService(intent);
		}
	};
	private OnClickListener mOnSaveToPhoneClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mMenuLoadingIndicator.setVisibility(View.VISIBLE);
			List<RecordingItem> selecteditemList = getSelectedList();
			String [] ids = new String[selecteditemList.size()];
			String [] urls = new String[selecteditemList.size()];
			String [] names = new String[selecteditemList.size()];
			int idx = 0;
			for (RecordingItem item : selecteditemList) {
				ids[idx] = Integer.toString(mDataList.indexOf(item));
				urls[idx] = item.getUrl();
				names[idx] = item.getName();
				idx++;
			}
			Intent intent = new Intent();
			intent.setAction(Def.ACTION_SAVE_TO_PHONE);
			intent.putExtra(Def.EXTRA_VIDEO_ITEM_ID, ids);
			intent.putExtra(Def.EXTRA_SAVE_ITEM_URL, urls);
			intent.putExtra(Def.EXTRA_SAVE_ITEM_NAME, names);
			intent.setClass(getApplicationContext(), DvrInfoService.class);
			startService(intent);
		}
	};
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			resetSelectState();
			Intent intent = new Intent();
			intent.putExtra(Def.EXTRA_VIDEO_ITEM_ID, position);
//			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
				intent.setClass(getApplicationContext(), VideoPlayEX.class);
//			} else {
//				intent.setClass(getApplicationContext(), VideoPlay.class);
//			}
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
	
	private List<RecordingItem> getSelectedList() {
		List<RecordingItem> itemList = new ArrayList<RecordingItem>();
		for (RecordingItem item : mDataList) {
			if (item.isSelected()) {
				itemList.add(item);
			}
		}
		return itemList;
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
            		if (mIsOTGReady) {
            			mSaveToOTG.setEnabled(true);
            		}
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
	
	private void registerOTGEvent() {
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Def.ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(Def.ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		if (UsbUtil.discoverDevice(getApplicationContext(), getIntent())) {
			//mSaveToOTG.setEnabled(true);
			mIsOTGReady = true;
		}
		
	}
	
	private void unregisterOTGEvent() {
		unregisterReceiver(mUsbReceiver);
	}
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Def.ACTION_USB_PERMISSION)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            Log.v(TAG, usbDevice.getDeviceName());
                        }
            			mIsOTGReady = true;
            			if (hasSelectedState()) {
                			mSaveToOTG.setEnabled(false);
            			}
                    } else {
                        Log.v(TAG, "usb permission is denied");
                        mSaveToOTG.setEnabled(false);
            			mIsOTGReady = false;
                    }
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    //close connection
                    Log.v(TAG,"Device disconnected");
                    //Toast.makeText(Records.this, "USB device disonnected!! getDeviceProtocol " + usbDevice.getDeviceProtocol() + "", Toast.LENGTH_LONG).show();
        			mSaveToOTG.setEnabled(false);
        			mIsOTGReady = false;
                }
            }else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Log.v(TAG,"Device connected");
                //Toast.makeText(Records.this, "USB device Connected!! getDeviceProtocol " + usbDevice.getDeviceProtocol() + "", Toast.LENGTH_LONG).show();
                //mSaveToOTG.setEnabled(true);
    			mIsOTGReady = true;
    			if (hasSelectedState()) {
        			mSaveToOTG.setEnabled(false);
    			}
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    };
    
    private void showDialog(boolean isSuccess, boolean isSaveToPhone) {
    	StatusDialog dialog;
		if (isSuccess) {
			if (isSaveToPhone)
				dialog = new StatusDialog(Records.this, "Save it to Phone", isSuccess);
			else 
				dialog = new StatusDialog(Records.this, "Save it to OTG", isSuccess);
		} else {
			dialog = new StatusDialog(Records.this, "Save Failed!", isSuccess);
		}
		dialog.show();
    }
}
