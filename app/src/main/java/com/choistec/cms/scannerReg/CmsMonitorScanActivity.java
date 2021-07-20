package com.choistec.cms.scannerReg;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.choistec.cms.scannerReg.adapter.MonitorDeviceAdapter;
import com.choistec.cms.scannerReg.bluetooth.BluetoothCallBack;
import com.choistec.cms.scannerReg.bluetooth.BluetoothLeConnect;
import com.choistec.cms.scannerReg.service.UserTokenService;
import com.choistec.cms.scannerReg.unit.MonitorItem;
import com.choistec.cms.scannerReg.util.ChoisHttpApi;
import com.choistec.cms.scannerReg.util.tokenModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CmsMonitorScanActivity extends Activity implements BluetoothCallBack {
    private ImageView mBackImg, mReScanImg;
    public RecyclerView mDeviceRecyclerView = null;
    private Context mContext;
    ArrayList<MonitorItem> mList = new ArrayList<MonitorItem>();
    private TextView mSearchText;
    private MonitorDeviceAdapter mMonitorAdapter;
    private Button mTestBtn;
    BluetoothManager mBleManager;
    BluetoothAdapter mBleAdapter;
    BluetoothLeScanner mBleLeScanner;
    List<ScanFilter> scanFilters;
    ScanSettings.Builder mScanSettings;
    BluetoothLeConnect mBleLeConnect;
    char[] PWD = new char[20];
    private FrameLayout mSettingLayout;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cms_monitor_scan);
        mContext = this;
        initView();
        checkToken();
        //asd
        try {
            chkIpAdr();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        setClickEvent();
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)

    private void initView(){
        mDeviceRecyclerView = findViewById(R.id.device_recycle_list);
        mDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBackImg = (ImageView)findViewById(R.id.back_img);
        mReScanImg = (ImageView)findViewById(R.id.research_img);
        mMonitorAdapter = new MonitorDeviceAdapter(mList, this);
        mDeviceRecyclerView.setAdapter(mMonitorAdapter);
        Log.d("BJY","count : "+mMonitorAdapter.getItemCount());
        setBle();
        char[] pw = new char[1];
        pw[0] = BluetoothLeConnect.MPWD_WRITE_CMD;
        String a  = ("choistec cms mon");

        char []pwd = a.toCharArray();
        System.arraycopy(pw,0,PWD,0,pw.length);
        System.arraycopy(pwd,0,PWD,pw.length,pwd.length);
    }

    
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void setBle(){
        mBleManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBleAdapter = mBleManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBleLeScanner = mBleAdapter.getBluetoothLeScanner();
        }
        startScanning();
    }
    public void startScanning() {
        Log.d("BJY","startScanning");
        AsyncTask.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                mScanSettings = new ScanSettings.Builder();
                mScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                // 얘는 스캔 주기를 2초로 줄여주는 Setting입니다.
                // 공식문서에는 위 설정을 사용할 때는 다른 설정을 하지 말고
                // 위 설정만 단독으로 사용하라고 되어 있네요 ^^
                // 위 설정이 없으면 테스트해 본 결과 약 10초 주기로 스캔을 합니다.
                ScanSettings scanSettings = mScanSettings.build();
                scanFilters = new Vector<>();
                ScanFilter.Builder scanFilter = new ScanFilter.Builder();
                ScanFilter scan = scanFilter.build();
                scanFilters.add(scan);

                mBleLeScanner.startScan(scanFilters, scanSettings, leScanCallback);
                mBleLeScanner.startScan(leScanCallback);
            }
        });
    }


    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            Log.d("BJY", "Name : " + result.getDevice().getName());
            // 게이트웨이와 스캐너 같이 검색되도록...
            if((result.getDevice().getName() != null)) {
                if (result.getDevice().getName().contains("CMS MONITOR")){
                    setDeviceInfo(result.getDevice().getName(), result.getDevice().getAddress());
                }
            }
        }
    };
    private void setDeviceInfo(String name, String address) {

        MonitorItem item = new MonitorItem();
        boolean isDuplicate = false;
        for (int i = 0; i < mList.size(); i++) {

            if (mList.get(i).getmDeviceName().equals(name)) {
                isDuplicate = true;
            } else {

            }

        }
        /*
         * 블루투스 기기 검색 이름 값이 중복 되었으면
         * 리스트 배열을 추가하지 않는다.
         */
        if (!isDuplicate) {
            item.setmDeviceName(name);
            item.setmAddr(address);
            mList.add(item);
            mMonitorAdapter.setDeviceList(mList);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMonitorAdapter.notifyDataSetChanged();
                }
            });
        }
    }
    public void stopScanning() {
        Log.d("BJY","stopping scanning");
        AsyncTask.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                mBleLeScanner.stopScan(leScanCallback);
            }
        });
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopScanning();
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopScanning();
    }

    @Override
    public void onRssiValue(int rssi, int index, String macAddress, String deviceName) {

    }

    @Override
    public void onBatteryValue(int battery, String macAddress, String deviceName) {

    }

    @Override
    public void onDeviceState(int code, String macAddress, String deviceName) {

    }

    @Override
    public void onBatteryEvent(int battery, String macAddress, String deviceName) {

    }

    @Override
    public void onClickEvent(String macAddress, String deviceName) {

    }

    @Override
    public void onReceiveData(String data) {

    }

    @Override
    public void onInputState(String data) {

    }

    @Override
    public void onCmdDisConnect() {

    }

    @Override
    public void onReadSuccess(int what, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onWriteSuccess(int what) {

    }

    private void setClickEvent(){
        mBackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });
        mReScanImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanning();
                startScanning();
                //TODO ReScanning !!!
            }
        });
        mMonitorAdapter.setClick123(new MonitorDeviceAdapter.callback() {
            @Override
            public void check(MonitorItem item) {
                stopScanning();
                item.getmAddr();
                item.getmDeviceName();
                item.getScannerId();

                mBleLeConnect = new BluetoothLeConnect();
//                mBleLeConnect.setBluetoothCallBackListener(CmsMonitorScanActivity.this);
                boolean conStatus = mBleLeConnect.initialize(mContext, 0,item.getmDeviceName(), item.getmAddr(),0,PWD);
                if(conStatus == false){
                    Log.d("BJY","BLE Connect is Fail");
                }else{
                    Log.d("BJY","BLE Connect is Success");

                    mDeviceRecyclerView.setVisibility(View.GONE);
                    mSettingLayout = (FrameLayout)findViewById(R.id.fram_layout);
                    FragmentCmsMonitor fragmentMonitor = new FragmentCmsMonitor(CmsMonitorScanActivity.this, mBleLeConnect,mContext);
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.fram_layout, fragmentMonitor);
                    transaction.commit();
                }
            }
        });
    }
    private String chkIpAdr() throws SocketException {
        for( Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
            NetworkInterface intf = en.nextElement();
            for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();){
                InetAddress inetAddress= enumIpAddr.nextElement();
                if(inetAddress.isLoopbackAddress()){
                    Log.d("cht"," value : "+intf.getDisplayName()+" // value 2 : "+inetAddress.getHostAddress());
                }else{
                    Log.d("cht"," value3 : "+intf.getDisplayName()+" // value 4 : "+inetAddress.getHostAddress());
                }

                if(!inetAddress.isLoopbackAddress() && !inetAddress.getHostAddress().contains(":")){
                    Log.d("cht","value 5 : "+inetAddress.getHostAddress());
                }
            }
        }
            return null;
    }
    private void checkToken(){

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()){
                            Log.d("BJY","Fetching FCM registration token failed"+task.getException());
                            return;
                        }

                        String token = task.getResult();
                        SharedPreferences prefs= getPreferences(MODE_PRIVATE);
                        String prefToken = prefs.getString("token","");


                        if(prefToken.equals("token123")){
                            Log.d("BJY","token Equals  value : "+token );
                        }else{
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("token",token);
                            writeToken(token);
                            Log.d("BJY","token New value : "+token);
                        }
                    }
                });

    }
    private void writeToken(String token){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http:127.0.0.1:3000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        HashMap<String, Object> map = new HashMap<>();
        map.put("token",token);
        map.put("group_id","xpointer");
        ChoisHttpApi choisHttpApi = retrofit.create(ChoisHttpApi.class);
        choisHttpApi.postData(map).enqueue(new Callback<tokenModel>() {
            @Override
            public void onResponse(Call<tokenModel> call, Response<tokenModel> response) {
                if(response.isSuccessful()){
                    tokenModel data = response.body();
                    Log.d("BJY","TEST SUCCESS !!!");
                }
            }

            @Override
            public void onFailure(Call<tokenModel> call, Throwable t) {
                Log.d("BJY","TEST FAIE!!!!");
            }
        });
//        disposable.add(service.getUserToken()
//                        .subscribeOn(AndroidSchedulers.mainThread())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribeWith(new DisposableSingleObserver<tokenModel>(){
//
//                            @Override
//                            public void onSuccess(@io.reactivex.annotations.NonNull tokenModel tokenModel) {
//                                tokenModel.setToken(tokenModel.getToken());
//                                tokenModel.setUserId(tokenModel.getUserId());
//                                disposable.clear();
//                            }
//
//                            @Override
//                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
//                                e.printStackTrace();
//                                disposable.clear();
//                            }
//                        }));

    }

}
