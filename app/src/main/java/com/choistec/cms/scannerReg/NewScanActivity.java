package com.choistec.cms.scannerReg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;

import com.choistec.cms.scannerReg.util.CommonInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by Chois on 2017-08-25.
 */

public class NewScanActivity extends Activity {

    private ListView listview;

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    private com.choistec.cms.scannerReg.DeviceListAdapter mAdapter;
    private ArrayList<com.choistec.cms.scannerReg.DeviceItem> mDeviceItemArr = new ArrayList<>();

    //선택한 비콘 넘버 배열 위치 값
    private int selectBeaconPos;
    //비콘 명칭 입력 다이얼로그 선언
    AlertDialog dialog;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Button close_btn = (Button) findViewById(R.id.close_btn);

        /*스캔 리스트뷰*/
        listview = (ListView) findViewById(R.id.listview);
        /*스캔 리스트뷰 어뎁터*/
        mAdapter = new com.choistec.cms.scannerReg.DeviceListAdapter(NewScanActivity.this, mDeviceItemArr, mHandler);
        listview.setAdapter(mAdapter);


        close_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });


        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btScanner = btAdapter.getBluetoothLeScanner();
        }

        startScanning();


    }

    List<ScanFilter> scanFilters;
    ScanSettings.Builder mScanSettings;

    public void startScanning() {
        System.out.println("start scanning");

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
//                scanFilter.setDeviceName("CMS SCANNER");
//                scanFilter.setDeviceName("CMSGateway");
                scanFilter.setServiceUuid(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
                ScanFilter scan = scanFilter.build();
                scanFilters.add(scan);

                btScanner.startScan(scanFilters, scanSettings, leScanCallback);
                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        AsyncTask.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }


    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
                    Log.d("BJY", "Name : " + result.getDevice().getName());
                    // 게이트웨이와 스캐너 같이 검색되도록...
                    if((result.getDevice().getName() != null)) {
                        if ((result.getDevice().getName().equals("CMS SCANNER")) || (result.getDevice().getName().equals("CMSGateway"))) {
                            setDeviceInfo(result.getDevice().getName(), result.getDevice().getAddress());
                        }
                    }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("yong","NewScanActivity onDestroy()");
        /*종료 될 때 다이얼로그를 dismiss 시킨다.*/
        if (dialog != null) {
            dialog.dismiss();
        }
//        stopScanning();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    /**
     * 디바이스 정보를 mDeviceItemArr 배열에 담는다.
     *
     * @param name    이름
     * @param address 주소
     */
    private void setDeviceInfo(String name, String address) {

        com.choistec.cms.scannerReg.DeviceItem item = new com.choistec.cms.scannerReg.DeviceItem();

        boolean isDuplicate = false;
        for (int i = 0; i < mDeviceItemArr.size(); i++) {

            if (mDeviceItemArr.get(i).getScannerId().equals(name)) {
                isDuplicate = true;
            } else {

            }

        }
        /*
        * 블루투스 기기 검색 이름 값이 중복 되었으면
        * 리스트 배열을 추가하지 않는다.
        */
        if (!isDuplicate) {
            item.setScannerId(name);
            item.setScannerMac(address);
            mDeviceItemArr.add(item);
            mAdapter.setmDeviceItemArr(mDeviceItemArr);
            mAdapter.notifyDataSetChanged();
        }

    }

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                /*비콘 디바이스 선택 이벤트 핸들러*/
                case CommonInfo.SCAN_DEVICE_SELECT:
                    Intent intent = new Intent();
                    /*디바이스 이름*/
                    intent.putExtra(getString(R.string.beacon_name), mDeviceItemArr.get(selectBeaconPos).getScannerId());
                    /*디바이스 맥주소*/
                    intent.putExtra(getString(R.string.beacon_mac), mDeviceItemArr.get(selectBeaconPos).getScannerMac());
                    setResult(RESULT_OK, intent);
                    stopScanning();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                    break;

            }

        }
    };


}

