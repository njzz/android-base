package com.njzz.bases.common;


import com.njzz.bases.utils.Utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GlobalCache {//全局缓存
    private static class SoftRef extends SoftReference< Object>{
        private String mkey;
        public SoftRef(String strKey,Object obj, ReferenceQueue< Object> q) {
            super(obj, q);
            mkey = strKey;
        }
    }
    private static final Lock lock=new ReentrantLock();
    private static final HashMap<String, SoftRef> maps=new LinkedHashMap<>();
    private static final ReferenceQueue<Object> refQueue=new ReferenceQueue<>();//引用队列，当引用对象被gc时，会放到该队列

    //获取缓存的对象
    @SuppressWarnings("unchecked")
    public static <T extends Object> T get(String key){
        SoftRef storage;
        lock.lock();
        check();
        storage=maps.get(key);
        lock.unlock();
        T rt=null;
        if(storage!=null){
            rt= Utils.cast(storage.get());
        }
        return rt;
    }

    public static void set(String key,Object v){
        if(v!=null) {
            lock.lock();
            maps.put(key, new SoftRef(key,v,refQueue));
            check();
            lock.unlock();
        }
    }

    public static Object del(String key){
        SoftRef o;
        lock.lock();
        o=maps.remove(key);
        lock.unlock();

        if(o!=null){
            return o.get();
        }

        return null;
    }

    //在lock里调用该函数
    private static void check(){
        SoftRef sv;
        while ((sv = (SoftRef) refQueue.poll()) != null) {
            maps.remove(sv.mkey);//清除SoftRef 本身
        }
    }
}
