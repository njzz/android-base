package com.njzz.bases.common;

import android.app.Activity;

import com.njzz.bases.utils.Utils;

import java.util.LinkedList;
import java.util.List;

/**
 * 通用通知器
 */
public class Notify {
    public static abstract class Receiver extends LifeAble {
        /**
         * @param activity 关联生命周期的activity
         */
        public Receiver(Activity activity){
            super(activity);
        }

        //监视器
        private List<Receiver> mObserver;
        public void addObserver(Receiver normal){
            if(normal==null) return;
            if(mObserver==null){
                synchronized (this){
                    if(mObserver==null)
                        mObserver=new LinkedList<>();
                }
            }

            synchronized (this) {
                if (mObserver.indexOf(normal) == -1) {
                    mObserver.add(normal);
                }
            }
        }
        public void removeObserver(Receiver normal){
            if(mObserver!=null){
                synchronized (this) {
                    mObserver.remove(normal);
                }
            }
        }
        abstract public void OnNotify(int arg1, int arg2, Object argObj);
    }
    public static abstract class UIReceiver extends Receiver {
        public UIReceiver(Activity activity){
            super(activity);
        }
    }

    //多个通知
    public static void AsyncSend(final List<Receiver> receiver, int arg1, int arg2, Object argObj){
        if(receiver !=null) {
            new Thread(() -> Send(receiver, arg1, arg2, argObj)).start();
        }
    }
    //单个通知
    public static void AsyncSend(Receiver receiver, int arg1, int arg2, Object argObj){
        if(receiver !=null) {
            new Thread(() -> Send(receiver, arg1, arg2, argObj)).start();
        }
    }

    //多个通知
    public static void Send(final List<Receiver> receiver, int arg1, int arg2, Object argObj){
        if(receiver !=null){
            for(Receiver n: receiver){
                Send(n,arg1,arg2,argObj);
            }
        }
    }
    //单个通知
    public static void Send(final Receiver receiver, int arg1, int arg2, Object argObj){
        if(receiver !=null) {

            if(receiver.mObserver!=null){//先通知observer
                Send(receiver.mObserver,arg1,arg2,argObj);
            }

            if(receiver instanceof UIReceiver)
                Utils.UIRun(() -> receiver.OnNotify(arg1,arg2,argObj));
            else
                receiver.OnNotify(arg1,arg2,argObj);
        }
    }
}
