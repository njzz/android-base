package com.librarys.tools.boardcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.librarys.tools.services.forgroundServer;
import com.njzz.bases.utils.LogUtils;

public class BoardReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i= new Intent(context, forgroundServer.class);
        LogUtils.i("Recviver Msgs~~~~~");
        context.startService(i);
    }
}
