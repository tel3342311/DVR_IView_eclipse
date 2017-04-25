package com.liteon.iview.util;

import java.util.ArrayList;
import java.util.List;

import com.liteon.iview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoItemAdapter extends BaseAdapter {

	private List<RecordingItem> mDataList;
	private LayoutInflater mInflater;
	
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
			convertView = mInflater.inflate(R.layout.video_item, parent);			
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
		
		select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View root = v.getRootView();
				Integer idx = (Integer)root.getTag();
				RecordingItem item = mDataList.get(idx.intValue());
				boolean isSelect = item.isSelected();
				item.setSelected(!isSelect);
				notifyDataSetChanged();
			}
		});
		convertView.setTag(new Integer(position));
		return convertView;
	}
	public void setDataList(List<RecordingItem> list) {
			mDataList = list;
	}
}
