package com.njzz.bases.common;

/*
* 全局发布器
* 用于业务解耦
*/

public class GlobalNotice {

    //全局通知器
    private static SignalSlot.Signaler gSignalGlobal=new SignalSlot.Signaler();

    //设置接收器
    public static boolean setListener(int care,SignalSlot.Slot slot){
        return gSignalGlobal.addSolt(care,slot);
    }
    //手动移除接收器
    public static void removeListener(SignalSlot.Slot slot){
        gSignalGlobal.removeSlot(slot);
    }
    //发送通知
    public static void Notice(int what){
        Notice(what,0);
    }
    //发送通知
    public static void Notice(int what,int arg1){
        Notice(what,arg1,0);
    }
    //发送通知
    public static void Notice(int what,int arg1,int arg2){
        Notice(what,arg1,arg2,null);
    }
    //发送通知
    public static void Notice(int what,Object argObj){
        Notice(what,0,0,argObj);
    }
    //发送通知
    public static void Notice(int what,int arg1,int arg2,Object argObj){
        gSignalGlobal.signal(what,arg1,arg2,argObj);
    }
}
