package com.choistec.cms.scannerReg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.choistec.cms.scannerReg.bluetooth.BluetoothCallBack;
import com.choistec.cms.scannerReg.bluetooth.BluetoothLeConnect;
import com.choistec.cms.scannerReg.unit.MonitorItem;
import com.choistec.cms.scannerReg.unit.SwitchButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;


public class FragmentCmsMonitor extends Fragment implements BluetoothCallBack {
    private ImageView mQrImg;
    private ImageView mReadBtn;
    private IntentIntegrator mQrScan;
    private Activity mCmsActivity;
    private Button mOkBtn;
    private EditText mQrEdit, mIntervalEdit;
    private NumberPicker mLowTmpEdit, mHighTmpEdit;
    private RadioButton mTmpC,mTmpF;
    private MonitorItem mItem;
    private SwitchButton mAlarmChk;
    private RecyclerView mSMSView;
    private BluetoothLeConnect mConn;
    private LinearLayout mAlarmLayout;
    private byte[] mMacByteArray;
    private byte[] mTempUnitByteArray;
    private byte[] mIntervalByteArray;
    private byte[] mTempByteArray;
    boolean mWriteAll = false;
    boolean mReadAll = false;
    private HashMap<Integer, BluetoothGattCharacteristic> mReadCharacteristicMap = new HashMap<>();
    private HashMap<Integer, BluetoothGattCharacteristic> mCloneMap;
    private BluetoothGattCharacteristic mMacChar;
    private BluetoothGattCharacteristic mUnintChar;
    private BluetoothGattCharacteristic mTempChar;
    private BluetoothGattCharacteristic mIntervalChar;
    private byte[] mMacByte;
    private byte[] mUnitByte;
    private byte[] mTempByte;
    private byte[] mIntervalByte;

    private android.app.ProgressDialog mDialog;
    private Context mContext;
    String mMsg = "";
    private List<BluetoothGattService> mBleServiceList;

    public FragmentCmsMonitor() {

    }

