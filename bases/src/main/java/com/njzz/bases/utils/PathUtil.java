package com.njzz.bases.utils;

import android.os.Environment;

import java.io.File;

public class PathUtil {
    private static final String PATH_ROOT =  Environment.getExternalStorageDirectory() + "/Android/data/com.librarys.tools";

    private static final String PATH_IMAGE = PATH_ROOT + "/download/images/";
    private static final String PATH_IMAGE_CACHE = PATH_ROOT + "/cache/images/";

    private static final String PATH_VIDEO = PATH_ROOT + "/download/videos/";
    private static final String PATH_VIDEO_CACHE = PATH_ROOT + "/cache/videos/";

    private static final String  PATH_VIEWO_THUMB = PATH_ROOT + "/cache/video_thumbnail/";


    public static final int IMAGE=1;//图片路径
    public static final int IMAGE_CACHE=2;//图片缓存路径
    public static final int VIDEO=3;//视频路径
    public static final int VIDEO_CACHE=4;//视频缓存路径

    public static final int VIDEO_THUNB=5;//缩略视频路径


    public static String get(final int w){
        String pathReturn=null;

        switch (w){
            case IMAGE:
                pathReturn= PATH_IMAGE;
                break;
            case IMAGE_CACHE:
                pathReturn= PATH_IMAGE_CACHE;
                break;
            case VIDEO:
                pathReturn= PATH_VIDEO;
                break;
            case VIDEO_CACHE:
                pathReturn=PATH_VIDEO_CACHE;
                break;
            case VIDEO_THUNB:
                pathReturn=PATH_VIEWO_THUMB;
                break;
        }

        if(pathReturn!=null) {
            File f = new File(pathReturn);
            if (!f.exists()) f.mkdir();
        }

        return pathReturn;
    }

    //判断给定的完整路径 string 是否是本地路径
    public static boolean isLocalFile(String strAbsPath){
        if(!Utils.emptystr(strAbsPath)){
            //格式  /xx/xx/xx 或者  file//
            return strAbsPath.charAt(0)=='/' || strAbsPath.toLowerCase().contains("file//");
        }
        return false;
    }

    //判断是否为httpFile
    public static boolean isHttpFile(String strPath){
        if(!Utils.emptystr(strPath)){
            return strPath.trim().toLowerCase().indexOf("http")==0;
        }
        return false;
    }
}
