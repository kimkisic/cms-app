package com.choistec.cms.scannerReg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends Activity implements View.OnClickListener {
    private TextView mLogin_title, mLogin_Txt, mLogin_Pwd_Txt;
    private EditText mId_Edit, mPwd_Edit;
    private Button mLoginBtn;
    private DrawerLayout mDrawerLayout;
    private View mDrawerView;
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private ImageView mSettingImg;
    private ImageView mCmsImg, mGateWayImg, mMonitorImg;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mContext = this;
        initView();
        setClickEvent();
    }
    private boolean checkLogin(){
        return false;
    }
    private void initView(){
        mLogin_title = (TextView)findViewById(R.id.lgn_title);
        mLogin_Txt = (TextView)findViewById(R.id.id_txt);
        mLogin_Pwd_Txt = (TextView)findViewById(R.id.pwd_edit_txt);

        mId_Edit = (EditText)findViewById(R.id.id_edit_txt);
        mPwd_Edit = (EditText)findViewById(R.id.pwd_edit_txt);

        mLoginBtn = (Button)findViewById(R.id.login_btn);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerView = (View)findViewById(R.id.drawer);
        mSettingImg = (ImageView)findViewById(R.id.setting_view);
        mGateWayImg = (ImageView)findViewById(R.id.gateway_move_btn);
        mMonitorImg = (ImageView)findViewById(R.id.monitor_move_btn);
    }
    private void setClickEvent(){
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mId_Edit.getText().toString() != null && mPwd_Edit.getText().toString() != null){
                    new JSONTask().execute("http://cms2.choistec.com/mobile/login");
                }
            }
        });
        mSettingImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerView);
            }
        });
        mCmsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BJY","cms move Btn Click");
                Intent intent = new Intent(mContext, CmsViewActivity.class);
                startActivity(intent);
            }
        });
        mGateWayImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BJY","Gate Way Btn Click");
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
            }
        });
        mMonitorImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Move to Monitor
            }
        });

    }



    @Override
    public void onClick(View v) {

    }
    @Override
    public void onResume(){
        super.onResume();
        if(mDrawerLayout.isDrawerOpen(mDrawerView)){
            mDrawerLayout.closeDrawer(mDrawerView);
        }
    }
    @Override
    public void onBackPressed(){
        //TODO 두번 누를시 종료 구현
        if(mDrawerLayout.isDrawerOpen(mDrawerView)){
            mDrawerLayout.closeDrawer(mDrawerView);
        }else{
            super.onBackPressed();
        }
    }






    public class JSONTask extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("gid", mId_Edit.getText().toString());
                jsonObject.accumulate("gpw", mPwd_Edit.getText().toString());
                HttpURLConnection con = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Cashe-Control", "no-cache");
                    con.setRequestProperty("Content-Type", "application/json");
//                    con.setRequestProperty("Accept", "text/html");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.connect();
                    // 서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    // 버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close(); // 버퍼를 받아줌
                    // 서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    return buffer.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close(); // 버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result == null){
                Log.d("BJY","Result is NUll");
                return;
            }
            if(result.equals("true")){
                Log.d("BJY","Login Success");
                SharedPreferences prefs = mContext.getSharedPreferences("login",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("login",true);
                editor.commit();
                Intent intent = new Intent(mContext, CmsViewActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(mContext, "가입하지 않은 아이디이거나, 잘못된 비밀번호입니다.", Toast.LENGTH_SHORT).show();
            }
            }
        }
    }
