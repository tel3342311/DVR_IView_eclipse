package com.liteon.iview.settings;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liteon.iview.R;
import com.liteon.iview.Settings;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;
import com.liteon.iview.util.ExtendNumberPicker;
import com.liteon.iview.util.StatusDialog;

public class InternetSetting extends Fragment implements OnConfirmListener{
	private String mAPN;
    private String mPIN;
    private String mDial_Num;
    private String mUsername;
    private String mPassword;
    private String mModem;
    private String mModemTitle;
    private Map<String, String> mModemList;

    private TextView mTextViewAPN;
    private TextView mTextViewPIN;
    private TextView mTextViewDialNum;
    private TextView mTextViewUserName;
    private TextView mTextViewPassword;
    private ViewGroup mPicker;
    private ImageView mConfirm;
    private View mRootView;
    private String currentAPN;
    private String currentPIN;
    private String currentDialNum;
    private String currentUsername;
    private String currentPassword;
    private String currentModem;
    private ExtendNumberPicker mNumPicker;
    private IntentFilter mIntenFilter;
    private View mProgressView;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private final static long PROGRESS_TIME = 1500;
	private Handler mHandlerTime;
	private long mStartTime;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_internet, container, false);
		findViews(view);
        setListeners();
        mHandlerTime = new Handler();
        return view;
    }
	
	private void setListeners() {
        mTextViewAPN.addTextChangedListener(mTextWatcherAPN);
        mTextViewPIN.addTextChangedListener(mTextWatcherPIN);
        mTextViewDialNum.addTextChangedListener(mTextWatcherDialNum);
        mTextViewUserName.addTextChangedListener(mTextWatcherUserName);
        mTextViewPassword.addTextChangedListener(mTextWatcherPassword);
    }
	
	private void setupPicker() {
        mNumPicker = new ExtendNumberPicker(getActivity(), null);
    	List<String> list = new ArrayList(mModemList.keySet());
        mNumPicker.setMinValue(0);
        mNumPicker.setMaxValue(list.size() - 1);
        mNumPicker.setDisplayedValues(list.toArray(new String[0]));
        mNumPicker.setValue(list.indexOf(mModemTitle));
        mNumPicker.setWrapSelectorWheel(false);
        mNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mNumPicker.setOnValueChangedListener(mOnValueChangeListener);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        mPicker.addView(mNumPicker);
    }
	
	private void isSettingChanged() {
        if (TextUtils.equals(currentAPN, mAPN) &&
                TextUtils.equals(currentPIN, mPIN) &&
                TextUtils.equals(currentDialNum, mDial_Num) &&
                TextUtils.equals(currentUsername, mUsername) &&
                TextUtils.equals(currentPassword, mPassword) &&
                TextUtils.equals(mModem, currentModem)) {
            mConfirm.setEnabled(false);
        } else {
            mConfirm.setEnabled(true);
        }
    }

    private void findViews(View view) {
        mTextViewAPN = (TextView) view.findViewById(R.id.edit_apn_server);
        mTextViewPIN = (TextView) view.findViewById(R.id.edit_pin);
        mTextViewDialNum = (TextView) view.findViewById(R.id.edit_dial_num_server);
        mTextViewUserName = (TextView) view.findViewById(R.id.edit_username);
        mTextViewPassword = (TextView) view.findViewById(R.id.edit_password);
        mPicker = (ViewGroup) view.findViewById(R.id.picker_container);
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

    private TextWatcher mTextWatcherAPN = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentAPN = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mTextWatcherPIN = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentPIN = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mTextWatcherDialNum = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentDialNum = s.toString();
            isSettingChanged();
        }
    };

    private TextWatcher mTextWatcherUserName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            currentUsername = s.toString();
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
            currentPassword = s.toString();
            isSettingChanged();
        }
    };



    private android.widget.NumberPicker.OnValueChangeListener mOnValueChangeListener = new android.widget.NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
            String modem = picker.getDisplayedValues()[newVal];
            Log.i("Modem", modem);

            currentModem = mModemList.get(modem);
            isSettingChanged();
        }
    };

    public String getCurrentAPN() {
        return currentAPN;
    }

    public String getCurrentPIN() {
        return currentPIN;
    }

    public String getCurrentDialNum() {
        return currentDialNum;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getCurrentModem() {
        return currentModem;
    }

    private void registerBroadcastRecevier() {
    	Activity activity = getActivity();
    	if (mIntenFilter == null) {
    		mIntenFilter = new IntentFilter(Def.ACTION_GET_NETWORKING);
    		mIntenFilter.addAction(Def.ACTION_SET_INTERNET);
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
				mAPN = sp.getString(Def.SP_APN3G, "APN");
				mPIN = sp.getString(Def.SP_PIN3G, "PIN");
				mDial_Num = sp.getString(Def.SP_DIAL3G, "Dial number");
				mUsername = sp.getString(Def.SP_USER3G, "User name");
				mPassword = sp.getString(Def.SP_PASSWORD3G, "Password");
				mModem = sp.getString(Def.SP_MODEM_NAME, "AUTO");
				String json = sp.getString(Def.SP_MODEM_LIST_JSON, "");
				Type typeOfHashMap = new TypeToken<Map<String, String>>() {
				}.getType();
				Gson gson = new GsonBuilder().create();
				mModemList = gson.fromJson(json, typeOfHashMap);
				if (mModemList == null) {
					mModemList = new HashMap<>();
					mModemList.put("AUTO", "AUTO");
					mModemList.put("HTC", "HTC");
					mModemList.put("CHT", "CHT");
				}
				mModemTitle = "";
				for (Map.Entry entry : mModemList.entrySet()) {
					if (mModem.equals(entry.getValue())) {
						mModemTitle = (String) entry.getKey();
						break;
					}
				}
				setupPicker();
				currentModem = mModem;
				// Toast.makeText(getContext(), "APN " + mAPN + ", PIN " + mPIN
				// + ", Dial_Num " + mDial_Num + ", User Name " + mUsername + ",
				// Password " + mPassword + " modem list " +
				// mModemList.toString(), Toast.LENGTH_LONG).show();
				// Set Default value
				mTextViewAPN.setText(mAPN);
				mTextViewPIN.setText(mPIN);
				mTextViewDialNum.setText(mDial_Num);
				mTextViewUserName.setText(mUsername);
				mTextViewPassword.setText(mPassword);
				mConfirm.setEnabled(false);
			} else if (TextUtils.equals(intent.getAction(), Def.ACTION_SET_INTERNET)) {
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
		String apn = getCurrentAPN();
        String pin = getCurrentPIN();
        String dial_Num = getCurrentDialNum();
        String username = getCurrentUsername();
        String password = getCurrentPassword();
        String modem = getCurrentModem();
        intent.setAction(Def.ACTION_SET_INTERNET);
        intent.putExtra(Def.EXTRA_APN, apn);
        intent.putExtra(Def.EXTRA_PIN, pin);
        intent.putExtra(Def.EXTRA_DIAL_NUM, dial_Num);
        intent.putExtra(Def.EXTRA_USERNAME_3G, username);
        intent.putExtra(Def.EXTRA_PASSWORD_3G, password);
        intent.putExtra(Def.EXTRA_MODEM, modem);
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
