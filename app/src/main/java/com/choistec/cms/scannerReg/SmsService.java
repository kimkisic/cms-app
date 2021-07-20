package com.choistec.cms.scannerReg;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.choistec.cms.scannerReg.dbbase.ChoisDBHelper;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.sql.Date;
import java.text.SimpleDateFormat;

import static com.choistec.cms.scannerReg.dbbase.ChoisDBHelper.sDB;

public class SmsService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        makeNotification(remoteMessage);
    }

    @Override
    public void onNewToken(String s) {
        Log.e("token?", s);
        super.onNewToken(s);
    }

    private void makeNotification(RemoteMessage remoteMessage) {
        try {
            int notificationId = -1;
            Context mContext = getApplicationContext();

            Intent intent = new Intent(this, MainActivity.class);
            if(ChoisDBHelper.sDB == null){
                ChoisDBHelper helper;
                helper = new ChoisDBHelper(this, "newdb", null, 1);
                sDB = helper.getWritableDatabase();
                sDB = helper.getReadableDatabase();
                helper.onCreate(sDB);
                Log.d("SmsService","sDB is Null !!!");
            }else{
                Log.d("SmsService","sDB is Not Null !!!");

            }
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("body");

            Log.d("BJY"," push title : "+title+" // msg : "+message);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
            String channelId ="test";
            Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.alarm_icon)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setSound(defaultUri)
                            .setContentIntent(pendingIntent)
                            .setPriority(Notification.PRIORITY_HIGH);

            NotificationManager notificationManager =
                    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0, notificationBuilder.build());

            String id = "xpointer";
            java.sql.Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat im = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = im.format(date);
            Log.d("nowTime"," value : "+ im.format(date));
//            Date date = new Date();
//            String time = System
            Log.d("SmsService"," id : "+id +"  / message : "+message + " date : "+im);
            Long result = ChoisDBHelper.insertValue(ChoisDBHelper.SMS_TABLE, id, message, "2021-07-13 12:34:22", false);
            Log.d("SmsService"," result : "+result );
        } catch (NullPointerException nullException) {
            Toast.makeText(getApplicationContext(), "알림에 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            Log.e("SmsService", nullException.toString());
        }
    }
    private void scheduleJob(){
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(null)
                .build();
        dispatcher.schedule(myJob);
    }
    private void setRegistrationToServer(String token){
        //TODO implemet this method to send token to your app server.
    }
    private void sendNotification(){

    }
}