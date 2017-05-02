package com.liteon.iview.util;

import com.liteon.iview.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusDialog extends Dialog {

	private Activity mActivity;
	private ImageView mStatusIcon;
	private ImageView mOk;
	private TextView mText;
	private TextView mTextRetry;
	private String mMessage;
	private boolean mIsSuccess;

	
	public StatusDialog(Activity context, String message, boolean isSuccess) {
		super(context);
		mActivity = context;
		mMessage = message;
		mIsSuccess = isSuccess;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	    setContentView(R.layout.dialog_save);
	    mOk = (ImageView) findViewById(R.id.ok_btn);
	    mText = (TextView) findViewById(R.id.status_text);
	    mTextRetry = (TextView) findViewById(R.id.status_fail_text);
	    mStatusIcon = (ImageView) findViewById(R.id.status_icon);
	    mOk.setOnClickListener(mOnClickListener);
	}
	
	private android.view.View.OnClickListener mOnClickListener = new android.view.View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			dismiss();
		}
	};
	
	protected void onStart() {
		super.onStart();
		mText.setText(mMessage);
		if (mIsSuccess) {
			mTextRetry.setVisibility(View.INVISIBLE);
			mStatusIcon.setBackground(getContext().getResources().getDrawable(R.drawable.popup_img_ok));
		} else {
			mTextRetry.setVisibility(View.VISIBLE);
			mStatusIcon.setBackground(getContext().getResources().getDrawable(R.drawable.popup_img_warning));
		}
	};
}
