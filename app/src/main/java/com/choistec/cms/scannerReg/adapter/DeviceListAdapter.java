package com.choistec.cms.scannerReg;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.choistec.cms.scannerReg.util.CommonInfo;

import java.util.ArrayList;

/**
 * Created by sejin on 2017. 2. 9..
 */

public class DeviceListAdapter extends BaseAdapter {

    private ArrayList<com.choistec.cms.scannerReg.DeviceItem> mDeviceItemArr;
    private Handler mHandler;
    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    public DeviceListAdapter(Context context, ArrayList<com.choistec.cms.scannerReg.DeviceItem> deviceItemArr, Handler handler) {
        mDeviceItemArr = deviceItemArr;
        mHandler = handler;
    }

    @Override
    public int getCount() {
        return mDeviceItemArr.size();
    }

    @Override
    public com.choistec.cms.scannerReg.DeviceItem getItem(int position) {
        return mDeviceItemArr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDeviceItemArr.get(position).hashCode();
    }

    public void setmDeviceItemArr(ArrayList<com.choistec.cms.scannerReg.DeviceItem> deviceItemArr){
        mDeviceItemArr = deviceItemArr;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        final int pos = position;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.device_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.device_text  = (TextView) convertView.findViewById(R.id.device_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.device_text.setText(mDeviceItemArr.get(pos).getScannerId());


        try {
            viewHolder.device_text.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    viewHolder.device_text.setBackgroundColor(Color.parseColor("#EEEEEE"));

                    try {
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                viewHolder.device_text.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            }
                        }, 150);
                    } catch (Exception e) {
                    }

                    Message msg = mHandler.obtainMessage();
                    msg.what = CommonInfo.SCAN_DEVICE_SELECT;
                    msg.arg1 = pos;
                    mHandler.sendMessage(msg);

                }
            });
        } catch (Exception e) {
        }


        return convertView;
    }




    private class ViewHolder {
        private TextView device_text;
    }





}


