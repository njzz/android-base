package com.librarys.tools.design.impl.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.librarys.tools.R;
import com.librarys.tools.services.NotifyService;
import com.librarys.tools.utils.SettingUtils;
import com.librarys.tools.services.ViewService;
import com.njzz.bases.common.BaseActivity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import com.librarys.tools.design.impl.view.Main2Activity.UserInfo;
import com.njzz.bases.common.GlobalNotice;
import com.njzz.bases.common.MessageSet;
import com.njzz.bases.common.SignalSlot;
import com.njzz.bases.utils.LogUtils;
import com.njzz.bases.utils.PermissionsUtils;
import com.njzz.bases.utils.ToastUtils;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BaseActivity.setDefaultInit();//在这里(MAIN)设置，则会在恢复时，显示主界面
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogUtils.e("main1 is onCreate.....");

        initStatusBar(true,true,0);

        PermissionsUtils.request(this, new String[]{"android.permission.READ_PHONE_STATE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"},
                new PermissionsUtils.Listener() {
                    @Override
                    public void permissionGranted(String[] permission) {
                        ToastUtils.show(MainActivity.this,"has all permissions");
                    }

                    @Override
                    public void permissionDenied(String[] permission) {
                        ToastUtils.show(MainActivity.this,"permissions denied.");
                    }
                });

        GlobalNotice.setListener(111, new SignalSlot.UISlot(this) {
            @Override
            public void onSignal(MessageSet ms) {
                onStartServer(null);
            }
        });
    }


    public void onNewAct(View v){

        Map<String,Object> par=new HashMap<>();
        par.put(Main2Activity.paramName,new UserInfo("张三","男"));
        startActivity(new Intent(this,Main2Activity.class),par);

    }

    public void onDial(View v){//拨号盘
//        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:123456"));
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //       //dial动作为调用拨号盘
        //       startActivity(intent);

        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_CALL_BUTTON);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setData(Uri.parse("tel:10086"));
        startActivity(intent);

    }

    public static void restart(){

    }

    public void onStartServer(View v){
        Intent intentOne = new Intent(this, ViewService.class);
        startService(intentOne);
    }

    public void onContentUser(View v){//联系人

        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setData(Uri.parse("tel:10086"));
        startActivityForResult(intent,0);


//        File fm=new File(PathUtil.get())
//
//        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        scanIntent.setData(Uri.fromFile(new File(filePath)));
//        this.sendBroadcast(scanIntent);

        //////////////////////////////////////音量等
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setMicrophoneMute(!audioManager.isMicrophoneMute());//mic静音
//        audioManager.setSpeakerphoneOn(!audioManager.isSpeakerphoneOn());//扬声器
//        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,0,0);//index 最小0，最大为 (MAX_STREAM_VOLUME)
//        audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL ,AudioManager.ADJUST_RAISE,0);//ADJUST_RAISE，升高，ADJUST_LOWER  降低


        //////////////////////录音
        //https://www.cnblogs.com/blosaa/p/9554871.html

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//获得选择联系人结果
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode==0) {
                ContentResolver reContentResolverol = getContentResolver();
                Uri contactData = data.getData();
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null,
                        null);
                while (phone.moveToNext()) {
                    String usernumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Toast.makeText(this, usernumber + " (" + username + ")", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    public void onWhiteList(View view) {
        SettingUtils.enterWhiteListSetting(this);
    }

    public void onKeepAlive(View view) {
        //Intent intent = new Intent(this,NotifyService.class);
        //startService(intent);
        String string = Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");
        LogUtils.i("已经允许使用通知权的应用:" + string);
        if (!string.contains(NotifyService.class.getName())) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
    }

    public void onWhiteList2(View view) {
        //判断用户是否开启这个程序的白名单
        //API大于等于23才能用添加到白名单
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //获取电量管理员
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

            //方法1 弹出菜单框让用户自己选择
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
               Intent intent=new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

}
