package com.liteon.iview.util;

import java.util.ArrayList;
import java.util.List;

import com.liteon.iview.R;
import com.liteon.iview.Records;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoItemAdapter extends BaseAdapter {

	private List<RecordingItem> mDataList;
	private LayoutInflater mInflater;
	private Handler mHandler;
	
	public VideoItemAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.video_item, null);			
		}
		RecordingItem item = mDataList.get(position);
		TextView name = (TextView) convertView.findViewById(R.id.video_name);
		TextView date = (TextView) convertView.findViewById(R.id.video_date);
		ImageView select = (ImageView) convertView.findViewById(R.id.video_item_select);
		name.setText(item.getName());
		date.setText(item.getTime());
		if (item.isSelected())
			select.setSelected(true);
		else {
			select.setSelected(false);
		}
		select.setTag(item);
		
		select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RecordingItem item = (RecordingItem) v.getTag();
				boolean isSelect = !item.isSelected();
				item.setSelected(isSelect);
				notifyDataSetChanged();
				int select = isSelect ? Records.ARG_SELECT : Records.ARG_UNSELECT;
				Message message = mHandler.obtainMessage(Records.UPDATE_TOOL_BAR, select, 0);
				mHandler.sendMessage(message);
			}
		});
		convertView.setTag(new Integer(position));
		return convertView;
	}
	public void setDataList(List<RecordingItem> list) {
			mDataList = list;
	}
	public void setUiHandler(Handler handler) {
		mHandler = handler;
	}
}
