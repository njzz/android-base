package com.librarys.tools.boardcast;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.njzz.bases.common.GlobalNotice;
import com.njzz.bases.utils.LogUtils;

public class PhoneReceiver extends BroadcastReceiver {
    boolean mSetListener=false;
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("action "+intent.getAction());
        if(Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())){//如果是去电
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            onCall(phoneNumber);
        }else{//设置一个监听器
            if(!mSetListener) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                if (tm != null) {
                    LogUtils.d("set listener..");
                    mSetListener=true;
                    tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
                }
            }
        }
    }

    private void onCall(String strPhone){//去电
        LogUtils.d( "去电:" + strPhone);
        GlobalNotice.Notice(111);
    }

    private void onHandsUp(){//空闲/挂断
        LogUtils.d("挂断");
    }

    private void onAnswer(){//接通
        LogUtils.d("接听");
    }

    private void onInComing(String strPhone){
        GlobalNotice.Notice(111);
        LogUtils.d("来电:"+strPhone);
    }

    PhoneStateListener listener=new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
        //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:
                    onHandsUp();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    onAnswer();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    onInComing(incomingNumber);
                    break;
            }
        }
    };
}
