package com.njzz.bases.common;

import android.graphics.Bitmap;

import com.njzz.bases.utils.BitmapUtils;
import com.njzz.bases.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
*资源管理器 ，负责资源的统一调度
 * */
public class ResManager {

    static private  VideoThumbCache mVideoThumbCache=new VideoThumbCache();
    static private  NetResCache mNetResCache=new NetResCache();

    /**
     * 视频预览图缓存
     */
    static private class VideoThumbCache extends ResCompetition{
        AsyncCaller mAsyncCaller;//多线程解

        private List<Bitmap> getFromCache(String strUri){
            String strCache=ResUtils.getDiskPath(strUri,ResUtils.ResType.IMG_VIDEO_THUMB);
            if(strCache!=null && new File(strCache).exists()){//如果存在，直接加载缓存文件
                return VideoViewCache1.getFromCache(strCache);
            }
            return null;
        }

        //异步获取帧
        private Notify.Receiver mCallBack=new Notify.Receiver(null) {
            @Override
            public void OnNotify(int arg1, int arg2, Object argObj) {
                String strUri = (String) argObj;
                String strVideo = ResUtils.getDiskPath(strUri, ResUtils.ResType.VIDEO_THUMB);

                try {
                    //检查是否已经被解出来了
                    if (getFromCache(strUri) != null) {
                        return;
                    }
                    String strCache = ResUtils.getDiskPath(strUri, ResUtils.ResType.IMG_VIDEO_THUMB);
                    VideoViewCache1.getFromVideo(strVideo, strCache);
                } finally {
                    setResLoaded(strUri, 0, 0, strVideo);
                }
            }
        };

        //去解视频帧
        private void addDecodeTask(String strUri, Notify.Receiver nn){
            if(!Utils.emptystr(strUri) && !isResLoad(strUri,nn) ){
               if(mAsyncCaller==null)
                   mAsyncCaller=new AsyncCaller();
                mAsyncCaller.AddTask(mCallBack,0,0,strUri);
            }
        }

        private List<Bitmap> getVideoFrame(String strUri, Notify.Receiver nn){
            if(ResUtils.isVideo(strUri)){//视频
                //优先检查缓存的图片帧
                List<Bitmap> caches=getFromCache(strUri);
                if(caches!=null)
                    return caches;

                //下面要在异步队列里处理，不然可能和下载完毕的视频在不同线程同时解。提取视频帧本身也是耗时操作
                //不存在，加载视频文件
                String strVideoFile=mNetResCache.getPath(strUri, ResUtils.ResType.VIDEO_THUMB, new Notify.Receiver(null) {
                    @Override
                    public void OnNotify(int arg1, int arg2, Object argObj) {
                    if(arg1== ErrorCode.SUCCESS || arg1== ErrorCode.EXIST)//下载完成，去解帧
                        addDecodeTask(strUri,nn);//添加到异步队列
                }});

                if(!Utils.emptystr(strVideoFile))//如果视频缓存文件存在，但缓存缩略图不存在
                    addDecodeTask(strUri,nn);
            }else{//图片
                Bitmap bmp = getBitmap(strUri, nn);
                if (bmp != null) {
                    List<Bitmap> l = new ArrayList<>(1);
                    l.add(bmp);
                    return l;
                }
            }

            return null;
        }
    }


    /**
     * 获取视频帧
     * @param strUri 资源路径，可以是图片
     * @param nn 如果返回null，则会通知[arg1 为 httpdownloader 的 errcode,argObj 为本地文件地址]
     * @return
     */
    static public List<Bitmap> getVideoFrame(String strUri, Notify.Receiver nn){
        return mVideoThumbCache.getVideoFrame(strUri,nn);
    }

    /**
     * 获取图片(缩略)
     * @param strUri 资源路径
     * @param nn 如果返回null，则会通知[arg1 为 httpdownloader 的 errcode ,argObj 为本地文件地址]
     * @return
     */
    static public Bitmap getBitmap(String strUri, Notify.Receiver nn){
        String str=mNetResCache.getPath(strUri, ResUtils.ResType.IMAGE_CACHE,nn);
        if(Utils.emptystr(str))//如果不存在
            return null;

        return BitmapUtils.bitmapFromFile(str);//如果存在
    }

    /**
     * 获取图片(原始)
     * @param strUri 资源路径
     * @param nn 如果返回null，则会通知[arg1 为 httpdownloader 的 errcode ,argObj 为本地文件地址]
     * @return
     */
    static public Bitmap getBitmapSrc(String strUri, Notify.Receiver nn){
        String str=mNetResCache.getPath(strUri, ResUtils.ResType.IMAGE_CACHE,nn);
        if(Utils.emptystr(str))//如果不存在
            return null;

        return BitmapUtils.bitmapFromFile(str,false);//如果存在
    }
}
