package com.librarys.tools.common;

import com.njzz.bases.common.Receiver;
import com.njzz.bases.common.RecentDownloader;
import com.njzz.bases.utils.ResUtils;
import com.njzz.bases.utils.HttpUtils;
import com.njzz.bases.utils.Utils;

import java.util.ArrayList;
import java.util.List;

//最近资源获取器
public class RecentVideoMgr {

    static private RecentDownloader mRecentDownloader=new RecentDownloader(2);

    //获取视频
    static public String cacheVideo(List<String> strUri){
        List<HttpUtils.downloadParam> cacheList=new ArrayList<>(strUri.size());
        for(String stri:strUri) {
            if(!Utils.emptystr(stri)) {
                String resPath = ResUtils.getDiskPath(stri, ResUtils.ResType.VIDEO_CACHE);
                if (!ResUtils.testFile(resPath)) {//该资源文件存在
                    cacheList.add(new HttpUtils.downloadParam(stri, resPath));
                }
            }
        }
        if(cacheList.size()>0)
            mRecentDownloader.addTask(cacheList);
        return null;
    }

    //获取视频
    static public String getVideo(String strUri, Receiver nn){
        String resPath = ResUtils.getDiskPath(strUri, ResUtils.ResType.VIDEO_CACHE);
        if(ResUtils.testFile(resPath)){//该资源文件存在
            return resPath;
        }else {
            mRecentDownloader.addListener(strUri,nn);
            return null;
        }
    }
}
