package com.choistec.cms.scannerReg;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import com.choistec.cms.scannerReg.dbbase.ChoisDBHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class CmsViewActivity extends Activity implements View.OnClickListener {

    private Button  mChangeBtn,mChkBtn;
    private ImageView mSettingImg ,mLogoutImg, mCmsImg, mGateWayImg, mMonitorImg;
    private WebView mCmsWebView;
    private View mDrawerView;
    private DrawerLayout mDrawerLayout;
    private Context mContext;
    private WebSettings mWebViewSetting;
    private int mBackCount = 1;
    private FrameLayout mSmsViewLayout;
    FragmentTransaction mTransaction;
    FragmentSMS mFragmentSMS;
    FragmentManager mFm;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cms_web);
        mContext = this;
        initView();
        setClickEvent();
        setWebView();
    }
    private void initView(){
        mCmsWebView = (WebView)findViewById(R.id.cms_webview);
        mSettingImg = (ImageView) findViewById(R.id.setting_view);
        mChangeBtn = (Button)findViewById(R.id.change_btn);
        mDrawerView = (View)findViewById(R.id.drawer);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mGateWayImg = (ImageView)findViewById(R.id.gateway_move_btn);
        mMonitorImg = (ImageView)findViewById(R.id.monitor_move_btn);
        mLogoutImg = (ImageView)findViewById(R.id.log_out_img);
        mChkBtn = (Button)findViewById(R.id.red_light);
        boolean check = getSmsChk();
        if(check == true){
            mChkBtn.setVisibility(View.VISIBLE);
        }



    }
    private int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }
    private void setClickEvent(){
        mSettingImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerView);
            }
        });
        mChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(mFragmentSMS != null){

//                    FragmentTransaction transaction = mFm.beginTransaction();
//                    transaction.remove(mFragmentSMS);
//                    transaction.commit();
//                    mFragmentSMS.onDestroy();
//                    mFragmentSMS.onDetach();
//                    mFragmentSMS = null;
//                    mChangeBtn.setText("SMS");
//                }else{
//                    mSmsViewLayout = (FrameLayout)findViewById(R.id.sms_view_frame);
//                    mCmsWebView.setVisibility(View.GONE);
//                    mFragmentSMS = new FragmentSMS(mContext);
//                    mFm = getFragmentManager();
//                    FragmentTransaction transaction = mFm.beginTransaction();
//                    transaction.replace(R.id.sms_view_frame, mFragmentSMS);
//                    transaction.commit();
//                    mChangeBtn.setText("CMS");
//                }
                if(mChangeBtn.getText().toString().equals("CMS")){
                    mChangeBtn.setText("SMS");
                    mFragmentSMS.isHidden();
                    setWebView();
                    mSmsViewLayout.setVisibility(View.GONE);
                    mCmsWebView.setVisibility(View.VISIBLE);
                    mTransaction.remove(mFragmentSMS);

                }else{
                    mChangeBtn.setText("CMS");
                    mSmsViewLayout = (FrameLayout)findViewById(R.id.sms_view_frame);
                    mSmsViewLayout.setVisibility(View.VISIBLE);

                    mCmsWebView.setVisibility(View.GONE);
                    mFragmentSMS = new FragmentSMS(mContext);
                    mFm = getFragmentManager();
                    mTransaction = mFm.beginTransaction();
                    mTransaction.replace(R.id.sms_view_frame, mFragmentSMS);
                    mTransaction.commit();
                    mChangeBtn.setText("CMS");
                }
            }
        });
        /*
        * else if(mFragmentSMS != null && mFragmentSMS.isHidden()){
//                    mSmsViewLayout.setVisibility(View.VISIBLE);
//                    mCmsWebView.setVisibility(View.GONE);
                }
        * */
        mLogoutImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences("login",MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("login",false);
                editor.commit();
                startActivity(new Intent(CmsViewActivity.this,LoginActivity.class));
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
                startActivity(new Intent(mContext,CmsMonitorScanActivity.class));
            }
        });

    }
    @Override
    public void onClick(View v) {

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("BJY","CmsView on onDestroy");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d("BJY","CmsView on Pause");
    }

    @Override
    public void onBackPressed(){
        //TODO ?????? ????????? ?????? ?????? ???????????? ????????????.
        if(mCmsWebView.canGoBack()){
            mCmsWebView.goBack();
            mBackCount =1;
        }else{
            if(mBackCount == 1){
                Toast.makeText(mContext, "CMS ?????? ?????? ???????????? ????????? ???????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                mBackCount++;
            }else{
                super.onBackPressed();
                finish();
            }
        }
    }

    private void setWebView(){
        mCmsWebView.setWebViewClient(new WebViewClient()); //????????? ?????? ????????????
        mWebViewSetting = mCmsWebView.getSettings();
        mWebViewSetting.setJavaScriptEnabled(true); //???????????? ?????????????????? ?????? ??????
        mWebViewSetting.setSupportMultipleWindows(false); //?????? ????????? ??????
        mWebViewSetting.setJavaScriptCanOpenWindowsAutomatically(false); //?????????????????? ?????? ?????????(?????????) ?????? ??????
        mWebViewSetting.setLoadWithOverviewMode(true);//???????????? ?????? ??????
        mWebViewSetting.setUseWideViewPort(true); //?????? ????????? ????????? ?????? ??????
        mWebViewSetting.setSupportZoom(true); //??? ?????? ??????
        mWebViewSetting.setBuiltInZoomControls(true); //?????? ?????? ?????? ??????
        mWebViewSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL); //????????? ????????? ?????????
        mWebViewSetting.setCacheMode(WebSettings.LOAD_NO_CACHE); //???????????? ?????? ?????? ??????
        mWebViewSetting.setDomStorageEnabled(true); //??????????????? ?????? ??????
        mWebViewSetting.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        mCmsWebView.loadUrl("cms2.choistec.com/list");
    }
    private boolean getSmsChk(){
        boolean chk = false;
        Cursor cursor = ChoisDBHelper.selectAllData("sms_table","xpointer");
        if(cursor != null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while(cursor.moveToNext()){
                int ck = cursor.getInt(cursor.getColumnIndex("sms_check"));
                if(ck == 0){
                    chk = true;
                }
            }
            if(cursor.isClosed() != false){
                cursor.close();
            }
        }
        return chk;
    }
}
