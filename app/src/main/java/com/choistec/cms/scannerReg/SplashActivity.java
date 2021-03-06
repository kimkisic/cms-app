package com.choistec.cms.scannerReg;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        PermissionCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(getApplicationContext(), R.string.gps_off, Toast.LENGTH_LONG).show();
            AlertDialog.Builder dlg = new AlertDialog.Builder(SplashActivity.this);
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
        } else {
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("BJY","Change LoginActivity !!!");
                    SharedPreferences pref = getPreferences(MODE_PRIVATE);
                    if( pref.getBoolean("login",false)){
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    }else{
                        startActivity(new Intent(SplashActivity.this,CmsViewActivity.class));
                    }
                    finish();
                }
            }, 4000);
        }
//        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            Toast.makeText(getApplicationContext(), R.string.gps_off, Toast.LENGTH_LONG).show();
//            AlertDialog.Builder dlg = new AlertDialog.Builder(SplashActivity.this);
//            dlg.setTitle(R.string.check_gps);
//            dlg.setMessage(R.string.move_gps);
//            dlg.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                    startActivity(intent);
//                }
//            });
//            dlg.show();
//        }
    }

    /**
     * ????????? ?????? ?????????????????? ????????? ????????????. ???????????? ????????? ???????????? ????????? ????????????.
     */
    private void PermissionCheckProc(){

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() { //????????? ?????? ?????? ??? ??????
//                goMainActivity();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) { //???????????? ???????????? ????????? ????????? ??????

                try{
                    new AlertDialog.Builder(SplashActivity.this)
                            .setMessage(getString(R.string.permission_msg2))
                            .setPositiveButton(getString(R.string.app_finish), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish(); //?????? ?????????
                                }
                            }).setNegativeButton(getString(R.string.permission_setting), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            PermissionCheckProc(); //?????? ????????? ????????? ??????
                        }
                    }).show();
                }catch(Exception e){}

            }
        };
        /*LogSave ????????? ???????????????
        ???????????? ????????? WRITE_EXTERNAL_STORAGE ???????????? ???????????? ??????.*/
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(getString(R.string.permission_msg)) //????????? ?????? ?????????
                //.setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE) //????????? ?????????
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE) //????????? ?????????
                .check();

        //????????? ???????????? ???????????? ????????? ??? ????????????.\n\n[??????] > [??????] ?????? ????????? ???????????? ????????????. <-- ????????? ?????? ?????????
    }

    /**
     * ????????? ?????? ?????? ??? ??????
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void goMainActivity(){

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(getApplicationContext(), R.string.gps_off, Toast.LENGTH_LONG).show();
            AlertDialog.Builder dlg = new AlertDialog.Builder(SplashActivity.this);
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
        } else {
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }, 4000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*???????????? ?????? ?????????*/
        if(requestCode == REQUEST_ENABLE_BT){
//            goMainActivity();
        }
    }

    /**
     * onResume?????? ?????? ????????? ??? ???????????? ????????? ?????????????????? ?????? ????????? ????????? ????????????.
     */
    private void PermissionCheck(){

        boolean granted = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }

        if(granted == false){ //???????????? ???????????? ?????? ??????
            PermissionCheckProc();
        }else{  //????????? ?????? ????????????
//            goMainActivity();
        }

    }

}