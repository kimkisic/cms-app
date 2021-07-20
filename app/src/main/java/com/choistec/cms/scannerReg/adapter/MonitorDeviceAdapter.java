package com.choistec.cms.scannerReg.adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.choistec.cms.scannerReg.R;
import com.choistec.cms.scannerReg.SmsRecyclerItem;
import com.choistec.cms.scannerReg.bluetooth.BluetoothCallBack;
import com.choistec.cms.scannerReg.bluetooth.BluetoothLeConnect;
import com.choistec.cms.scannerReg.unit.MonitorItem;

import java.util.ArrayList;

public class MonitorDeviceAdapter extends RecyclerView.Adapter<MonitorDeviceAdapter.ViewHolder>{

    public ArrayList<MonitorItem> mData = null;
    public Context mContext;
    private callback mClick;
    public void setDeviceList(ArrayList<MonitorItem> list){
        mData = list;
        notifyDataSetChanged();
    }
    public MonitorDeviceAdapter(){

    }
    public MonitorDeviceAdapter(ArrayList<MonitorItem> list, Context context){
        mData = list;
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView device ;
        TextView addr;

        ViewHolder(View itemView) {
            super(itemView) ;

            device = itemView.findViewById(R.id.device_id);
            addr = itemView.findViewById(R.id.device_addr);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        MonitorItem item = mData.get(pos);
                        mClick.check(item);
                    }
                }
            });

        }
    }
    @NonNull
    @Override
    public MonitorDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = LayoutInflater.from(mContext).inflate(R.layout.cms_monitor_item, parent, false);

        View view = inflater.inflate(R.layout.cms_monitor_item, parent, false);
        MonitorDeviceAdapter.ViewHolder viewHolder= new MonitorDeviceAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MonitorDeviceAdapter.ViewHolder holder, int position) {
       MonitorItem item = mData.get(position);

        holder.device.setText(item.getmDeviceName());
        holder.addr.setText(item.getmAddr());
    }
    public void setClick123(callback listener){
        this.mClick = listener;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    public interface callback{void check(MonitorItem item);}
}
