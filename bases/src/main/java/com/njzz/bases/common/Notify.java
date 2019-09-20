package com.njzz.bases.common;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

public class Notify {
    public interface Normal extends LiftAble{
        void OnNotify(int arg1, int arg2, Object argObj);
    }
    public interface UI extends Normal{
    }

    //多个通知
    public static void AsyncSend(List<Normal> normal, int arg1, int arg2, Object argObj){
        if(normal!=null) {
            new Thread(() -> Send(normal, arg1, arg2, argObj)).start();
        }
    }
    //单个通知
    public static void AsyncSend(Normal normal,int arg1,int arg2,Object argObj){
        if(normal!=null) {
            new Thread(() -> Send(normal, arg1, arg2, argObj)).start();
        }
    }

    //多个通知
    public static void Send(List<Normal> normal, int arg1, int arg2, Object argObj){
        if(normal!=null){
            for(Normal n:normal){
                Send(n,arg1,arg2,argObj);
            }
        }
    }
    //单个通知
    public static void Send(Normal normal,int arg1,int arg2,Object argObj){
        if(normal!=null) {
            if(normal instanceof UI)
                UINotify(normal, arg1, arg2, argObj);
            else
                normal.OnNotify(arg1,arg2,argObj);
        }
    }

    static private void UINotify(final Normal normal,final int arg1,final int arg2,final Object argObj){
       new Handler(Looper.getMainLooper(), message -> {
            if(message.what==0x11) {
                normal.OnNotify(arg1,arg2,argObj);
            }
            return true;
        }).sendEmptyMessage(0x11);
    }
}
