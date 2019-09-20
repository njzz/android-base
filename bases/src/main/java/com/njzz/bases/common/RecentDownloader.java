package com.njzz.bases.common;

import com.njzz.bases.utils.LogUtils;
import com.njzz.bases.utils.PathUtil;
import com.njzz.bases.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 最近下载模式，优先下载最近添加的资源
 */
public class RecentDownloader extends ResCompetition implements Notify.Normal {
    private int mDownloadVersion=1;

    private int beginDownload(String strUrl,String strFile) {
        RandomAccessFile output=null;
        int downLoadVersion=mDownloadVersion;
        try {
            File file=new File(strFile);
            if(file.exists())  {//本地文件存在
                if(file.length()==0) {//文件大小为0，错误文件删除
                    file.delete();
                }else {
                    return ErrorCode.EXIST;
                }
            }

            if(!PathUtil.isHttpFile(strUrl)){//如果下载目标是本地文件
                file=new File(strUrl);
                if(file.exists())  return ErrorCode.EXIST;//返回已经存在
                return ErrorCode.CONNECT;//不存在直接报错
            }

            URL url=new URL(strUrl);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(10000);

            //建立临时文件
            File fileDownload=new File(strFile+".rloading");
            fileDownload.getParentFile().mkdirs();//新建文件夹
            long fileExistLen=0;
            if(fileDownload.exists()) {
                fileExistLen=fileDownload.length();
                conn.setRequestProperty("Range", "bytes=" + fileExistLen + "-");
            }
            conn.connect();
            LogUtils.d("++++Thread:"+Thread.currentThread().getId()+" download file:"+strUrl);
            int code = conn.getResponseCode();//获取服务器返回的状态码

            if ( code !=200 && code!=206 ) { // 200 ,206
                return ErrorCode.SERVER;
            }

            output = new RandomAccessFile (fileDownload,"rw");
            if(code==206 && fileExistLen > 0){//如果206 续传
                output.seek(fileExistLen);
            }

            //取得inputStream，并将流中的信息写入
            InputStream input = conn.getInputStream();
            //读取大文件
            byte[] buffer = new byte[4 * 1024];
            int byteRead;
            while ((byteRead = input.read(buffer)) != -1) {
                if (byteRead > 0)
                    output.write(buffer, 0, byteRead);

                if(mDownloadVersion!=downLoadVersion) {
                    LogUtils.d("----download cancled:"+strUrl);
                    return ErrorCode.CANCLED;
                }
            }
            output.close();
            output=null;
            if(!fileDownload.renameTo(file))
                return ErrorCode.IO;

        } catch (IOException e) {
            e.printStackTrace();
            return ErrorCode.IO;
        }finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ErrorCode.SUCCESS;
    }

    private AsyncCaller asyncCaller;
    private class Datas{
        Datas(String s, String f){
            urlStr=s;
            pathFile=f;
        }
        private String urlStr;
        private String pathFile;
    }

    public RecentDownloader(int maxBothTask,int maxCacheTask){
        asyncCaller=new AsyncCaller(maxBothTask,maxCacheTask);
        asyncCaller.setRemoveListener(new Notify.Normal() {
            @Override
            public void OnNotify(int arg1, int arg2, Object argObj) {
                Datas curTask=(Datas)argObj;
                //取消资源
                setResLoaded(curTask.urlStr,ErrorCode.CANCLED,0,curTask.pathFile);
            }
        });
    }

    @Override
    public void OnNotify(int arg1, int arg2, Object argObj){
        Datas curTask=(Datas)argObj;
        int code=beginDownload(curTask.urlStr,curTask.pathFile);
        setResLoaded(curTask.urlStr,code,0,curTask.pathFile);
    }

    private void addTask(String urlStr, final String path, Notify.Normal nn) {
        if(!Utils.emptystr(urlStr) && !isResLoad(urlStr,nn)) {
            LogUtils.d("add recent download task:"+path);
            ++mDownloadVersion;
            asyncCaller.AddTask(this, 0, 0, new Datas(urlStr, path),0);
        }
    }
}
