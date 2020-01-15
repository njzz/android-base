package com.librarys.tools.constant;

public class ListKey {
    public static final String VIDEO_ID="wpID";//视频id
    public static final String VIDEO_TYPE="type";//视频分类
    public static final String VIDEO_IMG="imgUrl";//视频图片
    public static final String VIDEO_URL="videoUrl";//视频地址
    public static final String VIDEO_AUTHOR="upUser";//上传作者
    public static final String VIDEO_AUTHOR_HEAD="upHeadurl";//作者头像
    public static final String VIDEO_COUNT_DOWNLOAD="download";//下载次数
    public static final String VIDEO_COUNT_FAVORITE="collect";//收藏次数
    public static final String VIDEO_COUNT_SHARE="share";//分享次数
    public static final String VIDEO_ME_FAVORITE="bcollect";//是否被我收藏
    public static final String VIDEO_IMG_SMALL="thumbnailUrl";//缩略图
    public static final String VIDEO_HOT="hot";//热度
    public static final String VIDEO_STATUS="status";//状态


    /////////////////////////通用视频列表定义
    public static String[] strVideoAllParam;
    static public String [] videoAll(){
        if(strVideoAllParam==null){
            strVideoAllParam=new String []{VIDEO_ID,VIDEO_TYPE,VIDEO_IMG,VIDEO_URL,VIDEO_AUTHOR,VIDEO_AUTHOR_HEAD,VIDEO_COUNT_DOWNLOAD,
                    VIDEO_COUNT_FAVORITE,VIDEO_COUNT_SHARE,VIDEO_ME_FAVORITE,VIDEO_IMG_SMALL,VIDEO_HOT,VIDEO_STATUS};
        }
        return strVideoAllParam;
    }


    ////////////////////////我的，外字段定义
    public static final String OUT_PROJECT="work";//作品
    public static final String OUT_FAVORITE="collect";//收藏
    public static final String OUT_REMMEND="recommond";//推荐
    public static final String OUT_NICK="nickname";//昵称

    private static String[] strOutAllParam;
    static public String [] outAll(){
        if(strOutAllParam==null){
            strOutAllParam=new String []{OUT_PROJECT,OUT_FAVORITE,OUT_REMMEND,OUT_NICK};
        }
        return strOutAllParam;
    }


    //////////////广告字段定义
    public static final String AD_NAME="name";//广告字段
    public static final String AD_IMAGE="imgurl";//广告字段
    public static final String AD_URL="url";//广告字段
    private static String[] strAdAllParam;
    static public String [] adAll(){
        if(strAdAllParam==null){
            strAdAllParam=new String []{AD_NAME,AD_IMAGE,AD_URL};
        }
        return strAdAllParam;
    }

    ///////////////热门标签定义
    public static final String HOT_ID="id";//广告字段
    public static final String HOT_NAME="name";//广告字段
    public static final String HOT_IMAGE="img";//广告字段
    private static String[] strHotAllParam;
    static public String [] hotAll(){
        if(strHotAllParam==null){
            strHotAllParam=new String []{HOT_ID,HOT_NAME,HOT_IMAGE};
        }
        return strHotAllParam;
    }
}
