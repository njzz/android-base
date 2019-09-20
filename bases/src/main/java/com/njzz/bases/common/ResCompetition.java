package com.njzz.bases.common;

import android.app.Activity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 资源竞争父类，多线程环境下，排除资源被同时处理
 * 注意：只能防止同时处理资源，处理时还是要判断是否之前被处理过
 */
public class ResCompetition extends LifeBasedClass{
    /**
     * 保存正在请求的资源
     */
    private HashMap<String, List<Notify.Normal> > mExistMap=new HashMap<>();

    protected void onActivityLifeEnd(Activity activity){
        synchronized (this){
           for(String s:mExistMap.keySet()){
               List<Notify.Normal> l=mExistMap.get(s);
               listProcess(l,activity);
           }
        }
    }

    /**
     * 查看资源是否正在被请求
     * @param strKey 资源key
     * @param nn 通知对象
     * @return 资源是否正在被请求
     */
    protected boolean isResLoad(String strKey, Notify.Normal nn){
        boolean bRtValue=true;
        synchronized (this){
            List<Notify.Normal> normals = mExistMap.get(strKey);
            if(normals==null){//第一个
                normals=new LinkedList<>();
                mExistMap.put(strKey,normals);
                bRtValue=false;
            }
            if(nn!=null) {//添加到通知队列
                normals.add(nn);
            }
        }
        return bRtValue;
    }

    /**
     * 设置本次资源加载完毕
     * @param strKey key 组
     * @param arg1 透传给notify
     * @param arg2 透传给notify
     * @param argObj 透传给notify
     */
    protected void setResLoaded(String strKey,int arg1,int arg2,Object argObj){
        List<Notify.Normal> normals=null;
        synchronized (this){
            //查找对应key组
             normals = mExistMap.remove(strKey);
        }

        if(normals!=null){//存在，通知
            Notify.AsyncSend(normals,arg1,arg2,argObj);
        }
    }
}
