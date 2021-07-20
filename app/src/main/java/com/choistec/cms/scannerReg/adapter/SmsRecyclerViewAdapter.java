package com.choistec.cms.scannerReg.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.choistec.cms.scannerReg.R;

import java.util.ArrayList;

public class SmsRecyclerViewAdapter extends RecyclerView.Adapter {
    public ArrayList<com.choistec.cms.scannerReg.SmsRecyclerItem> mData = null;
    public Context mContext;
    private int priDate = 0;
    private int priPosition =0;
    public SmsRecyclerViewAdapter(ArrayList<com.choistec.cms.scannerReg.SmsRecyclerItem> list, Context context){
        mData = list;
        mContext = context;
    }
    public class MsgViewHolder extends RecyclerView.ViewHolder {
        TextView sms ;
        TextView smsDate ;
        MsgViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong ref`erence)
            sms = itemView.findViewById(R.id.sms_msg_txt) ;
            smsDate = itemView.findViewById(R.id.sms_date_txt_time) ;
        }
    }
    public class DateViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout dateLayout;
        TextView dd_sms_data;

        DateViewHolder(View itemView){
            super(itemView);
            dateLayout = itemView.findViewById(R.id.yy_mm_dd_layout);
            dd_sms_data = itemView.findViewById(R.id.yy_mm_date);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType ==1){
            View view = LayoutInflater.from(mContext).inflate(R.layout.sms_recyclerview_item, parent, false);
            return new DateViewHolder(view);
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.sms_recyclerview_item, parent, false);
            return new MsgViewHolder(view);
            // 원래는 바꿔줘야함
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        com.choistec.cms.scannerReg.SmsRecyclerItem item = mData.get(holder.getBindingAdapterPosition());
        if(holder instanceof MsgViewHolder){
            ((MsgViewHolder) holder).sms.setText(item.getmSms());
            ((MsgViewHolder) holder).smsDate.setText(item.getmSmsDate());
        }else{
            String strDate = item.getmSmsYYMMDD().replaceAll("-","");
            int date = Integer.parseInt(strDate);
            ((DateViewHolder) holder).dateLayout.setVisibility(View.VISIBLE);
            ((DateViewHolder) holder).dd_sms_data.setText(item.getmSmsYYMMDD());
        }
    }

//    @Override
//    public void onBindViewHolder(@NonNull SmsRecyclerViewAdapter.ViewHolder holder, int position) {
//        com.choistec.cms.scannerReg.SmsRecyclerItem item = mData.get(position);
//
//        holder.sms.setText(item.getmSms());
//        holder.smsDate.setText(item.getmSmsDate());
//        String strDate = item.getmSmsYYMMDD().replaceAll("-","");
//        int date = Integer.parseInt(strDate);
//
//        if(priDate != 0 && (date - priDate) == 0){
//            holder.dateLayout.setVisibility(View.GONE);
//        }
//        holder.dd_sms_data.setText(item.getmSmsYYMMDD());
//
//        if(priDate == 0){
//            priDate = date;
//            holder.dateLayout.setVisibility(View.VISIBLE);
//        }else if(priDate != 0 && (date - priDate) > 0){
//            holder.dateLayout.setVisibility(View.VISIBLE);
//        }
//
//
//    }

//    @Override
//    public int getItemViewType(int position) {
//        super.getItemViewType(position);
//        String date = mData.get(position).getmSmsYYMMDD();
//        date = date.replaceAll("-","");
//        //지금 포지션이 더 작아 이전 포지션이 더크고 근데 날짜가 이전꺼랑 지금꺼랑 빼서  -1 이면 리턴 날짜.
//        int date2 = Integer.parseInt(date);
//        Log.d("BJY","date 2: "+date2);
//        if(priDate == 0 || (priDate - date2) <0){
//            priDate = date2;
//            return 2;
//        }else{
//            return 1;
//        }
//
//    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
