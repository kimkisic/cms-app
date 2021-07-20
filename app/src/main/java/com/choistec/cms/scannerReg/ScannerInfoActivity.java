package com.choistec.cms.scannerReg;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.choistec.cms.scannerReg.util.CommonInfo;

import java.util.ArrayList;

public class ScannerInfoActivity extends Activity {


    private MainBCRReceiver mMainBCR = new MainBCRReceiver();

    public class MainBCRReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, android.content.Intent intent) {

            // BluethoothLeConnect의 LocalBroadcastManager를 통해 snedBroadcast하면 onReceive에서 받는다
            Log.d("mylog","리시버 들어옴");

            String action = intent.getAction();
            switch (action) {
                case CommonInfo.MSG_SCANNER_INFO_TITLE_STR:
                    String scannerInfo = intent.getStringExtra(CommonInfo.MSG_SCANNER_INFO_DATA_STR);
                    Log.d("mylog", "스캐너 값 : " + scannerInfo);
                    setTextScannerInfo(scannerInfo);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_info);

        //뷰 변수 선언
        setViewValue();

        //브로드 캐스트 변수를 셋팅하는 함수
        setBrodcastSetting();
    }

    /**
     * 브로드 캐스트 변수를 셋팅하는 함수
     */
    public void setBrodcastSetting(){

        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonInfo.MSG_SCANNER_INFO_TITLE_STR);
        filter.addAction(CommonInfo.MSG_SCANNER_INFO_DATA_STR);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMainBCR, filter);

    }

    ArrayList<String> dataArrScannerInfo = new ArrayList<String>();

    /**
     * 데이터가 들어오면 저장하고 정렬해서
     * 스캐너 정보를 각 타이틀에 맞게
     * 표현해주는 함수
     * @param data
     */
    public void setTextScannerInfo(String data){
        dataArrScannerInfo.add(data);
        if(dataArrScannerInfo.size() == 3){
            textViewSsid.setText(dataArrScannerInfo.get(1));
            textViewWifiPw.setText(dataArrScannerInfo.get(2));
            textViewMqttIp.setText(dataArrScannerInfo.get(0));

            /*
            * 입력이 끝나면 dataArrScannerInfo 변수를
            * 초기화 한다.
            */

            dataArrScannerInfo = new ArrayList<String>();
        }
    }

    @Override
    protected void onDestroy() {

        //브로드 캐스트 리시버 종료
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);

        super.onDestroy();
    }


    TextView textViewSsid;
    TextView textViewWifiPw;
    TextView textViewMqttIp;
    //진행 상황 텍스트뷰
    TextView textViewSendState;
    //닫기 버튼*
    Button btnCancel;
    /**
     * 뷰 변수 선언
     */
    public void setViewValue() {

        textViewSsid = (TextView)findViewById(R.id.textViewSsid);
        textViewWifiPw = (TextView)findViewById(R.id.textViewWifiPw);
        textViewMqttIp = (TextView)findViewById(R.id.textViewMqttIp);

        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {

                finish();
            }

        });
    }

}
