package com.njzz.bases.common;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
  * @Date 2019/8/28 18:06
  * @Desc 信号槽
  */
public class SignalSlot {
    //如果slot是匿名内部类，它会持有外部类的生命周期，而signaler会持有slot
    //即：如果signaler是常驻类对象，则slot不能是非常驻类的匿名对象，否则可能造成内存泄漏
    //如果slot在非常驻类里，应该申明为命名对象，然后在slot外部类生命周期将结束时，调用signaler的remove方法
    static public abstract class Slot extends LifeAble{
        public Slot(Activity activity){
            super(activity);
        }
        private int mCare= SysNotice.ANY;//any case
        private boolean isCare(int what){
            return mCare== SysNotice.ANY||mCare==what;
        }
        //////////////////////////////////////
        public abstract void onSignal(int what,int arg1,int agr2,Object argObj);
    }

    //ui slot ，会在ui线程通知
    static public abstract class UISlot extends Slot{
        public UISlot(Activity activity){
            super(activity);
        }
    }

    public static class Signaler {
        private List<Slot> mListSolt;

        public boolean addSolt(Slot slot){
            return addSolt(SysNotice.ANY,slot);
        }
        public boolean addSolt(int care,Slot slot) {
            if(slot!=null){
                slot.mCare=care;
                return addFinal(slot);
            }
            return false;
        }

        private boolean addFinal(Slot slot){
            synchronized (this) {
                if (mListSolt == null) {
                    mListSolt = new ArrayList<>();
                }
                if ( mListSolt.indexOf(slot)==-1) {//如果不存在
                    if (slot instanceof UISlot) {//最后加入的ui-slot有最优通知权
                        mListSolt.add(0, slot);
                    } else
                        mListSolt.add(slot);
                }
            }
            return true;
        }

        public void removeSlot(Slot slot){
            if(slot!=null) {
               synchronized (this) {
                   int index = mListSolt.indexOf(slot);
                   if (index != -1) {
                       mListSolt.remove(index);
                   }
               }
            }
        }

        private void removeSlot(Activity activity){
            synchronized (this) {
                for (int i = 0; i < mListSolt.size(); ) {
                    if(activity.equals(mListSolt.get(i).getAttached())){
                        mListSolt.remove(i);
                    }else{
                        ++i;
                    }
                }
            }
        }
        /////////////////////////////////////////////
        //异步通知
        public void signal_async(int what){
            signal_async(what,0,0,null);
        }
        //异步通知
        public void signal_async(int what,int arg1,int arg2,Object argObj){
            new Thread(() -> signal(what,arg1,arg2,argObj)).start();
        }

        //同步通知(UI依然会异步)
        public void signal(int what){
            signal(what,0,0,null);
        }
        //同步通知(UI通知依然会异步)
        public void signal(int what,int arg1,int arg2,Object argObj){
            if(what== SysNotice.ACTIVITY_DESTROY){//生命周期管理，自身不能从LifeBasedClass派生，不然不能初始化发布器
                removeSlot((Activity) argObj);
            }

            List<Slot> notifys = copy();
            if (notifys != null) {
                for (Slot slot : notifys) {//可以在正在进行的通知里，增加或者删除slot，不会有问题
                    if (slot.isCare(what)) {
                        if (slot instanceof UISlot) {//ui notify
                            final Slot ssn = slot;
                            new Handler(Looper.getMainLooper(), message -> {
                                if (message.what == 1) {
                                    try {
                                        ssn.onSignal(what, arg1, arg2, argObj);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                return true;
                            }).sendEmptyMessage(1);
                        } else {
                            try {
                                slot.onSignal(what, arg1, arg2, argObj);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        //拷贝一份list
        private List<Slot> copy(){
            List<Slot> listC=null;
            synchronized (this){
                if(mListSolt!=null) {
                    listC = new LinkedList<>(mListSolt);
                }
            }
            return listC;
        }
    }

}
