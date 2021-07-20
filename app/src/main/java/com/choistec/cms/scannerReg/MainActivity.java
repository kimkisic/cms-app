package com.choistec.cms.scannerReg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.choistec.cms.scannerReg.bluetooth.BluetoothCallBack;
import com.choistec.cms.scannerReg.bluetooth.BluetoothLeConnect;
import com.choistec.cms.scannerReg.dbbase.ChoisDBHelper;
import com.choistec.cms.scannerReg.util.CommonInfo;
import com.choistec.cms.scannerReg.util.CommonUtil;

import java.sql.Date;
import java.text.SimpleDateFormat;

import static com.choistec.cms.scannerReg.dbbase.ChoisDBHelper.sDB;

public class MainActivity extends Activity implements BluetoothCallBack, View.OnClickListener {
    private final static String TAG = "myLog";

    /*보안레벨 설정*/
    private static int securityLevel = 1;

    /*스캐너 정보 변수*/
    private com.choistec.cms.scannerReg.DeviceItem mScannerInfo = new com.choistec.cms.scannerReg.DeviceItem();
    /*디바이스 요청 Activity Result 함수 생성*/
    private static final int REQUEST_DEVICE = 2;

    /*블루투스 커넥트 객체 변수 ArrayList 선언*/
    private BluetoothLeConnect BluetoothLeConnect;
    private DrawerLayout mDrawerLayout;
    private View mDrawerView;
    private ImageView mSettingImg;
    private ImageView mCmsBtn;
    private Context mContext;
    /*MainActivity Context 선언*/
    /*전 클래스에서 사용하기 위해 public static*/
    /*으로 선언*/
    public static Context context;

    ProgressDialog customProgressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /*뷰 객체 선언*/
        declearView();
        mContext = this;
        setDataBase();
        /*클릭 이벤트 선언*/
        setOnClickEvent();

        /*저장된 스캐너 정보를 담는다.*/
//        getDeviceInfo();

//        실행 되자 마자 스캐너 액티비티를 실행시킨다.
//        Intent intent = new Intent(MainActivity.this, NewScanActivity.class);
//        intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_SINGLE_TOP);
//        startActivityForResult(intent, REQUEST_DEVICE);
        overridePendingTransition(0, 0);
        setWifiSsid();
    }

    /**
     * 와이파이 이름을 불러와서
     * editTextWifiName 에 setText 한다.
     *
     * @return
     */
    public String setWifiSsid() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ssid = info.getSSID();
        /*
         * ssid 처음과 끝에 " 가 생기므로
         * replace("\"", "") 함수를 사용해서
         * 제거한다.
         */
        if (!ssid.equals("<unknown ssid>")) {
            editTextWifiName.setText(ssid.replace("\"", ""));
        }
        return ssid;
    }


    private Button btnScannerSerach; //스캐너 추가 버튼
    private Button btnSendScannerData; //모든 정보 스캐너에 최종 입력 버튼
    private Button btnReadScannerInfo; //스캐너 정보 확인 버튼
    private Button btnMqttSetup; // MQTT설정 버튼
    /*디버깅용 텍스트뷰 선언*/
    TextView textViewConnectState;

    TextView tvMqtt;

    /*와이파이 이름 입력 에디트텍스트*/
    EditText editTextWifiName;
    /*와이파이 비밀번호 입력 에디트텍스트*/
    EditText editTextWifiPW;
    /*MQTT IP 입력 에디트텍스트*/
    EditText editTextMqttIP;

    /*와이파이 보안 타입 플래그*/
//    String secType;

    /*와이파이 보안 타입 라디오그룹*/
    private RadioGroup securityType;

    /*와이파이 보안 타입 스피너*/
    Spinner spinnerSec;
    ArrayAdapter secAdapter;
    String[] spinnerItems;

    /*와이파이 엔터프라이즈 체크박스*/
