package com.njzz.bases.common;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GlobalTransfer {
    private static final Lock lock=new ReentrantLock();
    private static final HashMap<String,Object> maps=new HashMap<>();
    public static <T extends Object> T get(String key){
        Object t;
        lock.lock();
        t=maps.get(key);
        lock.unlock();
        return (T)t;
    }

    public static void set(String key,Object v){
        lock.lock();
        maps.put(key,v);
        lock.unlock();
    }

    public static <T extends Object> T del(String key){
        Object o;
        lock.lock();
        o=maps.remove(key);
        lock.unlock();
        return (T)o;
    }
}
