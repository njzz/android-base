package com.njzz.bases.common;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.njzz.bases.utils.BitmapUtils;
import com.njzz.bases.utils.LogUtils;
import com.njzz.bases.utils.Utils;

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
        Receiver mDecoder=new DecoderWrap();
        Receiver mDownloadNotify = new  Receiver(null) {
            @Override
            public void OnNotify(MessageSet ms) {
                //下载完成的直接去解码
                String strUri = Utils.cast(ms.argObj);
                if(HttpDownloader.isSuccess(ms.what)) {
                    //下载完成的直接去加载
                    addDecodeTask(strUri );
                }else{//下载失败的
                    LogUtils.e("缩略视频下载失败,code: "+ms.what+" url:"+strUri);
                    setResLoaded(strUri,new MessageSet( ms.what, strUri));//清理通知
                }
            }
        };

        private List<Bitmap> getFromCache(String strUri){
            String strCache=ResUtils.getDiskPath(strUri,ResUtils.ResType.IMG_VIDEO_THUMB);
            if(ResUtils.testFile(strCache)){//如果存在，直接加载缓存文件
                return VideoViewCache.getFromCache(strCache);
            }
            return null;
        }

        private class DecoderWrap extends Receiver {
            private DecoderWrap(){
                super(null);
            }
            //异步获取帧
            @Override
            public void OnNotify(MessageSet ms) {
                String strUri = Utils.cast(ms.argObj);

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
                        listCache= VideoViewCache.getFromVideo(strVideo, strCache);
                    }

                    if(listCache==null){//视频文件不存在，去下载
                        HttpDownloader.add(strUri, strVideo, mDownloadNotify);
                    }


                } finally {
                    if ( listCache!= null) {
                        GlobalCache.set(strUri,listCache);
                        setResLoaded(strUri,new MessageSet( ErrorCode.SUCCESS, strUri));
                    }
                }
            }
        }

        //去解视频帧
        private void addDecodeTask(String strUri){
            if(mAsyncCaller==null)
                mAsyncCaller=new AsyncCaller();
            mAsyncCaller.AddTask(mDecoder,new MessageSet(0,strUri));
        }

        private List<Bitmap> getVideoFrame(String strUri, Receiver nn){
            if(ResUtils.isVideo(strUri)){//视频

                //从缓存获取
                List<Bitmap> bmpList = GlobalCache.get(strUri);
                if(bmpList!=null){//该资源文件存在
                    return bmpList;
                }

                //如果没有正在加载，开始加载
                if(!Utils.emptystr(strUri) && resNotLoaded(strUri,nn)){
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
    static public List<Bitmap> getVideoFrame(String strUri, Receiver nn){
        return mVideoThumbCache.getVideoFrame(strUri,nn);
    }

    /**
     * 获取图片(缩略)
     * @param strUri 资源路径
     * @param nn 如果返回null，则会通知[arg1 为 httpdownloader 的 errcode ,argObj 为本地文件地址]
     * @return
     */
    static public Bitmap getBitmap(String strUri, Receiver nn){
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
    static private Receiver gDecoder=new Receiver(null) {
        @Override
        public void OnNotify(MessageSet ms) {
            String strUri=Utils.cast(ms.argObj);
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
    static public Bitmap getBitmapSrc(String strUri, Receiver nn){
        String resPath = ResUtils.getDiskPath(strUri, ResUtils.ResType.IMAGE_CACHE);
        if(ResUtils.testFile(resPath)){//该资源文件存在
            return BitmapUtils.bitmapFromFile(resPath);
        }

        HttpDownloader.add(strUri, resPath, nn);
        return null;
    }
}
