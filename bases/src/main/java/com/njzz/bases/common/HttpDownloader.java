package com.njzz.bases.common;
import com.njzz.bases.utils.HttpUtils;
import com.njzz.bases.utils.Utils;

public class HttpDownloader extends ResCompetition{

    private AsyncCaller asyncCaller=new AsyncCaller();
    private Receiver mThreadBack=new Receiver(null){
        @Override
        public void OnNotify ( MessageSet ms){
            HttpUtils.downloadParam curTask = (HttpUtils.downloadParam) ms.argObj;
            int code = HttpUtils.download(  curTask);
            setResLoaded(curTask.getUrl(),new MessageSet( code, curTask.getUrl()));
        }
    };

    private void addTask(String urlStr, final String path, Receiver nn) {
        if(!Utils.emptystr(urlStr)) {
            if(resNotLoaded(urlStr,nn))
                asyncCaller.AddTask(mThreadBack, new MessageSet(0, new HttpUtils.downloadParam(urlStr, path) ));
        }else {
            Notify.Send(nn,new MessageSet(ErrorCode.CONNECT));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    static private HttpDownloader httpDownloader=new HttpDownloader();
    /**
     * 通用 http 下载接口
     * @param strUrl 下载资源的url
     * @param strPath 保存的本地路径
     * @param nn 接收器 [arg1 为 errorcode,argObj 为url地址]
     */
    public static void add(String strUrl, String strPath, Receiver nn){
        //LogUtils.d("add download task:"+strUrl);
        httpDownloader.addTask(strUrl,strPath,nn);
    }

    //测试是否成功
    public  static boolean isSuccess(int rtCode){
        return rtCode==ErrorCode.SUCCESS || rtCode==ErrorCode.EXIST;
    }
}
