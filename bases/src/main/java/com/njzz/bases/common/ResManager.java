package com.njzz.bases.common;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.njzz.bases.utils.BitmapUtils;
import com.njzz.bases.utils.LogUtils;
import com.njzz.bases.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
*资源管理器 ，负责资源的统一调度
 * */
public class ResManager {

    static private  VideoThumbCache mVideoThumbCache=new VideoThumbCache();
    /**
     * 视频预览图缓存
     */
    static private class VideoThumbCache extends ResCompetition {
        AsyncCaller mAsyncCaller;//多线程解
        Notify.Receiver mDecoder=new DecoderWrap();
        Notify.Receiver mDownloadNotify = new  Notify.Receiver(null) {
            @Override
            public void OnNotify(int arg1, int arg2, Object argObj) {
                //下载完成的直接去解码
                String strUri = Utils.cast(argObj);
                if(HttpDownloader.isSuccess(arg1)) {
                    //下载完成的直接去加载
                    addDecodeTask(strUri );
                }else{//下载失败的
                    LogUtils.e("缩略视频下载失败,code: "+arg1+" url:"+strUri);
                    setResLoaded(strUri, arg1, 0, strUri);//清理通知
                }
            }
        };

        private List<Bitmap> getFromCache(String strUri){
            String strCache=ResUtils.getDiskPath(strUri,ResUtils.ResType.IMG_VIDEO_THUMB);
            if(ResUtils.testFile(strCache)){//如果存在，直接加载缓存文件
                return VideoViewCache1.getFromCache(strCache);
            }
            return null;
        }

        private class DecoderWrap extends Notify.Receiver {
            private DecoderWrap(){
                super(null);
            }
            //异步获取帧
            @Override
            public void OnNotify(int arg1, int arg2, Object argObj) {
                String strUri = Utils.cast(argObj);

                List<Bitmap> listCache = null;
                try {
                    //检查是否已经被解出来了
                    listCache = getFromCache(strUri);
                    if ( listCache!= null) {
                        return;
                    }

                    //检查视频是否存在
                    String strVideo = ResUtils.getDiskPath(strUri, ResUtils.ResType.VIDEO_THUMB);
                    if(ResUtils.testFile(strVideo)){//该视频文件存在
                        String strCache = ResUtils.getDiskPath(strUri, ResUtils.ResType.IMG_VIDEO_THUMB);
                        listCache=VideoViewCache1.getFromVideo(strVideo, strCache);
                    }

                    if(listCache==null){//视频文件不存在，去下载
                        HttpDownloader.add(strUri, strVideo, mDownloadNotify);
                    }


                } finally {
                    if ( listCache!= null) {
                        GlobalCache.set(strUri,listCache);
                        setResLoaded(strUri, ErrorCode.SUCCESS, 0, strUri);
                    }
                }
            }
        }

        //去解视频帧
        private void addDecodeTask(String strUri){
            if(mAsyncCaller==null)
                mAsyncCaller=new AsyncCaller();
            mAsyncCaller.AddTask(mDecoder,0,0,strUri);
        }

        private List<Bitmap> getVideoFrame(String strUri, Notify.Receiver nn){
            if(ResUtils.isVideo(strUri)){//视频

                //从缓存获取
                List<Bitmap> bmpList = GlobalCache.get(strUri);
                if(bmpList!=null){//该资源文件存在
                    return bmpList;
                }

                //如果没有正在加载，开始加载
                if(!Utils.emptystr(strUri) && !isResLoad(strUri,nn)){
                    addDecodeTask(strUri);
                }

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
        Bitmap bmp = GlobalCache.get(strUri);
        if(bmp!=null){//该资源文件存在
            return bmp;
        }

        String resPath = ResUtils.getDiskPath(strUri, ResUtils.ResType.IMAGE_CACHE);
        if(nn==null){
            nn=gDecoder;
        }else{
            nn.addObserver(gDecoder);
        }
        HttpDownloader.add(strUri, resPath, nn);
        return null;
    }

    //异步解码图片
    @SuppressLint("StaticFieldLeak")
    static private Notify.Receiver gDecoder=new Notify.Receiver(null) {
        @Override
        public void OnNotify(int arg1, int arg2, Object argObj) {
            String strUri=Utils.cast(argObj);
            if(strUri!=null){
                String resPath = ResUtils.getDiskPath(strUri, ResUtils.ResType.IMAGE_CACHE);
                if(ResUtils.testFile(resPath)){//该资源文件存在
                    GlobalCache.set(strUri, BitmapUtils.bitmapFromFile(resPath) );
                }
            }
        }
    };

    /**
     * 获取图片(原始)
     * @param strUri 资源路径
     * @param nn 如果返回null，则会通知[arg1 为 httpdownloader 的 errcode ,argObj 为本地文件地址]
     * @return
     */
    static public Bitmap getBitmapSrc(String strUri, Notify.Receiver nn){
        String resPath = ResUtils.getDiskPath(strUri, ResUtils.ResType.IMAGE_CACHE);
        if(ResUtils.testFile(resPath)){//该资源文件存在
            return BitmapUtils.bitmapFromFile(resPath);
        }

        HttpDownloader.add(strUri, resPath, nn);
        return null;
    }
}
