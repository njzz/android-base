package com.njzz.bases.common;

import android.app.Activity;

import java.util.LinkedList;

//Activity 管理器
public class ActManager {

    static private ActManager gActMgr=null;
    static public ActManager ins(){
        if(gActMgr==null){
            synchronized (ActManager.class){
                if(gActMgr==null){
                    gActMgr=new ActManager();
                }
            }
        }
        return gActMgr;
    }
    private ActManager(){
        GlobalNotice.setListener(Notice.ANY, new SignalSlot.Slot(null) {
            @Override
            public void onSignal(int what, int arg1, int agr2, Object argObj) {
                if(what==Notice.ACTIVITY_CREATE)
                    addFirst((Activity) argObj);
                else if(what==Notice.ACTIVITY_DESTROY)
                    remove((Activity) argObj);
            }
        });
    }

    private final LinkedList<Activity> mCurrentAct=new LinkedList<>();
    public <T extends BaseActivity> T find(Class<?> clsName){
        if (clsName!=null){
            String strFind=clsName.getName();
            synchronized (this) {
                for (Activity activity : mCurrentAct) {
                    if (activity.getClass().getName().equals(strFind)) {
                        return (T)activity;
                    }
                }
            }
        }
        return null;
    }

    //获取最上层activity
    public <T extends Activity> T last(){
        synchronized (this){
            return (T)mCurrentAct.peekFirst();
        }
    }

    private void finish(Class<?> clsName){
        if (clsName!=null){
            String strFind=clsName.getName();
            Activity actFind;
            do {
                actFind=null;
                synchronized (this) {
                    for (Activity activity : mCurrentAct) {
                        if (activity.getClass().getName().equals(strFind)) {
                            actFind = activity;
                            break;
                        }
                    }
                }
                finish(actFind);
            }while(actFind!=null);
        }
    }

    private void finish(Activity activity){
        if (activity!=null){
            activity.finish();
        }
    }

    //入，自动调用
    private void addFirst(Activity activity){
        if(activity!=null) {
            synchronized (this){
                mCurrentAct.addFirst(activity);
            }
        }
    }

    //出，自动调用
    private boolean remove(Activity argObj){
        synchronized (this){
            return mCurrentAct.remove(argObj); //相对于 removeFirst，不会抛出异常
        }
    }
}
