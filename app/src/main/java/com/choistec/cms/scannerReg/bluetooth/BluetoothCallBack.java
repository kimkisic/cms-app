package com.choistec.cms.scannerReg.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Chois on 2017-09-01.
 */

public interface BluetoothCallBack {
    /*테스트를 위해 주석처리*/
    //void onRssiValue(int rssi, String macAddress, String deviceName);
    void onRssiValue(int rssi, int index ,String macAddress, String deviceName);
    void onBatteryValue(int battery, String macAddress, String deviceName);
    void onDeviceState(int code, String macAddress, String deviceName);
    void onBatteryEvent(int battery, String macAddress, String deviceName);
    void onClickEvent(String macAddress, String deviceName);
    void onReceiveData(String data);
    void onInputState(String data);
    //연결 끊는 명령 콜백
    void onCmdDisConnect();
    void onReadSuccess(int what, BluetoothGattCharacteristic characteristic);
    void onWriteSuccess(int what);
}
