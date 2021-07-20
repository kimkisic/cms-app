/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.choistec.cms.scannerReg.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.choistec.cms.scannerReg.DeviceItem;
import com.choistec.cms.scannerReg.R;
import com.choistec.cms.scannerReg.util.CommonInfo;
import com.choistec.cms.scannerReg.util.CommonUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeConnect {
    private final static String TAG = "myLog";
    public static String CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    public final static int STATE_CONNECTING = 1000;
    /*블루투스 연결 상수*/
    public final static int STATE_CONNECTED = 1001;
    /*블루투스 비연결 상수*/
    public final static int STATE_DISCONNECTED = 1002;
    public final static int SERVICES_DISCOVERED = 1003;
    public final static int STATE_DIVICE_OFF = 1004;
    public final static int MPWD = 7777;
    public final static int MMAC = 7778;
    public final static int MUNIT = 7779;
    public final static int MINTERVAL = 7780;
    public final static int MTEMP = 7781;

    public final static byte MPWD_WRITE_CMD = 0x1F;
    public final static byte MMAC_WRITE_CMD = 0x0F;
    public final static byte MUNIT_WRITE_CMD = 0x0E;
    public final static byte MINTERVAL_WRITE_CMD = 0x0C;
    public final static byte MTEMP_WRITE_CMD = 0x0D;

    public final static byte MMAC_READ_CMD = 0x2F;
    public final static byte MUNIT_READ_CMD = 0x2E;
    public final static byte MINTERVAL_READ_CMD = 0x2C;
    public final static byte MTEMP_READ_CMD = 0x2D;
    private int LOW_BATTERY = 75;
    private int TIMEOUT = 60;

    private int reConnectTime = 0;

    /*테스트를 위해 전역으로 선언*/
    public BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mBatteryCharacteristic = null;
    private BluetoothGattCharacteristic mClickCharacteristic = null;

    /*통신 캐릭터 변수 선언*/
    private BluetoothGattCharacteristic fff1_Characteristic = null;
    private BluetoothGattCharacteristic fff4_Characteristic = null;
    private BluetoothGattCharacteristic fff3_Characteristic = null;
    //rssi조회 타이머
    private Timer mRssiTimer;
    private BluetoothDevice device;
    private BluetoothGattCharacteristic characteristic = null;
    private String mBluetoothDeviceAddress;

    /*Rssi 데이터 조회 시간을 설정한다.*/
    private static final int RSSI_DELAY = 1000;
    /*테스트를 위해 전역으로 선언*/
    public boolean isDestory = false;
    /*블루 투스LE 콜백 객체 변수*/
    public BluetoothCallBack mListener = null;
    /*평균값 변수 선언*/
    int avgCount = 0;
    /*평균값 합 변수 선언*/
    int avgSum = 0;
    /*추가 데이터 있는지 확인 플래그*/
    boolean isOver = false;
    /*읽어온 20 + 20 바이트 배열 붙인 데이터*/
    byte[] full_data = new byte[40];
    char[] mPwd ;
    public void setBluetoothCallBackListener(BluetoothCallBack listener) {
        mListener = listener;
    }

    private String sDeviceName = "";
    private Context mContext;

    /*비콘 배열 위치 값*/
    private int nIndex = -1;
    private BluetoothGattCharacteristic mFFF1 = null;

    /**
     * 클래스 실행시 첫번째로 셋팅되는 부분
     *
     * @param context    호출한 클래스의 context 값
     * @param index      연결하려는 디바이스 배열 값의 index 값
     * @param name       연결하려는 디바이스 이름
     * @param mac        연결하려는 디바이스 맥주소
     * @param lowBattery 배터리
     */
    public boolean initialize(Context context, int index, String name, String mac, int lowBattery, char[] _Pwd) {
        /*끊겼을 때 1초에 한번 씩 Reconnect를 시도한다.*/
        //롤리팝 이상
        boolean status = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reConnectTime = 100;
        } else {
            reConnectTime = 100;
        }
        mPwd = _Pwd;
        sDeviceName = name;
        mContext = context;
        LOW_BATTERY = lowBattery;
        /*연결된 블루투스 배열 위치 값을 받는다*/
        nIndex = index;
        status = btInitialize();
        status = btConnect(mac);
        Log.d("BJY"," ble connect is succees ? "+status);
        CallbackItem callbackItem = new CallbackItem();
        callbackItem.setDeviceName(name);
        callbackItem.setDeviceName(mac);

        return status;
    }

    /**
     * 비콘 연결을 종료시킨다.
     */
    public void stop() {
        disconnect();

        mBluetoothDeviceAddress = "";
        try {
            if (mRssiTimer != null) {
                mRssiTimer.cancel();
                mRssiTimer = null;
            }
        } catch (Exception e) {
        }
        isDestory = true;
    }


    /**
     * 블루투스 연결을 종료시킨다.
     */
    private void disConnect() {

        reConnectTime = 100000000;

        close();

        mBluetoothGatt = null;
        mBluetoothDeviceAddress = "";
        characteristic = null;

        device = null;
        mConnectHandler.removeCallbacks(runnable);
        mConnectHandler.removeCallbacksAndMessages(null);
        runnable = null;
        mBatteryCharacteristic = null;

        try {
            if (mRssiTimer != null) {
                mRssiTimer.cancel();
                mRssiTimer = null;
            }
        } catch (Exception e) {
        }

        Log.d("myLog", "disConnectdisConnectdisConnect");
    }

    /**
     * 블루투스 콜백 함수 선언
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /**
         * 콜백 함수에서 onReadRemoteRssi를 오버라이드한다.
         * @param gatt 콜백 함수
         * @param rssi 블루투스 수신 감도 값
         * @param status 상태코드
         */

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                /*로그 값을 출력한다.*/
                Log.d("onReadRemoteRssi ", "Test Device Name " + device.getName() +
                        " Rssi :" + Integer.toString(rssi));
                rssiCallback(rssi);

            }
        }

        /**
         * 디바이스 연결 상태 변경 리스너
         * @param gatt BluetoothGatt 콜백함수
         * @param status 과거 status 넘버
         * @param newState 새롭게 바뀐 status 넘버
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.i("BJY", String.valueOf(status) + " compare " + BluetoothGatt.GATT_SUCCESS);
            Log.i("BJY", "newState " + String.valueOf(newState));
            switch (newState) {
                /*비콘이 연결 되었을 때*/
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d("BJY","BluetoothProfile.STATE_CONNECTED: Connected");
                    Message msg = handler2.obtainMessage();
                    msg.arg1 = 2;
                    handler2.sendMessage(msg);
                    mBluetoothGatt.discoverServices();
                    /*블루투스 접속 시도하는 핸들러를 취소 시킨다.*/
                    mConnectHandler.removeCallbacks(runnable);
                    /*자동연결 처리 취소*/
                    mConnectHandler.removeCallbacksAndMessages(null);
                    /*연결 되었으면 연결 끊김을 알려주는 disConnRunnable을 중지 시킨다.*/
                    mDisconnectCheckHandler.removeCallbacks(disConnRunnable);
                    /*연결끊김 타임아웃 취소*/
                    mDisconnectCheckHandler.removeCallbacksAndMessages(null);
                    /*테스트를 위해 주석처리*/
                    /*정해놓은 시간 만큼 Rssi Timer를 실행 시킨다.*/
                    //setRssiTimer();
                    /*로그를 찍는다.*/
                    Log.d("BJY", "STATE_CONNECTED");

                    break;
                /*비콘 연결이 끊겼을 때*/
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d("BJY","BluetoothProfile.STATE_DISCONNECTED: Connected");

                    if (isDestory == true) {
                        /*블루투스 연결을 종료시킨다.*/
                        disConnect();
                        msg = handler2.obtainMessage();
                        msg.arg1 = 3;
                        handler2.sendMessage(msg);
                        return;
                    }
                    msg = handler2.obtainMessage();
                    msg.arg1 = 3;
                    handler2.sendMessage(msg);
                    /*연결 끊김 상태를*/
                    /*onDeviceState으로 브로드 캐스트 콜백을 날린다.*/
                    deviceStateCallback(mBluetoothDeviceAddress, STATE_DISCONNECTED); //연결끊김 알림
                    mBluetoothGatt.disconnect();
                    /*BluetoothGatt 객체 함수에 close를 실행 시킨다.*/
                    close();
                    /*재연결 핸들러를 초기화 후 재연결을 호출한다.*/
                    reConnectProc();
                    /*로그를 찍는다.*/
                    Log.d("BJY", "STATE_DISCONNECTED");
                    mListener.onCmdDisConnect();
                    break;
            }
        }


        /**
         * 서비스 발견 리스너
         * @param gatt BluetoothGatt 콜백 함수
         * @param status 상태 코드
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("BJY","onSerivcesDiscoverd !!  / status : "+status);
            List<BluetoothGattService> service;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                service = gatt.getServices();
                /*gattServices 가 특정 이벤트 프로토콜과 맞으면 콜백함수를 셋팅한다.*/
                displayGattServices(getSupportedGattServices());
                service.forEach(new Consumer<BluetoothGattService>() {
                    @Override
                    public void accept(BluetoothGattService bluetoothGattService) {
                        Log.d("bbjjyy"," value : "+bluetoothGattService.getUuid().toString());
                        if (bluetoothGattService.getUuid().toString().contains("0000fff0")) {
                            bluetoothGattService.getCharacteristics().forEach(new Consumer<BluetoothGattCharacteristic>() {
                                @Override
                                public void accept(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                                    if(bluetoothGattCharacteristic.getUuid().toString().contains("0000fff3")){
                                        byte[] by = chagneChar(mPwd);
                                        bluetoothGattCharacteristic.setValue(by);
                                        writeCharacteristic(bluetoothGattCharacteristic,"Pwd write");
                                    }else if(bluetoothGattCharacteristic.getUuid().toString().contains("0000fff4")){
                                        try{
                                            Log.d("BJY","discoverdService set Descriptor !!!");
                                            boolean success = setCharacteristicNotification(bluetoothGattCharacteristic,true);
//                                            mBluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
//                                            BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//                                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                                            boolean success = mBluetoothGatt.writeDescriptor(descriptor);
                                            Log.d("BJY","writeDescriptor : "+ success);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            });
                        }
                    }
                });
//                    Log.d("BJY","BleLe Connect item value : "+);

//                        Log.d("bbjjyy",""+item.getUuid().toString().toLowerCase(Locale.US).split("-")));


//                if(){
//                    Log.d("hello",""+gatt.getServices().toString().toLowerCase(Locale.US).startsWith("0000fff0"));
//                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * gattServices 가 특정 이벤트 프로토콜과 맞으면 콜백함수를 셋팅한다.
         * @param gattServices 콜백 함수
         */
        private void displayGattServices(List<BluetoothGattService> gattServices) {
            if (gattServices == null) return;
            /*serviceUUID 가 특정 프로토콜과 맞으면 콜백 함수를 셋팅한다.*/
            GattService(gattServices);
        }

        /**
         * 내부 값이 업데이트 되면 리스너가 실행된다.
         * @param gatt 콜백함수
         * @param characteristic 이벤트 발생 객체
         * @param status 이벤트 발생 코드
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("BJY","BluetoothLeConnect onCharacteristicRead !!!");
            if (status == BluetoothGatt.GATT_SUCCESS) {

                getBattery(characteristic, mBluetoothDeviceAddress);
            }else{
                Log.d("BJY","BluetoothLeConnect onCharacteristicRead FAIL !!!");
            }
        }

        /**
         * setCharacteristicNotification 에 등록한
         * characteristic 이 변화 될 때 작동된다.
         * @param gatt BluetoothGatt 콜백 함수
         * @param characteristic BluetoothGattCharacteristic 특성 멤버 클래스 변수
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("BJY","BluetoothLeConnect onCharacteristicChanged !!!");
            byte[] data = characteristic.getValue();
            Log.d("BJY","data value : "+data[0]);
            if(data[0] == 0x0F || data[0] == 0x0E || data[0] == 0x0D || data[0] == 0x0C){
                switch (data[0]){
                    case MMAC_WRITE_CMD:
                        mListener.onReadSuccess(MMAC, characteristic);
                        Log.d("BJY"," onCharacteristicChanged MMAC !!!");
                        break;
                    case MTEMP_WRITE_CMD:
                        mListener.onReadSuccess(MTEMP, characteristic);
                        Log.d("BJY","onCharacteristicChanged MTEMP !!!");
                        break;
                    case MINTERVAL_WRITE_CMD:
                        mListener.onReadSuccess(MINTERVAL, characteristic);
                        Log.d("BJY","onCharacteristicChanged MINTERVAL !!!");
                        break;
                    case MUNIT_WRITE_CMD:
                        mListener.onReadSuccess(MUNIT, characteristic);
                        Log.d("BJY","onCharacteristicChanged MUNIT !!!");
                        break;
                }

            }else{
                if (data[3] > 16) {
                    System.arraycopy(data, 0, full_data, 0, 20);
                    isOver = true;
                } else {
                    Message msg = handler2.obtainMessage();
                    msg.arg1 = 1;
                    if (isOver) {
                        System.arraycopy(data, 0, full_data, 20, 20);
                        msg.obj = (Object) full_data;
                        full_data = new byte[40];
                        isOver = false;
                    } else {
                        msg.obj = (Object) data;
                    }
                    handler2.sendMessage(msg);
                }

                for (int i = 0; i < 20; i ++) {
                    Log.i("yong", "byte[" + i + "]: " + data[i]);
                }
            }
            //읽어온 데이터의 길이가 16이상일 경우(데이터가 2번에 나뉘어져 보내져오는데, 두번째 데이터가 유효할 경우)
            //첫번째, 두번째 데이터를 합쳐서 메시지 전달
            //아니면 첫번째 데이터만 메시지 전달

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            super.onCharacteristicWrite(gatt, characteristic, status);
            if(status == 0){
                byte[] bytes = characteristic.getValue();
                if(bytes[0] == MMAC_WRITE_CMD){
                    Log.d("BJY","BluetootheLeConnect MAC Write SuccessWrite !!!");
                    mListener.onWriteSuccess(0);
                }else if(bytes[0] == MUNIT_WRITE_CMD){
                    Log.d("BJY","BluetootheLeConnect UNIT Write SuccessWrite !!!");
                    mListener.onWriteSuccess(1);
                }else if(bytes[0] == MINTERVAL_WRITE_CMD){
                    Log.d("BJY","BluetootheLeConnect INTERVAL Write SuccessWrite !!!");
                    mListener.onWriteSuccess(2);
                }else if(bytes[0] == MTEMP_WRITE_CMD){
                    Log.d("BJY","BluetootheLeConnect TEMP Write SuccessWrite !!!");
                    mListener.onWriteSuccess(3);
                }
            }
        }

    };

    /**
     * 재연결 핸들러를 초기화 후 재연결을 호출한다.
     */
    private void reConnectProc() {
        /*블루투스 접속 시도하는 핸들러를*/
        /*중지 시킨다.*/
        mConnectHandler.removeCallbacks(runnable);
        mConnectHandler.removeCallbacksAndMessages(null);
        /*블루투스를 재 연결한다.*/
        btReConnect();
    }

    /**
     * 블루투스 매니저 객체 변수를 초기화한다.
     *
     * @return
     */
    public boolean btInitialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Bluetooth LE 장치에 호스트 된 GATT 서버에 연결합니다.
     *
     * @param address 대상 장치의 장치 주소입니다.
     * @return
     */
    public boolean btConnect(final String address) {

        if (mBluetoothAdapter == null || address == null) {
            Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }


        if (mBluetoothDeviceAddress != null && CommonUtil.nvl(address).equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            //Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            //mBluetoothGatt.connect();
            return false;
        }

        /*if(mBluetoothGatt.connect()) {
            deviceStateCallback(address, STATE_CONNECTING);
            return true;
        }else{
            return false;
        }*/

        device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothDeviceAddress = address;
        /*블루투스 연결 커넥트에 콜백 함수를 넣는다.*/
        /*블루투스 접속을 시도한다.*/
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        if(mBluetoothGatt == null){
            Log.w(TAG, "mBluetoothGatt is Null !!!");
            return false;
        }
        Log.d("BJY", "연결 시도" + mBluetoothDeviceAddress );

        return true;
    }

    /**
     * BluetoothGatt 객체 함수에 disconnect를 실행 시킨다.
     */
    public void disconnect() {
        try {
            mBluetoothGatt.disconnect();
        } catch (Exception e) {
        }
    }

    /**
     * BluetoothGatt 객체 함수에 close를 실행 시킨다.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * 배터리 콜백 함수를 정의한다.
     *
     * @param characteristic 배터리 콜백 함수
     */
    public void setBatteryCharacteristic(BluetoothGattCharacteristic characteristic) {
        mBatteryCharacteristic = characteristic;
    }

    /**
     * 강제적으로 배터리 레벨을 요청한다.
     */
    public void read_battery_level() {
        if (mBatteryCharacteristic != null) {
            mBluetoothGatt.readCharacteristic(mBatteryCharacteristic);
        }
    }


    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }


    private Handler mConnectHandler = new Handler();

    /**
     * 블루투스를 재 연결한다.
     */
    public void btReConnect() {
        try {
            /*블루투스 연결 성공 여부를 체크한다.*/
            if (!btConnect(mBluetoothDeviceAddress)) {
                /*실패하면 블루투스 접속 시도하는 핸들러를*/
                /*실행 시킨다.*/
                try {
                    /*4.4는 0.3초, 5.0 이상은 1초로 설정한다.*/
                    mConnectHandler.postDelayed(runnable, reConnectTime);
                } catch (Exception e) {
                }

            } else {
                /*연결에 성공한 경우 핸들러 작업 중단한다.*/
                mConnectHandler.removeCallbacks(runnable);
                mConnectHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
        }

    }

    /**
     * 블루투스 접속 시도하는 핸들러
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            try {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    close();
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
                    mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
                    mBluetoothGatt.connect();
                    /*로그를 출력한다.*/
                    Log.d("runnable", "connect");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            btReConnect();
        }
    };


    private Handler mHandler = new Handler();

    /*테스트를 위해 주석처리*/
    /**
     * 연결 끊김을 알려주는 핸들러
     */
    Runnable disConnRunnable = new Runnable() {
        @Override
        public void run() {
            deviceStateCallback(mBluetoothDeviceAddress, STATE_DISCONNECTED); //연결끊김 알림
        }
    };

    private Handler mDisconnectCheckHandler = new Handler();

    /**
     * serviceUUID 가 특정 프로토콜과 맞으면 콜백 함수를 셋팅한다.
     * UUID : 원하는 정보를 제공하는 함수 고유 아이디
     *
     * @param gattServices Bluetooth 콜백 함수
     */
    private void GattService(List<BluetoothGattService> gattServices) {

        for (BluetoothGattService svc : gattServices) {
            String uuidStrService = svc.getUuid().toString();

//            Log.d("mylog", "첫번째 캐릭터 : " + svc.getCharacteristics());

            String[] serviceUUID = uuidStrService.split("-");

            if (serviceUUID.length == 0) {
                continue;
            }

            if (CommonUtil.nvl(serviceUUID[0]).toUpperCase().equals("0000FFF0")) {
                List<BluetoothGattCharacteristic> listChar = svc.getCharacteristics();

//                Log.d("mylog", "두번째 캐릭터 : " + svc.getCharacteristics().toString());

                for (BluetoothGattCharacteristic characteristic : listChar) {

//                    Log.d("mylog", "세번째 캐릭터 : " + svc.getCharacteristics().toString());

                    String[] CharacterUUID = characteristic.getUuid().toString().split("-");

                    if (CharacterUUID.length == 0) {
                        continue;
                    }

                    /*스캐너 내용 확인 명령 캐릭터*/
                    if (CommonUtil.nvl(CharacterUUID[0]).toUpperCase().equals("0000FFF1")) {
                        //mClickCharacteristic = characteristic;
                        fff1_Characteristic = characteristic;
                        Log.d("mylog", fff1_Characteristic.toString());
                        if (mBluetoothGatt == null) {
                            return;
                        }
                    }

                    /*스캐너 내용 입력 캐릭터*/
                    if (CommonUtil.nvl(CharacterUUID[0]).toUpperCase().equals("0000FFF3")) {

                        fff3_Characteristic = characteristic;
                        if (mBluetoothGatt == null) {
                            return;
                        }

                    }

                    /*
                     * 스캐너 내용 확인 캐릭터
                     * 이 부분은 Notify 로 특성을 넣는다.
                     */
//                    if (CommonUtil.nvl(CharacterUUID[0]).toUpperCase().equals("0000FFF4")) {
//
//                        fff4_Characteristic = characteristic;
//                        Log.d("BJY", "뭔지는 모르겠지만 notification 등록"+fff4_Characteristic.toString());
//                        if (mBluetoothGatt == null) {
//                            return;
//                        }
//                        setCharacteristicNotification(fff4_Characteristic, true);
//
//                    }

                }

            }

            /*블루투스 콜백 함수 중 배터리 리스너 uuid를 찾는다.*/
            if (svc.getUuid().toString().toLowerCase(Locale.US).startsWith("0000180f")) {

                List<BluetoothGattCharacteristic> listChar = svc.getCharacteristics();

                for (BluetoothGattCharacteristic characteristic : listChar) {
                    String uuidStr = characteristic.getUuid().toString();

                    if (uuidStr.toLowerCase(Locale.US).startsWith("00002a19")) {
                        /*배터리 값을 받으면*/
                        //readCharacteristic(characteristic);
                        setBatteryCharacteristic(characteristic);
                    }
                }
            }


        }
    }

    /**
     * Wifi 데이터를 불러오는 함수
     */
    public void readWifiData() {
        //fff1_Characteristic 값이 널이 아닐 때만 명령을 동작한다.
        if(fff1_Characteristic != null) {
            byte[] bytes = new byte[]{(byte) 0xFF};
            fff1_Characteristic.setValue(bytes);
            writeCharacteristic(fff1_Characteristic, "0xFF 입력 성공 여부");
        }
    }

    //입력 데이터 전송 성공 여부 변수
    public boolean isSendSuccess2;

    /**
     * 스캐너에 정보를 입력하는 함수
     */
    public void sendScannerData(DeviceItem deviceItem, int securityLevel) {
        isSendSuccess2 = true;
        byte[] sec_byte = new byte[0];
//        if (isEnt) {
            /*엔터프라이즈*/
            /**
             * TODO: EAP VALUE?
             */
//            switch (secType) {
//                case "WPA":
//                case "WPA2":
//                    sendScannerInfo((byte) 0xF0, "15?", 0l, mContext.getString(R.string.input_progress_ip_input_0_percent));
//                    break;
//            }
//        } else {
            Log.i("yong", "securityLevel: " + securityLevel);
            /*일반 보안*/
            switch (securityLevel) {
                case 0:
                    Log.d("yong", "OPEN entered");
                    deviceItem.setWifiPW("");
                    break;
                case 1:
                    Log.d("yong", "WPA/WPA2 entered");
                    sec_byte = new byte[]{0x00, 0x02};
                    break;
//            }
        }
        /*와이파이 보안 타입 입력*/
        sendScannerInfo((byte) 0xF0, sec_byte, 0l, mContext.getString(R.string.input_progress_ip_input_0_percent));
        sec_byte = new byte[0];
        Log.i("yong", "scannerInfo\n" + "ssid: " + deviceItem.getWifiSsid() + "\npw: " + deviceItem.getWifiPW() + "\nmqtt: " + deviceItem.getMqttIp() + "\n");
        /*와이파이 이름 입력*/
        sendScannerInfo((byte) 0xFE, deviceItem.getWifiSsid(), 6000l, mContext.getString(R.string.input_progress_ip_input_10_percent));
        /*와이파이 패스워드 입력*/
        sendScannerInfo((byte) 0xFD, deviceItem.getWifiPW(), 12000l, mContext.getString(R.string.input_progress_ip_pw_input_40_percent));
        /*MQTT 주소 입력*/
        sendScannerInfo((byte) 0xFF, deviceItem.getMqttIp(), 18000l, mContext.getString(R.string.input_progress_mqtt_ip_input_70_percent));

        //연결 끊기 명령
        TimerTask mTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = handler2.obtainMessage();
                msg.arg1 = 5;
                handler2.sendMessage(msg);
                Log.d("myLog", "스캐너 연결 해제");
            }
        };
        Timer mTimer = new Timer();
        mTimer.schedule(mTask, 18000l + 6000);

    }

    /**
     * 스캐너 정보 전송 함수 (byte[])
     * @param firstByte
     * @param byteStr
     * @param delay
     * @param logMsg
     */
    public void sendScannerInfo(final byte firstByte, byte[] byteStr, long delay, final String logMsg) {
        final byte[] bytes = new byte[20];

        final byte[] strBytes = byteStr;
        Log.d("myLog", "자리수 : " + strBytes.length);
        Log.i("yong", "before " + byteStr);
        Log.i("yong", "after " + strBytes);

        for (int i = 0; i < 20; i++) {

            switch (i) {
                case 0:
                    bytes[i] = firstByte;
                    break;
                case 1:
                    if (strBytes.length > 16) {
                        bytes[i] = intToBytes(16, 1);
                    } else {
                        bytes[i] = intToBytes(strBytes.length, 1);
                    }
//                    bytes[i] = intToBytes(strBytes.length, 1);
                    break;
                case 2:
                    bytes[i] = (byte) 0x00;
                case 3:
                    bytes[i] = (byte) 0x01;
                    break;
            }

            Log.d("myLog", "자리수2 : " + strBytes.length);

            if (i > 3 && i < 20) {
                if (i < (strBytes.length + 4)) {
                    bytes[i] = strBytes[i - 4];
                } else {
                    bytes[i] = (byte) 0x00;
                }
            }
        }
        Log.d("myLog", "자리수3 : " + strBytes.length);
        Log.d("myLog", "바이트1 : " + bytes.length);
        Log.i("yong", "byte1 to_str: " + new String(bytes, StandardCharsets.UTF_8));

        TimerTask mTask = new TimerTask() {
            @Override
            public void run() {

                String s2 = null;
                try {
                    s2 = new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d("myLog", s2);
                Log.d("myLog", String.valueOf(s2.length()));
                //fff3_Characteristic 이 널값이 아니면 통과 시킨다.
                if(fff3_Characteristic != null) {
                    fff3_Characteristic.setValue(bytes);
                    writeCharacteristic(fff3_Characteristic, logMsg);
                }

            }
        };

        Log.d("myLog", "자리수4 : " + strBytes.length);

        Timer mTimer = new Timer();
        mTimer.schedule(mTask, delay);


        final byte[] bytes2 = new byte[20];
        for (int i = 0; i < 20; i++) {

            switch (i) {
                case 0:
                    bytes2[i] = firstByte;
                    break;
                case 1:
                    /* 두번째 데이터에서 스트링 길이가
                     *  16 보다 크면 두번 째 바이트 값에 길이 값을 넣고
                     *  아니면 0 값을 넣는다.
                     */
                    if (strBytes.length > 16) {
                        bytes2[i] = intToBytes(strBytes.length - 16, 1);
                    } else {
                        bytes2[i] = (byte) 0x00;
                    }
                    break;
                case 2:
                    bytes2[i] = (byte) 0x00;
                case 3:
                    bytes2[i] = (byte) 0x02;
                    break;
            }
            Log.d("myLog", "자리수5 : " + strBytes.length);

            if (i > 3 && i < 20) {
                if (i < ((strBytes.length - 16) + 4)) {
                    bytes2[i] = strBytes[(i - 4) + 16];
                } else {
                    bytes2[i] = (byte) 0x00;
                }
            }
        }
        Log.i("yong", "byte2 to_str: " + new String(bytes2, StandardCharsets.UTF_8));

        /*3초뒤에 작업을 진행한다*/
        /*와이파이 이름 정보2 전송*/
        mTask = new TimerTask() {
            @Override
            public void run() {
                String s2 = null;
                try {
                    s2 = new String(bytes2, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d("myLog", "두번째 패킷 값 : " +  s2);
                Log.d("myLog", "제발요 : " + bytes2.length);
                fff3_Characteristic.setValue(bytes2);
                writeCharacteristic(fff3_Characteristic, logMsg);
                Log.d("myLog", logMsg + " 두 번째 20 바이트 전송");
            }
        };
        Log.d("myLog", "자리수6 : " + strBytes.length);

        mTimer = new Timer();
        mTimer.schedule(mTask, delay + 3000);
    }

    /**
     * 스캐너 정보 전송 함수 (String)
     * @param firstByte
     * @param byteStr
     * @param delay
     * @param logMsg
     */
    public void sendScannerInfo(final byte firstByte, String byteStr, long delay, final String logMsg) {
        final byte[] bytes = new byte[20];

        final byte[] strBytes = byteStr.getBytes(Charset.forName("UTF-8"));
        Log.d("myLog", "자리수 : " + strBytes.length);
        Log.i("yong", "before " + byteStr);
        Log.i("yong", "after " + strBytes);

        for (int i = 0; i < 20; i++) {

            switch (i) {
                case 0:
                    bytes[i] = firstByte;
                    break;
                case 1:
                    if (strBytes.length > 16) {
                        bytes[i] = intToBytes(16, 1);
                    } else {
                        bytes[i] = intToBytes(strBytes.length, 1);
                    }
//                    bytes[i] = intToBytes(strBytes.length, 1);
                    break;
                case 2:
                    bytes[i] = (byte) 0x00;
                case 3:
                    bytes[i] = (byte) 0x01;
                    break;
            }

            Log.d("myLog", "자리수2 : " + strBytes.length);

            if (i > 3 && i < 20) {
                if (i < (strBytes.length + 4)) {
                    bytes[i] = strBytes[i - 4];
                } else {
                    bytes[i] = (byte) 0x00;
                }
            }
        }
        Log.d("myLog", "자리수3 : " + strBytes.length);
        Log.d("myLog", "바이트1 : " + bytes.length);
        Log.i("yong", "byte1 to_str: " + new String(bytes, StandardCharsets.UTF_8));

        TimerTask mTask = new TimerTask() {
            @Override
            public void run() {

                String s2 = null;
                try {
                    s2 = new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d("myLog", s2);
                Log.d("myLog", String.valueOf(s2.length()));
                //fff3_Characteristic 이 널값이 아니면 통과 시킨다.
                if(fff3_Characteristic != null) {
                    fff3_Characteristic.setValue(bytes);
                    writeCharacteristic(fff3_Characteristic, logMsg);
                }

            }
        };

        Log.d("myLog", "자리수4 : " + strBytes.length);

        Timer mTimer = new Timer();
        mTimer.schedule(mTask, delay);


        final byte[] bytes2 = new byte[20];
        for (int i = 0; i < 20; i++) {

            switch (i) {
                case 0:
                    bytes2[i] = firstByte;
                    break;
                case 1:
                    /* 두번째 데이터에서 스트링 길이가
                     *  16 보다 크면 두번 째 바이트 값에 길이 값을 넣고
                     *  아니면 0 값을 넣는다.
                     */
                    if (strBytes.length > 16) {
                        bytes2[i] = intToBytes(strBytes.length - 16, 1);
                    } else {
                        bytes2[i] = (byte) 0x00;
                    }
                    break;
                case 2:
                    bytes2[i] = (byte) 0x00;
                case 3:
                    bytes2[i] = (byte) 0x02;
                    break;
            }
            Log.d("myLog", "자리수5 : " + strBytes.length);

            if (i > 3 && i < 20) {
                if (i < ((strBytes.length - 16) + 4)) {
                    bytes2[i] = strBytes[(i - 4) + 16];
                } else {
                    bytes2[i] = (byte) 0x00;
                }
            }
        }
        Log.i("yong", "byte2 to_str: " + new String(bytes2, StandardCharsets.UTF_8));

        /*3초뒤에 작업을 진행한다*/
        /*와이파이 이름 정보2 전송*/
        mTask = new TimerTask() {
            @Override
            public void run() {
                String s2 = null;
                try {
                    s2 = new String(bytes2, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d("myLog", "두번째 패킷 값 : " +  s2);
                Log.d("myLog", "제발요 : " + bytes2.length);
                fff3_Characteristic.setValue(bytes2);
                writeCharacteristic(fff3_Characteristic, logMsg);
                Log.d("myLog", logMsg + " 두 번째 20 바이트 전송");
            }
        };
        Log.d("myLog", "자리수6 : " + strBytes.length);

        mTimer = new Timer();
        mTimer.schedule(mTask, delay + 3000);
    }

    /**
     * 인트 값을 바이트값으로 변환 시킨다.
     *
     * @param x
     * @param n
     * @return
     */
    public static byte intToBytes(int x, int n) {
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++, x >>>= 8)
            bytes[i] = (byte) (x & 0xFF);
        return bytes[0];
    }


    public static String byteArrayToHexString(byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {

            sb.append(String.format("%02X", b & 0xff));
        }

        return sb.toString();
    }


    /**
     * 정해진 블루투스 특성 주소에 데이터를 보낸다.
     *
     * @param characteristic 특정 통신 함수
     * @param logMsg         스트링 형식의 현재 상황 메시지
     */
    public void writeCharacteristic(final BluetoothGattCharacteristic characteristic, final String logMsg) {
        Log.d("BJY","BluetoothLeConnect writeCharacteristic !!! logMsg : "+logMsg);
        Log.d("BJY","writeCharacteristic ! uuid : "+characteristic.getUuid());
        if(characteristic != null){
            byte[] test = characteristic.getValue();
            if(test[0] == 0x0C){
                Log.d("BJY "," INTERVAL WRTIE !!! ");
            }
        }
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("BJY", "BluetoothAdapter not initialized");
            return;
        }

        final boolean[] isSendSuccess = {false};

        Handler m = new Handler(Looper.getMainLooper());
        m.postDelayed(new Runnable() {
            @Override
            public void run() {
                //characteristic 값과 mBluetoothGatt 값이 널이 아닐 때만 전송 한다.
                if(characteristic != null && mBluetoothGatt != null) {
                    if(characteristic.getProperties() !=0 && BluetoothGattCharacteristic.PROPERTY_WRITE != 0){
                        Log.d("BJY","BluetoothLeConnect writeCharacteristic Type = DEFAULT");
                        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    }else{
                        Log.d("BJY","BluetoothLeConnect writeCharacteristic Type = WRITE_TYPE_NO_RESPONSE");
                        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    }
                    isSendSuccess[0] = mBluetoothGatt.writeCharacteristic(characteristic);
                    for (int i = 0; i < 6; i++) {
                        if (isSendSuccess[0] == false) {

                            writeCharacteristic(characteristic,logMsg);
//                            Log.d("BJY", logMsg + ": 재접속 상태 : " + isSendSuccess[0]);

                            /*
                             * write 시도에 실패하면 400 밀리 세컨드를 쉬고 다시 write
                             * 시도를 한다.
                             */

                            try {
                                Thread.sleep(400l);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }


                    /*
                     * 한번이라도 isSendSuccess[0] 결과가
                     * false 가 나오면 isSendSuccess2 상태를
                     * false 로 유지시킨다.
                     */
                    if (isSendSuccess[0] == false) {
                        isSendSuccess2 = false;
                    }
                    /*현재 진행 상황을 핸들러로 보낸다.*/
                    Message msg = handler2.obtainMessage();
                    msg.arg1 = 4;
                    msg.obj = (Object) logMsg;
                    handler2.sendMessage(msg);
                    Log.d("BJY", logMsg + " " + Boolean.toString(isSendSuccess[0]));
                    if(isSendSuccess[0] == true ){
                        if(logMsg != null && logMsg.contains("Pwd")){
                            Log.d("BJY","BluetootheLeConnect PWD Write SuccessWrite !!!");
                            mListener.onReadSuccess(MPWD, null);
                        }

                    }
                }

        }
    },200);
//        new Thread(new Runnable() {
//            @Override
//            public void run()
//        }).start();
    }

    /**
     * mBluetoothGatt 콜백 함수 정보를 셋팅한다.
     * callback method 처럼 characteristic이 변화 될 때 onCharacteristicChanged 을 작동하게 하려면
     * 위와 같은 설정을 해주어야 한다.
     *
     * @param characteristic 특성
     * @param enable         활성화 여부
     * @return
     */

    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        Log.w("BJY", "  setCharacteristicNotification !!! ");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("BJY", "BluetoothAdapter not initialized");
            return false;
        }

        // UUID_INIT_NOTI_RECV 수신 설정
        // callback method 등록하는 것 처럼
        // 이 부분이 반드시 있어야 함

        mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR));
        //descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[] { 0x00, 0x00 });
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        return mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * 배터리를 값 받는다.
     *
     * @param characteristic 배터리 특성 콜백 함수
     * @param mac            배터리 보낸 장비 맥주소
     */
    private void getBattery(BluetoothGattCharacteristic characteristic, final String mac) {
        final byte[] data = characteristic.getValue();

        if (data.length > 3) {
            int a = 0;
            a = 2;

        }
        //배터리 이벤트 발생 (동시에 호출하면 호출이 처리되지 않아서 200 딜레이 필요)
        if ((int) data[0] <= LOW_BATTERY) {

            final int battery = (int) data[0];
            try {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        lowBatteryCallback(mac, battery);
                    }
                }, 200);
            } catch (Exception e) {
            }
        }

        mListener.onBatteryValue((int) data[0], mac, sDeviceName);
    }

    /**
     * Main UI 전용 핸들러
     */
    @SuppressLint("HandlerLeak")
    private final Handler handler2 = new Handler()

    {

        public void handleMessage(Message msg)

        {
            /*
             * 1 : 스캐너 정보 읽기 핸들러
             * 2 : 블루투스 연결 됨 핸들러
             * 3 : 블루투스 연결 끊김 핸들러
             * 4 : 전송 진행 상황 핸들러
             * 5 : 연결 끊기 명령 핸들러
             */
            switch ((int) msg.arg1) {
                case 1:
                    /*읽은 바이트 값을 스트링으로 변환해서 확인한다.*/
                    String file_string = "";

                    try {
                        file_string += new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    Log.d("myLog", "myString" + file_string);
                    sendBrodCastSpcannerInfoData(file_string);
                    break;
                case 2:
                    deviceStateCallback(mBluetoothDeviceAddress, STATE_CONNECTED);
                    break;
                case 3:
                    deviceStateCallback(mBluetoothDeviceAddress, STATE_DISCONNECTED);
                    break;
                case 4:
                    inputStateCallBack((String) msg.obj);
                    break;
                case 5:

                    /*
                     * 마지막 단계인 연결해제가 상황이 되면
                     * isSendSuccess2 를 비교해서 성공과 실패 메시지를
                     * 브로드 캐스트 메시지로 보낸다.
                     */

                    if(isSendSuccess2){
                        Intent send_gs = new Intent(CommonInfo.MSG_INPUT_PROGRESS_TITLE_STR);
                        send_gs.putExtra(CommonInfo.MSG_INPUT_PROGRESS_DATA_STR,
                                mContext.getString(R.string.input_progress_success_input_msg));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(send_gs);
                        /*
                         * 전송에 성공하면 스캐너와의 연결을 끊는다.
                         */
                        cmdDisconnectCallBack();
                    }else{
                        Intent send_gs = new Intent(CommonInfo.MSG_INPUT_PROGRESS_TITLE_STR);
                        send_gs.putExtra(CommonInfo.MSG_INPUT_PROGRESS_DATA_STR,
                                mContext.getString(R.string.input_progress_fail_input_msg));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(send_gs);
                    }

            }

        }

    };

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    /**
     * 시간 딜레를 준 시간 만큼 반복 된다.
     *
     * @param rssi 수신기의 rssi 값
     */
    public void rssiCallback(int rssi) {

        if (CommonUtil.nvl(mBluetoothDeviceAddress).equals("")) {
            return;
        }

        mListener.onRssiValue(rssi, nIndex, mBluetoothDeviceAddress, sDeviceName);
    }

    /**
     * onDeviceState 를 브로드 캐스트 콜백을 날린다.
     *
     * @param mac  장비의 맥주소
     * @param code 상태 코드 네임
     */
    public void deviceStateCallback(String mac, int code) {
        mListener.onDeviceState(code, mac, sDeviceName);

    }

    public void lowBatteryCallback(String mac, int battery) {
        mListener.onBatteryEvent(battery, mac, sDeviceName);
    }

    /**
     * 입력 진행 상황 데이터를
     * 브로드 캐스트로 전송하는 함수
     * @param state
     */
    public void inputStateCallBack(String state) {

        Intent send_gs = new Intent(CommonInfo.MSG_INPUT_PROGRESS_TITLE_STR);
        send_gs.putExtra(CommonInfo.MSG_INPUT_PROGRESS_DATA_STR,
                state);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(send_gs);


    }

    /**
     * 비콘을 클릭 했을 때 콜백 실행 함수
     *
     * @param mac 클릭한 비콘의 mac 주소
     */
    public void clickCallback(String mac) {
        mListener.onClickEvent(mac, sDeviceName);
    }

    /**
     * 연결 끊기 명령 콜백 함수
     */
    public void cmdDisconnectCallBack() {
        mListener.onCmdDisConnect();
    }

    /**
     * OXFF 신호를 통해 받은
     * 스캐너 정보를 브로드캐스트로 방송한다.
     */
    public void sendBrodCastSpcannerInfoData(String data) {

        String filterData = CommonUtil.extractWord(data);
        if(filterData.equals("")){

        }else {

            Intent send_gs = new Intent(CommonInfo.MSG_SCANNER_INFO_TITLE_STR);
            send_gs.putExtra(CommonInfo.MSG_SCANNER_INFO_DATA_STR,
                    filterData);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(send_gs);
            Log.d("myLog","스캐너 정보 리시버 보냄");

        }


        //mListener.onReceiveData(data);
    }

    private byte[] chagneChar(char[] chars){
        CharBuffer chb = CharBuffer.wrap(chars);
        ByteBuffer byf = Charset.forName("UTF-8").encode(chb);
        byte[] bytes = Arrays.copyOfRange(byf.array(), byf.position(), byf.limit());
        Arrays.fill(byf.array(), (byte)0);
        return bytes;
    }

}
