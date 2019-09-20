package com.njzz.bases.common;

import android.util.SparseLongArray;

public class MulitLock {
    public static int TIME_FOREVER = -1;
    private SparseLongArray mLockState=new SparseLongArray();

    //设置状态，如果之前的状态未old
    private boolean setLock(int lockid){
        synchronized (this){
            long id =  mLockState.get(lockid);
            if(id == 0 || id==Thread.currentThread().getId() ){//可以重复进入
                mLockState.put(lockid,Thread.currentThread().getId());
                return true;
            }
        }
        return false;
    }

    //锁定
    public void lock(int lockid){
        lockWait(lockid,TIME_FOREVER);
    }
    //超时之前尝试锁定，锁定返回true
    public boolean tryLock(int lockid,int timeOut){
        return lockWait(lockid,timeOut);
    }
    //解除锁定
    public boolean unlock(int lockid){
        synchronized (this){
            if( mLockState.get(lockid)!=0 ){
                mLockState.delete(lockid);
                return true;
            }
        }
        return false;
    }

    //设置状态，超时之前一直尝试设置
    public boolean lockWait(int lockid,long timeOut){
        long tEnter=System.currentTimeMillis();
        do{
            if(setLock(lockid))
                return true;
            try{
                Thread.sleep(1);
            }catch (Exception ignored){
            }
        }while(timeOut==TIME_FOREVER || (timeOut>0 && System.currentTimeMillis()-tEnter<timeOut) );//timeout=0 ,只调用一次

        return false;
    }
}