//    CheckBox checkBoxEnt;

    //프로그레스바
    ProgressBar progressBar;

    /**
     * 뷰 객체를 선언한다.
     */
    public void declearView() {

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        /*비콘 추가 버튼*/
        btnScannerSerach = (Button) findViewById(R.id.btnScannerSerach);

        /*스캐너 정보 입력*/
        btnSendScannerData = (Button) findViewById(R.id.btnSendScannerData);
        btnSendScannerData.setVisibility(View.INVISIBLE);

        /*스캐너 정보 확인*/
        btnReadScannerInfo = (Button) findViewById(R.id.btnReadScannerInfo);
        btnReadScannerInfo.setVisibility(View.INVISIBLE);

        /*MQTT 설정 버튼*/
        btnMqttSetup = (Button) findViewById(R.id.btn_mqtt_setup);

        tvMqtt = (TextView) findViewById(R.id.tv_mqtt);

        /*텍스트뷰*/
        textViewConnectState = (TextView) findViewById(R.id.textViewConnectState);

        /*에디트 텍스트*/

        /*와이파이 이름 입력 에디트텍스트*/
        editTextWifiName = (EditText) findViewById(R.id.editTextWifiName);
        /*와이파이 비밀번호 입력 에디트텍스트*/
        editTextWifiPW = (EditText) findViewById(R.id.editTextWifiPW);
        /*MQTT IP 입력 에디트텍스트*/
        editTextMqttIP = (EditText) findViewById(R.id.editTextMqttIP);
        editTextMqttIP.setText("cms2.choistec.com");

        /*와이파이 보안 라디오그룹*/
        securityType = (RadioGroup) findViewById(R.id.security_type);

        /*와이파이 보안 스피너*/
//        spinnerSec = (Spinner) findViewById(R.id.spinnerSec);

        /*엔터프라이즈 체크박스*/
//        checkBoxEnt = (CheckBox) findViewById(R.id.checkBoxEnt);

        //현재 앱 버전을 찾아 텍스트뷰에 넣어준다.
        TextView textViewVersion = (TextView) findViewById(R.id.textViewVersion);
        textViewVersion.setText("CMSG01 Ver "+ CommonInfo.VERSION);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerView = (View)findViewById(R.id.drawer);
        mSettingImg = (ImageView)findViewById(R.id.setting_view);
    }

    /**
     * 버튼 이벤트 리스너 선언 객체
     */
    private void setOnClickEvent() {

        securityType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_open) {
                    securityLevel = 0;
                    Toast.makeText(getApplicationContext(), "OPEN", Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.radio_wpa2) {
                    securityLevel = 1;
                    Toast.makeText(getApplicationContext(), "WPA/WPA2", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnMqttSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edttext = new EditText(MainActivity.this);
                edttext.setSingleLine();
                edttext.setText(editTextMqttIP.getText().toString());

                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("MQTT Server");
                dlg.setView(edttext);
                dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), edttext.getText().toString(), Toast.LENGTH_SHORT).show();
                        editTextMqttIP.setText(edttext.getText().toString());
                    }
                });
                dlg.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dlg.show();
