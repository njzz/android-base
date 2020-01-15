package com.librarys.tools.services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.njzz.bases.utils.LogUtils;

public class NotifyService extends NotificationListenerService {
    @Override
    public void onListenerConnected() {
        //当连接成功时调用，一般在开启监听后会回调一次该方法
        LogUtils.d("on listen connected");
        super.onListenerConnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //当收到一条消息时回调，sbn里面带有这条消息的具体信息
        LogUtils.d("onNotificationPosted");
        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //当移除一条消息的时候回调，sbn是被移除的消息
        LogUtils.d("onNotificationRemoved");
        super.onNotificationRemoved(sbn);
    }
}
