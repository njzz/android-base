package com.njzz.bases.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//函数对象封装类，如果是静态方法，obj可以为null
public class Functor {
    private Method mMethod;
    private Object mObjCall;
    public Functor(Object obj,Class<?> c,String funcTion,Class<?>... types) throws NoSuchMethodException{
        if(c!=null){
            mMethod=c.getDeclaredMethod(funcTion, types);
            mMethod.setAccessible(true);
            mObjCall=obj;
        }
    }
    public Object call(Object ...values) throws IllegalAccessException, InvocationTargetException {
        return mMethod.invoke(mObjCall,values);
    }
}
