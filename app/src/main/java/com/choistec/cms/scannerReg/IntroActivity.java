package com.choistec.cms.scannerReg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.choistec.cms.scannerReg.util.CommonInfo;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

/**
 * 인트로 클래스
 */

public class IntroActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    Button btnCheck;

    LocationManager locationManager;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        setViewValue();

        /*onResume에서 필수 퍼미션 중 한개라도 빠지면 라이브러리를 통해 퍼미션 팝업이 올라온다.*/
        PermissionCheck();

        setClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(getApplicationContext(), R.string.gps_off, Toast.LENGTH_LONG).show();
            AlertDialog.Builder dlg = new AlertDialog.Builder(IntroActivity.this);
            dlg.setTitle(R.string.check_gps);
            dlg.setMessage(R.string.move_gps);
            dlg.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            });
            dlg.show();
        }
    }

    /**
     * 뷰와 관련된 변수를 셋팅하는 함수
     */
    public void setViewValue(){

        //현재 앱 버전을 찾아 텍스트뷰에 넣어준다.
        TextView textViewVersion = (TextView) findViewById(R.id.textViewVersion);
        textViewVersion.setText("CMSG01 Ver "+ CommonInfo.VERSION);

    }

    /**
     * 클릭 리스너를 선언하는 함수
     */
    public void setClick(){

        /*
        * 확인 버튼을 누르면 앱이 종료되고 MainActivity 로 전환된다.
        */
        btnCheck.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            }
        });

    }

    /**
     * 퍼미션 체크 라이브러리로 체크를 진행한다. 자동으로 거부한 퍼미션만 골라서 보여준다.
     */
    private void PermissionCheckProc(){

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() { //퍼미션 모두 허용 시 호출
                goMainActivity();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) { //한개라도 퍼미션이 거부된 상태인 경우

                try{
                    new AlertDialog.Builder(IntroActivity.this)
                            .setMessage(getString(R.string.permission_msg2))
                            .setPositiveButton(getString(R.string.app_finish), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish(); //앱을 종료함
                                }
                            }).setNegativeButton(getString(R.string.permission_setting), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            PermissionCheckProc(); //다시 퍼미션 팝업을 띄움
                        }
                    }).show();
                }catch(Exception e){}

            }
        };
        /*LogSave 함수를 실행하면서
        테스트를 위해선 WRITE_EXTERNAL_STORAGE 퍼미션을 추가해야 된다.*/
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(getString(R.string.permission_msg)) //퍼미션 거부 메시지
                //.setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE) //필요한 퍼미션
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE) //필요한 퍼미션
                .check();

        //권한을 불허하면 서비스를 사용할 수 없습니다.\n\n[설정] > [권한] 에서 권한을 허용하여 주십시오. <-- 퍼미션 거부 메시지
    }

    /**
     * 퍼미션 전부 허용 시 진행
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void goMainActivity(){

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }else {


        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*블루투스 권한 요청시*/
        if(requestCode == REQUEST_ENABLE_BT){
            goMainActivity();
        }

    }

    /**
     * onResume에서 필수 퍼미션 중 한개라도 빠지면 라이브러리를 통해 퍼미션 팝업이 올라온다.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void PermissionCheck(){

        boolean granted = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }

        if(granted == false){ //퍼미션이 한개라도 빠진 경우
            PermissionCheckProc();
        }else{  //퍼미션 모두 허용상태
            goMainActivity();
        }

    }

}
