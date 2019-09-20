package com.njzz.bases.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppSet extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActManager.ins();//初始化管理器
        /*网络改变监听*/
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
                    //网络可用的回调
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        GlobalNotice.Notice(Notice.NET_AVAILABLE);
                    }
                    //网络丢失的回调
                    @Override
                    public void onLost(Network network) {
                        super.onLost(network);
                        GlobalNotice.Notice(Notice.NET_LOST);
                    }
                });
            }
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                GlobalNotice.Notice(Notice.ACTIVITY_CREATE,activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) { }
            @Override
            public void onActivityResumed(@NonNull Activity activity) { }
            @Override
            public void onActivityPaused(@NonNull Activity activity) {   }
            @Override
            public void onActivityStopped(@NonNull Activity activity) {  }
            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {  }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                GlobalNotice.Notice(Notice.ACTIVITY_DESTROY,activity);

            }
        });//注册activity生命周期管理
    }
}
