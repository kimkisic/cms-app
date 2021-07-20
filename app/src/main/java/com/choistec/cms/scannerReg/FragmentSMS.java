package com.choistec.cms.scannerReg;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.choistec.cms.scannerReg.adapter.SmsRecyclerViewAdapter;
import com.choistec.cms.scannerReg.dbbase.ChoisDBHelper;
import com.google.zxing.integration.android.IntentIntegrator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class FragmentSMS extends Fragment {
    public Context mContext;
    public RecyclerView mRecyclerView = null;
    private SmsRecyclerViewAdapter mSmsAdapter= null;
    ArrayList<com.choistec.cms.scannerReg.SmsRecyclerItem> mList = new ArrayList<com.choistec.cms.scannerReg.SmsRecyclerItem>();
    public FragmentSMS(Context context){
        mContext = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.cms_sms_view, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.cms_sms_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mSmsAdapter = new SmsRecyclerViewAdapter(getListItem(),mContext);
        mRecyclerView.setAdapter(mSmsAdapter);
        return rootView;
//        return inflater.inflate(R.layout.fragment_monitor_stting, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
//        initView();
//        setClickEvent();
        //onCreateView 이후 호출


    }
    /////////////////////////////////////////



    ////////////////////////////////////////



    @Override public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("text", 2);
        //사라질때 현재의 상태를 저장하고 나중에 Fragment가 다시 돌아오면 내용을 사용할 수 있게해주는 메소드드
    }
    public void onStart(){
        super.onStart();
        //모든 UI가 만들어지고 호출된다.
    }
    private void initView(){
    }
    private void setClickEvent(){
    }
    private void test(BluetoothGattCharacteristic a ){
        int chars = a.getProperties();
        if((chars | BluetoothGattCharacteristic.PROPERTY_NOTIFY) >0){

        }
    }
    private ArrayList<com.choistec.cms.scannerReg.SmsRecyclerItem> getListItem(){
        ArrayList<com.choistec.cms.scannerReg.SmsRecyclerItem> items = new ArrayList<>();
        Cursor cursor = ChoisDBHelper.selectAllData("sms_table","xpointer");
        if(cursor != null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while(cursor.moveToNext()){
                com.choistec.cms.scannerReg.SmsRecyclerItem item = new com.choistec.cms.scannerReg.SmsRecyclerItem();
                String data = cursor.getString(cursor.getColumnIndex("sms_data"));
                String date = cursor.getString(cursor.getColumnIndex("sms_date"));
                String[] date2= date.split(" ");
                for(int i=0; i< date2.length; i++){
                    Log.d("QWE"," value : "+date2[i]);
                }
                Log.d("BJY","date : "+date);
                item.setmSms(data);
                item.setmSmsDate(date2[1]);
                item.setmSmsYYMMDD(date2[0]);
                items.add(item);
            }
            if(cursor.isClosed() != false){
                cursor.close();
            }
        }
        Log.d("BJY","list size : "+items.size());
    return items;
    }
}
