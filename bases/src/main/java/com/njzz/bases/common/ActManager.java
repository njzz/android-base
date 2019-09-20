package com.njzz.bases.common;

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
        GlobalNotice.setListener(Notice.ANY, new SignalSlot.Slot() {
            @Override
            public void onSignal(int what, int arg1, int agr2, Object argObj) {
                if(what==Notice.ACTIVITY_CREATE)
                    push((BaseActivity) argObj);
                else if(what==Notice.ACTIVITY_DESTROY)
                    pop();
            }
        });
    }

    private final LinkedList<BaseActivity> mCurrentAct=new LinkedList<>();
    public <T extends BaseActivity> T find(Class<?> clsName){
        if (clsName!=null){
            String strFind=clsName.getName();
            synchronized (this) {
                for (BaseActivity activity : mCurrentAct) {
                    if (activity.getClass().getName().equals(strFind)) {
                        return (T)activity;
                    }
                }
            }
        }
        return null;
    }

    public <T extends BaseActivity> T current(){
        synchronized (this){
            return (T)mCurrentAct.peekFirst();
        }
    }

    private void push(BaseActivity activity){
        if(activity!=null) {
            synchronized (this){
                mCurrentAct.addFirst(activity);
            }
        }
    }

    private BaseActivity pop(){
        synchronized (this){
            return mCurrentAct.pollFirst(); //相对于 removeFirst，不会抛出异常
        }
    }
}