//                editTextMqttIP.setVisibility(View.VISIBLE);
//                tvMqtt.setVisibility(View.VISIBLE);
            }
        });

        /*비콘 추가 버튼 리스너*/
        btnScannerSerach.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*
                * 블루투스가 과거의 이름으로 재연결 시도 중일 수도 있으니
                * 다시 검색할 때 재연결된 블루투스를 해지한다.
                */
                scanStop();
                Intent intent = new Intent(MainActivity.this, NewScanActivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, REQUEST_DEVICE);
                overridePendingTransition(0, 0);
            }
        });

        /*Wifi 정보을 입력하는 버튼*/
        btnSendScannerData.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            public void onClick(View v) {

                if (mScannerInfo.isConnected()) {
                    mScannerInfo.setWifiSsid(editTextWifiName.getText().toString());
                    mScannerInfo.setWifiPW(editTextWifiPW.getText().toString());
                    mScannerInfo.setMqttIp(editTextMqttIP.getText().toString());
                    /*Wifi 이름을 입력하는 함수*/
//                    BluetoothLeConnect.sendScannerData(mScannerInfo, secType, checkBoxEnt.isChecked());
                    BluetoothLeConnect.sendScannerData(mScannerInfo, securityLevel);
                    /*스캐너 정보를 저장한다.*/
                    setDeviceInfo();

                    /*
                     * 먼저 데이터를 전송한 다음에 진행 상황
                     * 액티비티를 띄워준다.
                     */
                    Intent intent=new Intent(MainActivity.this,InputProgressActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.gateway_is_not_connected
                            ,1).show();
                }
            }
        });

        /*스캐너 정보 확인 버튼 리스너*/
        btnReadScannerInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mScannerInfo.isConnected()) {
                    Intent intent = new Intent(MainActivity.this, ScannerInfoActivity.class);
                    startActivity(intent);
                    /*스캐너 정보 불러오는 함수를 실행 시킨다.*/
                    BluetoothLeConnect.readWifiData();
                }else {
                    Toast.makeText(getApplicationContext(),
                            R.string.gateway_is_not_connected
                            ,Toast.LENGTH_LONG).show();
                }
            }
        });
        mSettingImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerView);
            }
        });

        /*스피너, 체크박스 필요 변수 초기화*/
//        final String[] notEntItems = new String[]{"WPA/WPA2" , "OPEN"};
//        final String[] entItems = new String[]{"WPA/WPA2"};
//        final ArrayAdapter notEntAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, notEntItems);
//        final ArrayAdapter entAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, entItems);
//        spinnerItems = notEntItems;
//        secAdapter = notEntAdapter;
//        checkBoxEnt.setChecked(false);

        /*와이파이 엔터프라이즈 체크박스 리스너*/
//        checkBoxEnt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    spinnerItems = entItems;
//                    secAdapter = entAdapter;
//                } else {
//                    spinnerItems = notEntItems;
//                    secAdapter = notEntAdapter;
//                }
//                spinnerSec.setAdapter(secAdapter);
//                secType = spinnerItems[0];
//            }
//        });

        /*와이파이 보안 타입 스피너 리스너*/
