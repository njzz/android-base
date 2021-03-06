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
    private HashMap<String, List<Receiver> > mExistMap=new HashMap<>();

    protected void onActivityLifeEnd(Activity activity){
        synchronized (this){
           for(String s:mExistMap.keySet()){
               List<Receiver> l=mExistMap.get(s);
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
    protected boolean resNotLoaded(String strKey, Receiver nn){
        boolean bRtValue=false;
        synchronized (this){
            List<Receiver> receivers = mExistMap.get(strKey);
            if(receivers ==null){//第一个
                receivers =new LinkedList<>();
                mExistMap.put(strKey, receivers);
                bRtValue=true;
            }
            if(nn!=null && receivers.indexOf(nn)==-1 ) {//添加到通知队列
                receivers.add(nn);
            }
        }
        return bRtValue;
    }

    /**
     * 设置本次资源加载完毕
     * @param strKey key 组
     * @param ms 透传给notify
     */
    protected void setResLoaded(String strKey,MessageSet ms){
        List<Receiver> receivers;
        synchronized (this){
            //查找对应key组
             receivers = mExistMap.remove(strKey);
        }

        if(receivers !=null){//存在，通知
            Notify.Send(receivers,ms);
        }
    }
}