    @SuppressLint("ValidFragment")
    public FragmentCmsMonitor(Activity activity/*, MonitorItem item*/, BluetoothLeConnect con, Context _Context) {
        mCmsActivity = activity;
        mContext = _Context;
//        mItem = item;
        mConn = con;
    }

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_monitor_stting, container, false);
        mQrImg = (ImageView) rootView.findViewById(R.id.qr_btn);
        mOkBtn = (Button) rootView.findViewById(R.id.ok_btn);
        mQrEdit = (EditText) rootView.findViewById(R.id.qr_edit_txt);
        mBleServiceList = mConn.getSupportedGattServices();
        mTmpC = (RadioButton) rootView.findViewById(R.id.btn_c);
        mTmpF = (RadioButton) rootView.findViewById(R.id.btn_f);
        mConn.setBluetoothCallBackListener(this);
        mReadBtn = (ImageView) rootView.findViewById(R.id.back_btn);
        mAlarmChk = (SwitchButton) rootView.findViewById(R.id.sw_hi_snd);
        mQrEdit = (EditText) rootView.findViewById(R.id.qr_edit_txt);
        mLowTmpEdit = (NumberPicker) rootView.findViewById(R.id.low_tmp);
        mHighTmpEdit = (NumberPicker) rootView.findViewById(R.id.high_tmp);
        mLowTmpEdit.setMinValue(0);
        mLowTmpEdit.setMaxValue(18);
        mLowTmpEdit.setDisplayedValues(setMinValue());
        mHighTmpEdit.setMaxValue(50);
        mAlarmLayout = (LinearLayout) rootView.findViewById(R.id.alarm_layout);
        mIntervalEdit = (EditText) rootView.findViewById(R.id.interval_edt);
        mDialog = new android.app.ProgressDialog(mContext);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.show();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        setClickEvent();
        //onCreateView 이후 호출
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("text", 2);
        //사라질때 현재의 상태를 저장하고 나중에 Fragment가 다시 돌아오면 내용을 사용할 수 있게해주는 메소드드
    }

    public void onStart() {
        super.onStart();
        //모든 UI가 만들어지고 호출된다.
    }


    private void initView() {
    }

    private void setClickEvent() {
        mQrImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQrScan = new IntentIntegrator(mCmsActivity);
                mQrScan.setOrientationLocked(false);
                mQrScan.setPrompt("QR코드를 사각형 안에 비춰주세요.");
                mQrScan.forFragment(FragmentCmsMonitor.this).initiateScan();

                mQrScan.initiateScan();
            }
        });
        mReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadSuccess(BluetoothLeConnect.MPWD, null);
            }
        });
        mAlarmChk.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked == false) {
                    mAlarmLayout.setVisibility(View.INVISIBLE);
                } else {
                    mAlarmLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String testMac = mQrEdit.getText().toString();

                int[] intMac = new int[6];
                byte[] byteMac = new byte[20];
                if (testMac != null) {
                    try {
                        String[] test = testMac.split(":");
                        for (int i = 0; i < test.length; i++) {
                            intMac[i] = Integer.parseInt(test[i], 16);
                            Log.d("BJYMAC", " 순서 : " + intMac[i]);
                        }
                        byteMac[5] = (byte) (intMac[0]);
                        byteMac[4] = (byte) (intMac[1]);
                        byteMac[3] = (byte) (intMac[2]);
                        byteMac[2] = (byte) (intMac[3]);
                        byteMac[1] = (byte) (intMac[4]);
                        byteMac[0] = (byte) (intMac[5]);
                        mMacByteArray = byteMac;
                    } catch (Exception e) {

                    }
                    for (int i = 0; i < byteMac.length; i++) {
                        Log.d("BJYMAC", "뒤집어 : " + byteMac[i]);
                    }
                }

                byte[] tmpUnitByteArr = new byte[20];
                boolean tempUnitCF = mTmpC.isChecked();
                if (tempUnitCF == false) {
                    tmpUnitByteArr[0] = 0x01;
                } else {
                    tmpUnitByteArr[0] = 0x00;
                }
                tmpUnitByteArr[1] = 0x00;
                boolean alarm = mAlarmChk.isChecked();
                if (alarm == false) {
                    tmpUnitByteArr[2] = 0x00;
                } else {
                    tmpUnitByteArr[2] = 0x01;
                }
                tmpUnitByteArr[3] = 0x01;
                //TODO 이게 임시로 넣어논 값이여서 비교할때 맞추기 위해서 일단은 값을 바로 넣어준 것임.
                mTempUnitByteArray = tmpUnitByteArr;

                if (alarm == false) {
                    return;
                } else {
                    Float low = Float.parseFloat(Integer.toString(mLowTmpEdit.getValue() - 15));
                    Log.d("BJY", " Number Spicker Low Value : " + low);
                    low *= 100;
                    short HLow = (short) Math.round(low);
                    short LLow = (short) Math.round(low);


                    byte byteHLow = (byte) ((HLow >> 8) & 0xFF);
                    byte byteLLow = (byte) (LLow & 0xFF);

                    Float high = Float.parseFloat(Integer.toString(mHighTmpEdit.getValue()));
                    Log.d("BJY,", "Number Spicker High Value : " + high);
                    high *= 100;
                    short HHigh = (short) Math.round(high);
                    short LHigh = (short) Math.round(high);

                    byte byteHHigh = (byte) ((HHigh >> 8) & 0xFF);
                    byte byteLHigh = (byte) (LHigh & 0xFF);
                    byte[] tmpArray = new byte[20];
                    tmpArray[0] = byteHHigh;
                    tmpArray[1] = byteLHigh;
                    tmpArray[2] = byteHLow;
                    tmpArray[3] = byteLLow;
                    mTempByteArray = tmpArray;

                    String strInterval = mIntervalEdit.getText().toString();
                    strInterval = strInterval.replaceAll("[^0-9]", "");
                    int interval = Integer.parseInt(strInterval);

                    byte[] intervalByte = new byte[20];
                    intervalByte[0] = (byte) ((interval << 24) & 0xFF);
                    intervalByte[1] = (byte) ((interval << 16) & 0xFF);
                    intervalByte[2] = (byte) ((interval << 8) & 0xFF);
                    intervalByte[3] = (byte) (interval & 0xFF);
                    mIntervalByteArray = intervalByte;
                    if (mMacByteArray == null) {
                        testWrite(BluetoothLeConnect.MUNIT, mTempUnitByteArray);
                    } else {
                        mDialog.show();
                        testWrite(BluetoothLeConnect.MMAC, mMacByteArray);
                    }

                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("BJY", "Cancel !!!");
            } else {
                Log.d("BJY", "QrScan Value : " + result.getContents());
                mQrEdit.setText(result.getContents().toString());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }


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
        Log.d("BJY", " data : " + data);
    }

    @Override
    public void onInputState(String data) {

    }

    @Override
    public void onCmdDisConnect() {
        Log.d("BJY", "FragmentMonitor Ble disconnect !!!");
    }

    @Override
    public void onReadSuccess(int what, BluetoothGattCharacteristic characteristic) {
        Handler m = new Handler(Looper.getMainLooper());
        m.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (what) {
                    case BluetoothLeConnect.MPWD:
                        Log.d("BJY", " onReadSuccess MMAC!!!");
                        testRead(BluetoothLeConnect.MMAC);
                        break;
                    case BluetoothLeConnect.MMAC:
                        Log.d("BJY", " onReadSuccess MTEMP!!!");
                        mMacByte = characteristic.getValue().clone();
                        testRead(BluetoothLeConnect.MTEMP);
                        break;
                    case BluetoothLeConnect.MTEMP:
                        Log.d("BJY", " onReadSuccess MINTERVAL!!!");
                        mTempByte = characteristic.getValue().clone();
                        testRead(BluetoothLeConnect.MINTERVAL);
                        break;
                    case BluetoothLeConnect.MINTERVAL:
                        Log.d("BJY", " onReadSuccess MUNIT!!!");
                        mIntervalByte = characteristic.getValue().clone();
                        testRead(BluetoothLeConnect.MUNIT);
                        break;
                    case BluetoothLeConnect.MUNIT:
                        mUnitByte = characteristic.getValue().clone();
                        Log.d("BJY", " Read All !!!! ");
                        if(mDialog != null && mDialog.isShowing() == true)
                        mDialog.dismiss();
                        setReadValue();
                        if (mWriteAll) {
                            mCloneMap = (HashMap<Integer, BluetoothGattCharacteristic>) mReadCharacteristicMap.clone();
                            mWriteReadHandler.sendEmptyMessage(1);
                        } else {
                            mWriteReadHandler.sendEmptyMessage(2);
                        }
                        break;

                }
            }
        }, 50);
    }

    @Override
    public void onWriteSuccess(int what) {
        Log.d("BJY", " Fragment onWrite Scuccess : " + what);
        switch (what) {
            case 0:
                testWrite(BluetoothLeConnect.MUNIT, mTempUnitByteArray);
                break;
            case 1:
                testWrite(BluetoothLeConnect.MINTERVAL, mIntervalByteArray);
                break;
            case 2:
                testWrite(BluetoothLeConnect.MTEMP, mTempByteArray);
                break;
            case 3:
                Log.d("BJY", " Wrtie All !!!!");
                if(mDialog != null && mDialog.isShowing() == true)
                mDialog.dismiss();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("BJY", "CMS Monitor Fragment is Destroy!!!");
        mConn.disconnect();
        mConn.deviceStateCallback(mConn.mBluetoothGatt.getDevice().getAddress(), BluetoothLeConnect.STATE_DISCONNECTED);
        mConn.mBluetoothGatt.disconnect();
        mConn.stop();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("BJY", "CMS Monitor Fragment is onPause !!!");
    }

    private void testWrite(int what, byte[] data) {
        byte cmd = 0x00;
        switch (what) {
            case BluetoothLeConnect.MMAC:
                cmd = BluetoothLeConnect.MMAC_WRITE_CMD;
                mMsg = "mac";
                break;
            case BluetoothLeConnect.MUNIT:
                cmd = BluetoothLeConnect.MUNIT_WRITE_CMD;
                mMsg = "unit";
                break;
            case BluetoothLeConnect.MINTERVAL:
                cmd = BluetoothLeConnect.MINTERVAL_WRITE_CMD;
                mMsg = "interval";
                break;
            case BluetoothLeConnect.MTEMP:
                cmd = BluetoothLeConnect.MTEMP_WRITE_CMD;
                mMsg = "temp";
                break;
        }
        byte[] bt1 = new byte[20];
        bt1[0] = cmd;
        for (int i = 0; i < data.length - 1; i++) {
            bt1[i + 1] = data[i];
        }
        List<BluetoothGattService> service;
        if (mConn == null)
            return;
        service = mConn.mBluetoothGatt.getServices();
        service.forEach(new Consumer<BluetoothGattService>() {
            @Override
            public void accept(BluetoothGattService bluetoothGattService) {
                if (bluetoothGattService.getUuid().toString().contains("0000fff0")) {
                    bluetoothGattService.getCharacteristics().forEach(new Consumer<BluetoothGattCharacteristic>() {
                        @Override
                        public void accept(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                            if (bluetoothGattCharacteristic.getUuid().toString().contains("0000fff3")) {
                                bluetoothGattCharacteristic.setValue(bt1);
                                mConn.writeCharacteristic(bluetoothGattCharacteristic, mMsg);
                            }
                        }
                    });
                }
            }
        });
    }

    private void testRead(int what) {
        Log.d("BJY", "CMS Monitor Fragment write test!!!  What : " + what);
        byte cmd = 0x00;
        switch (what) {
            case BluetoothLeConnect.MTEMP:
                cmd = BluetoothLeConnect.MTEMP_READ_CMD;
                break;
            case BluetoothLeConnect.MINTERVAL:
                cmd = BluetoothLeConnect.MINTERVAL_READ_CMD;
                break;
            case BluetoothLeConnect.MUNIT:
                cmd = BluetoothLeConnect.MUNIT_READ_CMD;
                break;
            case BluetoothLeConnect.MMAC:
                cmd = BluetoothLeConnect.MMAC_READ_CMD;
                break;


        }
        byte[] bt1 = new byte[1];
        bt1[0] = cmd;
        List<BluetoothGattService> service;
        if (mConn == null)
            return;
        service = mConn.mBluetoothGatt.getServices();
        service.forEach(new Consumer<BluetoothGattService>() {
            @Override
            public void accept(BluetoothGattService bluetoothGattService) {
                if (bluetoothGattService.getUuid().toString().contains("0000fff0")) {
                    bluetoothGattService.getCharacteristics().forEach(new Consumer<BluetoothGattCharacteristic>() {
                        @Override
                        public void accept(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                            if (bluetoothGattCharacteristic.getUuid().toString().contains("0000fff1")) {
                                bluetoothGattCharacteristic.setValue(bt1);
                                mConn.writeCharacteristic(bluetoothGattCharacteristic, "read_setting  : " + what);
                            }
                        }
                    });
                }
            }
        });
    }


    private boolean chkEmptyByteArray(byte[] arr) {
        boolean check = false;
        if (arr == null)
            return check;
        if (arr.length < 1)
            return check;

        return true;
    }

    public Handler mWriteReadHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (chkEmptyByteArray(mTempUnitByteArray) && chkEmptyByteArray(mIntervalByteArray) && chkEmptyByteArray(mTempByteArray)) {
                        onReadSuccess(BluetoothLeConnect.MPWD, null);
                    }
                    mWriteAll = true;
                    break;
                case 1:
                    if (valueChk(mCloneMap)) {
                        Log.d("BJY", "Write Same Read !!! OK Success !!!");
                        mConn.disconnect();
                        mConn.deviceStateCallback(mConn.mBluetoothGatt.getDevice().getAddress(), BluetoothLeConnect.STATE_DISCONNECTED);
                        mConn.mBluetoothGatt.disconnect();
                        mConn.stop();
                    } else {
                        Log.d("BJY", "Read Write Same Fail !!!");
                    }
                    mReadCharacteristicMap.clear();
                    break;
                case 2:
                    break;
            }
        }
    };

    private boolean valueChk(HashMap<Integer, BluetoothGattCharacteristic> map) {
        boolean chk = false;

//        BluetoothGattCharacteristic characteristic = map.get(BluetoothLeConnect.MMAC);
//        BluetoothGattCharacteristic characteristic2 = map.get(BluetoothLeConnect.MINTERVAL);
//        BluetoothGattCharacteristic characteristic3 = map.get(BluetoothLeConnect.MTEMP);
//        BluetoothGattCharacteristic characteristic4 = map.get(BluetoothLeConnect.MUNIT);
        BluetoothGattCharacteristic characteristic = mMacChar;
        BluetoothGattCharacteristic characteristic2 = mIntervalChar;
        BluetoothGattCharacteristic characteristic3 = mTempChar;
        BluetoothGattCharacteristic characteristic4 = mUnintChar;
        if (mMacByteArray != null) {
            chk = arrayChk(mMacByteArray, characteristic);
        }
        chk = arrayChk(mIntervalByteArray, characteristic2);
        chk = arrayChk(mTempByteArray, characteristic3);
        chk = arrayChk(mTempUnitByteArray, characteristic4);

        return chk;
    }

    private boolean arrayChk(byte[] writeArr, BluetoothGattCharacteristic characteristic) {
        byte[] readArr = characteristic.getValue();
        for (int i = 0; i < writeArr.length; i++) {
            if (writeArr[i] != readArr[i])
                return false;
        }
        return true;
    }

    private String[] setMinValue() {
        String[] strMin = new String[19];
        for (int i = 0; i < strMin.length; i++) {
            strMin[i] = Integer.toString(i - 15);
        }
        return strMin;
    }

    private boolean setReadValue() {
        boolean read = false;
        byte[] mac = mMacByte;
        byte[] unit = mUnitByte;
        byte[] temp = mTempByte;
        byte[] interval = mIntervalByte;
//        int decimal = Integer.toHexString()
        String str = getMacAddr(mac);
        mQrEdit.setText(str);
        checkUnit(unit);
        int a = getInterval(interval);
        mIntervalEdit.setText(Integer.toString(a));
        setTempValue(temp);
        Log.d("BJY", "Str value : " + str);

        return read;
    }

    private int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    private String getMacAddr(byte[] macByte) {
        byte mac[] = macByte;
        String str = "";
        int intMac[] = new int[6];
        try {
            for (int i = mac.length - 1; i >= 1; i--) {
                if (mac[i] == 0)
                    continue;
                intMac[i - 1] = unsignedByteToInt(mac[i]);
            }
            String[] strMac = new String[intMac.length];
            int count = 5;
            for (int i = 0; i < intMac.length; i++) {
                strMac[count] = Integer.toHexString(intMac[i]);
                count--;
            }
            for (int i = 0; i < strMac.length; i++) {
                str = str + strMac[i].toUpperCase();
                if (i < strMac.length - 1)
                    str += ":";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("BJY", "FragmentCms Monitor setReadValue Method Exception !!!");
        }
        return str;
    }
    private void checkUnit(byte[] unitByte){
        byte[] unit = unitByte;
        if(unit[1] == 0x00){
            mTmpC.setChecked(true);
        }else{
            mTmpF.setChecked(true);
        }
        if(unit[3] == 0x00){
            mAlarmChk.setChecked(false);
        }else{
            mAlarmChk.setChecked(true);
        }
    }
    private int getInterval(byte[] intervalByte){

        int a = (unsignedByteToInt(intervalByte[1]) >> 24)+(unsignedByteToInt(intervalByte[2]) >> 16)+
                (unsignedByteToInt(intervalByte[3]) >> 8)+(unsignedByteToInt(intervalByte[4]));
    Log.d("BJY","interaval value : "+a);
    return a;
    }
    private void setTempValue(byte[] tempByte){
        int HLow1 = unsignedToSigned((unsignedByteToInt(tempByte[1]) << 8) + unsignedByteToInt(tempByte[2]), 8);
        int HLow2 = unsignedToSigned((unsignedByteToInt(tempByte[3]) << 8) + unsignedByteToInt(tempByte[4]), 8);

        short h = (short)HLow1;
        short l = (short)HLow2;
        float lowTemp = (float)l;
        float highTemp = (float)h;
        Log.d("BJY"," temp Short Value : h: "+h+" // AND low : "+l);
        String strHighTmp = Float.toString(highTemp);
        String strLowTmp = Float.toString(lowTemp);
         int low = (int)l/100;
        int high = (int)h/100;
//        mLowTmpEdit.setValue(low);
        mHighTmpEdit.setValue(high);
        mLowTmpEdit.setVerticalScrollbarPosition(low+15);
    }
    private int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }
}

