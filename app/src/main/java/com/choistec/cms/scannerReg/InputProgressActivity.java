package com.choistec.cms.scannerReg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.choistec.cms.scannerReg.util.CommonInfo;

public class InputProgressActivity extends Activity {

    private MainBCRReceiver mMainBCR = new MainBCRReceiver();

    public class MainBCRReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, android.content.Intent intent) {

            Log.d("mylog", "입력 정보 리시버 들어옴");
            String action = intent.getAction();
            switch (action) {
                case CommonInfo.MSG_INPUT_PROGRESS_TITLE_STR:
                    String inputProgress = intent.getStringExtra(CommonInfo.MSG_INPUT_PROGRESS_DATA_STR);
                    /*
                     * 마지막 단계 메시지가 오면 버튼을 보이도록 한다.
                     * 프로그레스바는 숨긴다.
                     */
                    if (inputProgress.equals(getString(R.string.input_progress_fail_input_msg))) {
                        textViewSendState.setText(inputProgress);
                        btnCancel.setVisibility(View.VISIBLE);
//                        btnFinish.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    } else if (inputProgress.equals(getString(R.string.input_progress_success_input_msg))){
                        textViewSendState.setText(inputProgress);
                        btnCancel.setVisibility(View.VISIBLE);
                        btnCms.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        textViewSendState.setText(inputProgress);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_input_progress);

        //뷰 변수 선언
        setViewValue();
        //브로드 캐스트 변수를 셋팅하는 함수
        setBrodcastSetting();
    }

    /**
     * 브로드 캐스트 변수를 셋팅하는 함수
     */
    public void setBrodcastSetting() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonInfo.MSG_INPUT_PROGRESS_TITLE_STR);
        filter.addAction(CommonInfo.MSG_INPUT_PROGRESS_DATA_STR);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMainBCR, filter);

    }

    //진행 상황 텍스트뷰
    TextView textViewSendState;
    //닫기 버튼*
    Button btnCancel;
    Button btnFinish;
    Button btnCms;
    //프로그레스바
    ProgressBar progressBar;

    /**
     * 뷰 변수 선언
     */
    public void setViewValue() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textViewSendState = (TextView) findViewById(R.id.textViewSendState);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnFinish = (Button) findViewById(R.id.btn_finish);
        btnCms = (Button) findViewById(R.id.btn_cms);

        btnCms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cms2.choistec.com"));
                startActivity(intent);
                finish();
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
                finishAndRemoveTask();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        btnCancel.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {

                finish();
            }

        });
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
                    break;

                case 2:
                    break;

                case 3:
                    break;

                case 4:
                    textViewSendState.setText((String) msg.obj);
                    break;


            }
        }

    };

    @Override
    protected void onDestroy() {
        //브로드 캐스트 리시버 종료
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);
        super.onDestroy();
    }

}
