package com.librarys.tools.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telecom.Call;
import android.telecom.InCallService;
import android.telecom.VideoProfile;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.librarys.tools.R;
import com.librarys.tools.common.ITelephony;
import com.librarys.tools.design.impl.presenter.Main2ActivityPresenter;
import com.librarys.tools.design.impl.view.MainActivity;
import com.njzz.bases.common.ActManager;
import com.njzz.bases.common.GlobalNotice;
import com.njzz.bases.utils.LogUtils;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ViewService extends InCallService {
    private static WindowManager windowManager;
    private static View viewShow=null;

    static public Call mCall;
    static public int mType=0;

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        call.registerCallback(callback);
        mCall = call;

        if (call.getState() == Call.STATE_RINGING) {
            mType=1;
        } else if (call.getState() == Call.STATE_CONNECTING) {
            mType=2;
        }
        LogUtils.d("server call add..");
        //showOver();
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        call.unregisterCallback(callback);
        mCall = null;
        LogUtils.d("server call remove..");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int r=super.onStartCommand(intent,flags,startId);
        LogUtils.d("server started..");
        ITelephony ll;
        return r;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(viewShow!=null) {
            windowManager.removeView(viewShow);
        }

        LogUtils.w("service destroy");

        GlobalNotice.Notice(111);
    }

    private void showOver() {
        if(viewShow==null) {
            LogUtils.d("show foreground..");
            Context last = ActManager.ins().find(MainActivity.class);
            viewShow = View.inflate(last, R.layout.activity_main2, null);
            viewShow.setVisibility(View.VISIBLE);

            windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            //layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;//这个优先级低一些
            //layoutParams.type= WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;//WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            //layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            //| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

            layoutParams.width = 600;
            layoutParams.height = 800;
            layoutParams.gravity = Gravity.CENTER;
            //layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //layoutParams.format = PixelFormat.RGBA_8888 | PixelFormat.TRANSLUCENT;

            windowManager.addView(viewShow, layoutParams);
            eventSet(viewShow.findViewById(R.id.reject),true);
            eventSet(viewShow.findViewById(R.id.answer),false);

            new Main2ActivityPresenter(last, viewShow.findViewById(R.id.recyler_show)).start();

            LogUtils.d("add view show......");
        }
    }

    private void eventSet(View v,boolean reject){
        v.setVisibility(View.VISIBLE);
        v.setOnClickListener(view->{
                if(mCall!=null) {
                    if (reject) {
                        mCall.reject(false,"");
                    }else{
                        mCall.answer(VideoProfile.STATE_AUDIO_ONLY);
                    }
                }
            windowManager.removeView(viewShow);
            viewShow=null;
        });
    }






///////////////////////////////////




    private Call.Callback callback = new Call.Callback()  {
        @Override
        public void onStateChanged(Call call, int state) {
            super.onStateChanged(call, state);
            switch (call.getState()) {
                case Call.STATE_ACTIVE:
                    // 通话中
                    //GlobalNotice.SysNotice(NOTIFY_STATE_ACTIVE);
                    LogUtils.e("state_：STATE_ACTIVE");
                    break;
                case Call.STATE_RINGING:
                    LogUtils.e("state_：STATE_RINGING");
                    break;
                case Call.STATE_CONNECTING:
                    LogUtils.e("state_：STATE_CONNECTING");
                    break;
                case Call.STATE_DIALING:
                    //呼叫中
                    LogUtils.e("state_：STATE_DIALING");
                    break;
                case Call.STATE_DISCONNECTED:
                    // 通话结束
                    //GlobalNotice.SysNotice(NOTIFY_STATE_DISCONNECTED);
                    LogUtils.e("state_：STATE_DISCONNECTED");
                    break;
                case Call.STATE_DISCONNECTING:
                    // 正在结束通话
                    //GlobalNotice.SysNotice(NOTIFY_STATE_DISCONNECTING);
                    LogUtils.e("state_：STATE_DISCONNECTING");
                    break;
                case Call.STATE_HOLDING:
                    //通话保持
                    LogUtils.e("state_：STATE_HOLDING");
                    break;
                case Call.STATE_NEW:
                    LogUtils.e("state_：STATE_NEW");
                    break;
                case Call.STATE_PULLING_CALL:
                    LogUtils.e("state_：STATE_PULLING_CALL");
                    break;
                case Call.STATE_SELECT_PHONE_ACCOUNT:
                    LogUtils.e("state_：STATE_SELECT_PHONE_ACCOUNT");
                    break;
                default:
                    LogUtils.e("state_：default");
                    break;
            }
        }
    };
}
