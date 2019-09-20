package com.njzz.bases.common;

import com.njzz.bases.utils.Utils;

import java.io.File;

/**
 * 网络资源缓存类，通知为异步通知
 */
public class NetResCache  {

    public NetResCache(){
    }

    /**
     * 设置自定义的loader,load为加载网络资源的函数对象，默认为HttpDownloader
     * 有三个参数，String:下载链接  String:本地保存文件路径  Notify.Normal:下载完成通知对象，通知参数和HttpDownloader一样。
     * */
    Functor mLoader;
    public NetResCache(Functor loder){
        mLoader=loder;
    }

    /**
     * 获取网络缓存资源，资源有三种状态，不存在/下载中/已经存在
     * 如果不存在，该请求会触发下载操作
     * @param strUri 资源路径
     * @param listener 不存在或者下载中的回调
     * @return
     */
    public String getPath(String strUri, ResUtils.ResType resType, Notify.Normal listener){
        String resPath = ResUtils.getDiskPath(strUri, resType);
        if(Utils.emptystr(resPath)){//路径或者uri为空
            return null;
        }

        if(new File(resPath).exists())//资源存在，直接返回
            return resPath;

        load(strUri,resPath,listener);
        return null;
    }

    /**
     * 加载资源
     * @param strUri 资源路径
     * @param strDownload 保存的本地路径
     * @param listener 结果通知
     */
    private void load(final String strUri, String strDownload, Notify.Normal listener){
        //添加到下载队列
        if(mLoader==null) {
            HttpDownloader.add(strUri, strDownload, listener);
        }else{//使用自定义加载对象
            try {
                mLoader.call(strUri, strDownload, listener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
