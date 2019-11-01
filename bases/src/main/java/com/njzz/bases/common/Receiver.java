package com.njzz.bases.common;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

public abstract class Receiver extends LifeAble {
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

    public void action(MessageSet ms){//直接调用
        if(mObserver!=null){//如果存在观察者
            Notify.Send(mObserver,ms);
        }
        finalAction(ms);
    }

    protected void finalAction(MessageSet ms){
        OnNotify(ms);
    }
    abstract protected void OnNotify(MessageSet ms);
}
