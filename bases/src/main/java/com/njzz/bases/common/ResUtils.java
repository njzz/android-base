package com.njzz.bases.common;

import com.njzz.bases.utils.PathUtil;
import com.njzz.bases.utils.Utils;

import java.io.File;

public class ResUtils {
    public enum ResType{
        IMAGE,IMAGE_CACHE,VIDEO,VIDEO_CACHE,VIDEO_THUMB, IMG_VIDEO_THUMB
        //图片 缓存图片   视频    视频缓存  视频缩略缓存   视频缩略图帧缓存
        //缓存和非缓存的区别是会不会被清空
    }

    //定义资源扩展名
    private static final String EXT_IMAGE=".png";
    private static final String EXT_VIDEO=".mp4";
    private static final String EXT_IMG_VIDEO_VIEW=".ivv";//image video view

    //定义资源状态
    public static final int RES_NOT_EXIST=-1;//资源不存在
    public static final int RES_EXIST=0;//资源存在
    public static final int RES_LOADING=1;//资源加载中

    //获取资源扩展名
    private static String getExt(ResType type){
        switch (type){
            case IMAGE://图片
            case IMAGE_CACHE://图片缓存
                return EXT_IMAGE;
            case VIDEO://视频
            case VIDEO_THUMB://视频缩略图
            case VIDEO_CACHE://视频缓存
                return EXT_VIDEO;
            case IMG_VIDEO_THUMB://视频缩略图图片帧
                return EXT_IMG_VIDEO_VIEW;
        }

        assert (false);
        return "";
    }

    //获取资源路径
    public static String getTypePath(ResType type){
        int getID=0;
        switch (type){
            case IMAGE://图片路径
                getID=PathUtil.IMAGE;
                break;
            case IMAGE_CACHE://图片缓存
                getID=PathUtil.IMAGE_CACHE;
                break;
            case VIDEO://视频缓存
                getID=PathUtil.VIDEO;
                break;
            case VIDEO_CACHE:
                getID=PathUtil.VIDEO_CACHE;
                break;
            case VIDEO_THUMB://视频缩略图缓存
            case IMG_VIDEO_THUMB://视频缩略图图片帧缓存
                getID=PathUtil.VIDEO_THUNB;
                break;
                default:
                    assert false;
                    break;
        }

        return PathUtil.get(getID);
    }

    //判断资源是否是一个视频
    public static boolean isVideo(String strUri){
        return strUri!=null && strUri.contains(".mp4");
    }

    //获取一个资源id
    public static String getID(String strUri,ResType resType){
        String sID = Utils.md5(strUri);
        if(Utils.emptystr(sID)){
            assert (false);
            return null;
        }
        return sID+getExt(resType);
    }

    //获取资源路径
    public static String getDiskPath(String strUri,ResType resType){
        if(Utils.emptystr(strUri)){//空，直接返回null
            return null;
        }

        if(PathUtil.isLocalFile(strUri)){//本地路径
            if( !isVideo(strUri) || resType!=ResType.IMG_VIDEO_THUMB )//如果不是根据本地视频来获取缩略图，直接返回
                return strUri;
        }

        return getTypePath(resType)+getID(strUri,resType);
    }

    /**
     * 检查文件是否存在
     */
    public static boolean testFile(String strFile){
        return !Utils.emptystr(strFile) && new File(strFile).exists();
    }

    /**
     * 移除资源
     */
    public boolean remove(String strUri,ResType resType){
        String resPath = getDiskPath(strUri, resType);
        if(Utils.emptystr(resPath)){
            return false;
        }
        File fres = new File(resPath);
        if(fres.exists()){
            return fres.delete();
        }
        return true;
    }

}
