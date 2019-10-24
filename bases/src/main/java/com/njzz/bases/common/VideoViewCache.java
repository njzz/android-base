package com.njzz.bases.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import com.njzz.bases.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class VideoViewCache {
    private static final int IMG_SIZE_SET =3;
    /**
     * 从cache里面读取图片帧
     * @param strCacheFile cache 文件
     * @return
     */
    public static List<Bitmap> getFromCache( String strCacheFile){
        List<Bitmap> result =new ArrayList<>(IMG_SIZE_SET);
        if(!getFromCache(strCacheFile,result))
            result=null;
        return result;
    }

    /**
     * 从video 里面读取视频帧，并保存到缓存文件里
     * @param strVideoFile 视频文件
     * @param strCacheFile 保存的cache路径
     * @return
     */
    public static List<Bitmap> getFromVideo( String strVideoFile,String strCacheFile){
        return getFromVideo(strVideoFile,strCacheFile, IMG_SIZE_SET);
    }


    private static float caculateInSampleSize(int srcWid,int srcHei, int reqWidth, int reqHeight) {
        float inSampleSize = 1.0f;
        if (srcWid > reqWidth || srcHei > reqHeight) {
            float widthRadio = reqWidth*1.0f/srcWid  ;
            float heightRadio = reqHeight*1.0f/srcHei  ;
            inSampleSize = Math.min(widthRadio, heightRadio);
        }
        return inSampleSize;
    }

    private static List<Bitmap> getFromVideo(String strVideoPath,String  strCachePath,int count){
        if(count<=0 || !new File(strVideoPath).exists()) return null;
        List<Bitmap> result=new ArrayList<>();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(strVideoPath);
        }catch (Exception e){
            e.printStackTrace();
            new File(strVideoPath).delete();
            return null;
        }
        // 取得视频的长度(单位为毫秒)
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int totaltime=Integer.valueOf(time);
        int timeSep=totaltime/count;//
        if(timeSep>300) timeSep=300;
        //timeSep/=20;//
        //
        //int seconds = Integer.valueOf(time) / count;
        for(int i=0;i<count;++i) {
            Bitmap bitmap = retriever.getFrameAtTime((i+1)*timeSep*1000, MediaMetadataRetriever.OPTION_CLOSEST);//SYNC 检索的为关键帧，关键帧有问题
            if(bitmap!=null) {
                final int maxSet=400;
                int srcW=bitmap.getWidth(),srcH=bitmap.getHeight();
                if(srcW<maxSet && srcH<maxSet) {
                    result.add(bitmap);
                }else{
                    float fMin=caculateInSampleSize(srcW,srcH,maxSet,maxSet);
                    Bitmap bmpReq=Bitmap.createScaledBitmap(bitmap,Math.round(srcW*fMin),Math.round(srcH*fMin),true);
                    if(!bmpReq.equals(bitmap)){
                        bitmap.recycle();
                    }
                    result.add(bmpReq);
                }
            }
        }

        //save cache
        if(result.size()>0){
            saveToCache(strCachePath,result);
        }
        return result;
    }


    //读取保存的序列化图片
    private static boolean getFromCache(String strFilePath,List<Bitmap> result){
        if(strFilePath!=null) {
            File f=new File(strFilePath);
            if(f.exists()) {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
                    List<byte[]> lst=(List<byte[]>) in.readObject();
                    in.close();
                    toBitmap(lst,result);
                } catch (Exception e) {
                    e.printStackTrace();
                    f.delete();
                }
            }
        }
        return result.size()>0;
    }

    //将list<Bitmap> 序列化保存
    private static void saveToCache(String strFilePath,List<Bitmap> result){
        if(!Utils.emptystr(strFilePath)) {
            File f=new File(strFilePath);
            if(! f.exists() ) {
                try {
                    File ft=new File(strFilePath+".tmp");
                    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ft));
                    List<byte[]> lst=new ArrayList<>();
                    toBytes(result,lst);
                    out.writeObject(lst);
                    out.close();

                    ft.renameTo(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //list<byte[]> 转换到 list<bitmap>
    private static void toBitmap(List<byte[]> lbyte,List<Bitmap> lbitmap){
        for(byte [] b:lbyte){
            lbitmap.add(BitmapFactory.decodeByteArray(b,0,b.length));
        }
    }

    //list<bitmap> 转换到 list<byte[]>
    private static void toBytes(List<Bitmap> lbitmap,List<byte[]> lbyte){
        for(Bitmap b:lbitmap){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            lbyte.add(baos.toByteArray());
        }
    }
}