//        spinnerSec.setAdapter(secAdapter);
//        spinnerSec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                secType = spinnerItems[position];
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                secType = spinnerItems[0];
//            }
//        });
    }

    /**
     * 비콘 Mac 주소를 스캔한다.
     */
    private void ScanStart(String name, String mac) {

        /*arrBluetoothLeConnect.get(i).mListener 값이 null 이면*/
        /*연결 시도를 하지 않는다.*/
        Log.i("yong","hello ----------------->");
//        Toast.makeText(MainActivity.this, "연결중 입니다.", Toast.LENGTH_SHORT).show();
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);
        customProgressDialog.show();
        if (BluetoothLeConnect == null) {
            /*블루투스LE를 초기화한다. 추가한다.*/
            BluetoothLeConnect = new BluetoothLeConnect();
            BluetoothLeConnect.setBluetoothCallBackListener(MainActivity.this);
            BluetoothLeConnect.initialize(this, 0, name, mac, 0,null);
        }

    }

    /**
     * 포커스 다시 잡힐 때 사용되는 함수
     */
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 종료 이벤트 함수
     */
    @Override
    protected void onDestroy() {

        /*스캐너 정보를 저장한다.*/
        setDeviceInfo();

        super.onDestroy();
    }

    /**
     * 액티비티 종료 캐치 메소드
     *
     * @param requestCode 요청코드
     * @param resultCode  응답 코드
     * @param data        데이터
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            if (resultCode == RESULT_OK) {
                /*Beacon 장비 이름*/
                String name = data.getStringExtra(getString(R.string.beacon_name));
                /*비콘 맥 주소*/
                String mac = data.getStringExtra(getString(R.string.beacon_mac));

                if (!CommonUtil.nvl(name).equals("") && !CommonUtil.nvl(mac).equals("")) {
                    /*블루투스 연결 함수*/
                    ScanStart(name, mac);
                }
            }
        }
    }

    /**
     * 저장된 스캐너 정보를 담는다.
     */
    public void getDeviceInfo() {

        SharedPreferences pref = getSharedPreferences("deviceInfo", 0);
        try {
            //스캐너 이름
            mScannerInfo.setScannerId(pref.getString(getString(R.string.scanner_id), ""));
            //스캐너 주소
            mScannerInfo.setScannerMac(pref.getString(getString(R.string.scanner_mac), ""));
            //와이파이 이름
            mScannerInfo.setWifiSsid(pref.getString(getString(R.string.wifi_ssid), ""));
            //와이파이 비밀번호
            mScannerInfo.setWifiPW(pref.getString(getString(R.string.wifi_pw), ""));
            //MQTT IP
            mScannerInfo.setMqttIp(pref.getString(getString(R.string.mqtt_ip), ""));
            /*
             * 현재 사용되지 않기로 결정해서 주석처리하였다.
             */
            //editTextWifiName.setText(mScannerInfo.getWifiSsid());
            //editTextWifiPW.setText(mScannerInfo.getWifiPW());
            //editTextMqttIP.setText(mScannerInfo.getMqttIp());

        } catch (Exception e) {
        }
    }
    /**
    * 백버튼 2회시 종료 시키는
    * 시간 변수
    */
    private long mBackPressedTime = 0;
    @Override
    public void onBackPressed() {
        Resources res = getResources();
        String msg = res.getString(R.string.msg_finish_message);

        if (mBackPressedTime == 0) {
            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
            mBackPressedTime = System.currentTimeMillis();

        } else {
            int seconds = (int) (System.currentTimeMillis() - mBackPressedTime);

            if (seconds > CommonInfo.FINSH_INTERVAL_TIME) {
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                mBackPressedTime = 0;
            } else {
                super.onBackPressed();
                finish();
            }

        }
    }

    /**
     * 에디트 텍스트에 적힌 정보를
     * Preferences DB 에 담는다.
     */
    public void setDeviceInfo() {
        SharedPreferences pref = getSharedPreferences("deviceInfo", 0);
        SharedPreferences.Editor edit = pref.edit();
        //스캐너 와이파이 이름
        edit.putString("wifi_ssid", editTextWifiName.getText().toString());
        //스캐너 와이파이 비밀번호
        edit.putString("wifi_pw", editTextWifiPW.getText().toString());
        //mqtt 아이피 주소
        edit.putString("mqtt_ip", editTextMqttIP.getText().toString());

        edit.commit();
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRssiValue(int rssi, int index, String macAddress, String deviceName) {

    }

    @Override
    public void onBatteryValue(int battery, String macAddress, String deviceName) {

    }

    @Override
    public void onDeviceState(int code, String macAddress, String deviceName) {

        /*
         * 1001 : 블루투스 연결 됨 핸들러
         * 1002 : 블루투스 연결 끊김 핸들러
         */

        switch (code) {
            case 1001:

                Message msg = handler.obtainMessage();
                msg.arg1 = 2;
                handler.sendMessage(msg);
                mScannerInfo.setConnected(true);
                customProgressDialog.dismiss();

                if (mScannerInfo.isConnected()) {
                    mScannerInfo.setWifiSsid(editTextWifiName.getText().toString());
                    mScannerInfo.setWifiPW(editTextWifiPW.getText().toString());
                    mScannerInfo.setMqttIp(editTextMqttIP.getText().toString());
                    /*Wifi 이름을 입력하는 함수*/
//                    BluetoothLeConnect.sendScannerData(mScannerInfo, secType, checkBoxEnt.isChecked());
                    BluetoothLeConnect.sendScannerData(mScannerInfo, securityLevel);
                    /*스캐너 정보를 저장한다.*/
                    setDeviceInfo();

                    /*
                     * 먼저 데이터를 전송한 다음에 진행 상황
                     * 액티비티를 띄워준다.
                     */
                    Intent intent=new Intent(MainActivity.this,InputProgressActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.gateway_is_not_connected
                            , Toast.LENGTH_SHORT).show();
                }

//                btnReadScannerInfo.setVisibility(View.VISIBLE);
//                btnSendScannerData.setVisibility(View.VISIBLE);

                break;
            case 1002:

                msg = handler.obtainMessage();
                msg.arg1 = 3;
                handler.sendMessage(msg);
                mScannerInfo.setConnected(false);

                btnReadScannerInfo.setVisibility(View.INVISIBLE);
                btnSendScannerData.setVisibility(View.INVISIBLE);

                break;
        }


    }


    /**
     * UI 변경 전용 핸들러
     */
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler()

    {
        public void handleMessage(Message msg)

        {
            /*
             * 1 : 스캐너 정보 읽기 핸들러
             * 2 : 블루투스 연결 됨 핸들러
             * 3 : 블루투스 연결 끊김 핸들러
             * 4 : 스캐너 정보 전송 상황 핸들러
             */
            switch ((int) msg.arg1) {
                case 1:
                    /*
                    * 한글 영문 숫자 점 만 추출해서
                    * filterStr 변수에 담는다.
                    */
                    String filterStr = CommonUtil.extractWord((String) msg.obj);

                    if (filterStr.equals("")) {

                    } else {

                    }
                    break;

                case 2:
                    //스캐너가 연결되면 프로그레스 바를 활성화 시킨다.
                    progressBar.setVisibility(View.VISIBLE);
                    textViewConnectState.setText(R.string.gateway_connected);
                    break;

                case 3:
                    //스캐너가 연결되지 않으면 프로그레스바를 숨긴다.
                    progressBar.setVisibility(View.GONE);
                    textViewConnectState.setText(R.string.gateway_not_connected);
                    break;
                case 4:
                    break;
            }
        }

    };


    @Override
    public void onBatteryEvent(int battery, String macAddress, String deviceName) {

    }

    @Override
    public void onClickEvent(String macAddress, String deviceName) {

    }

    /**
     * 스캐너 내부 정보를 보여주는 콜백 함수
     *
     * @param data
     */
    @Override
    public void onReceiveData(String data) {

        /*받은 데이터를 핸들러로 보냄*/
        Message msg = handler.obtainMessage();
        msg.arg1 = 1;
        msg.obj = (Object) data;
        handler.sendMessage(msg);
    }

    /**
     * 입력 진행 상황을 알려주는 콜백 함수
     *
     * @param data
     */
    @Override
    public void onInputState(String data) {
        /*현재 진행 상황을 핸들러로 보냄*/
        Message msg = handler.obtainMessage();
        msg.arg1 = 4;
        msg.obj = (Object) data;
        handler.sendMessage(msg);
    }

    public void scanStop(){
        if(BluetoothLeConnect != null) {
            if (BluetoothLeConnect.mListener != null) {
                BluetoothLeConnect.stop();
                BluetoothLeConnect = null;
            }
        }
    }

    /**
     * 스캐너 연결 해제 명령
     */
    @Override
    public void onCmdDisConnect() {
        scanStop();
    }

    @Override
    public void onReadSuccess(int what, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onWriteSuccess(int what) {

    }

    private void setDataBase(){
        ChoisDBHelper helper;
        SQLiteDatabase db;
        helper = new ChoisDBHelper(MainActivity.this, "newdb", null, 1);
        sDB = helper.getWritableDatabase();
        sDB = helper.getReadableDatabase();
        helper.onCreate(sDB);
    }
}
