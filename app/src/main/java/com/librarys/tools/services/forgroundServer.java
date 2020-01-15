package com.librarys.tools.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.librarys.tools.R;
import com.njzz.bases.utils.LogUtils;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class forgroundServer extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotify();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 重启自己
        Intent intent = new Intent(getApplicationContext(), forgroundServer.class);
        startService(intent);
    }


    public static final int NOTICE_ID = 100;
    private String processName;
    private final String CHANNEL_ONE_ID = "CHANNEL_ONE_ID_ID";
    private final String CHANNEL_ONE_NAME= "CHANNEL_ONE_ID_NAME";
    private String mMyKey;
    void forgroundStart(){
        Notification.Builder builder = new Notification.Builder(this);
        NotificationChannel notificationChannel;
        //进行8.0的判断
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel= new NotificationChannel(CHANNEL_ONE_ID,CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            //.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);

            builder.setChannelId(CHANNEL_ONE_ID);
        }

        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,, 0);
        Notification notification= builder.setTicker("Nature")
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setContentIntent(pendingIntent)
                .setContentTitle("这是一个测试标题")
                .setContentText("这是一个测试内容")
                .build();
        notification.flags|= Notification.FLAG_AUTO_CANCEL;
        startForeground(NOTICE_ID, notification);

        //Utils.UIRun(this::removeMy,3000);
    }

//    void removeMy(){
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            for (StatusBarNotification sbn : getActiveNotifications()) {
//                if (sbn.getNotification().getChannelId() .equals( CHANNEL_ONE_ID) ){
//                    cancelNotification(sbn.getKey());
//                    break;
//                }
//            }
//        }
//    }

    private String getMyName(){
        if(processName==null) {
            int pid = android.os.Process.myPid();
            ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                if (process.pid == pid) {
                    processName = process.processName;
                    break;
                }
            }
        }
        return processName;
    }

    void sendNotify(){
        LogUtils.i("sendNotify");
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am != null) {
            int elapseTime = 10 * 1000;
            long interval = SystemClock.elapsedRealtime() + elapseTime;
            //传递userId参数给MyNewTaskReceiver,为了到时候回传回来
            LogUtils.i("sended am msg:"+interval);
            Intent i = new Intent("repeatKeepRecevier");
            i.setPackage(getPackageName());
            i.putExtra("uid", "msg_test_id");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.ELAPSED_REALTIME, interval, pi);
        } else {
            LogUtils.e("am is null");
        }
    }

}
