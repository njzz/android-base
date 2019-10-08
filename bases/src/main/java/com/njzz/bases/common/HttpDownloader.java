package com.njzz.bases.common;
import com.njzz.bases.utils.PathUtil;
import com.njzz.bases.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownloader extends ResCompetition{

    private int beginDownload(String strUrl,String strFile) {
        RandomAccessFile output=null;
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
            File fileDownload=new File(strFile+".loading");
            fileDownload.getParentFile().mkdirs();//新建文件夹
            long fileExistLen=0;
            if(fileDownload.exists()) {
                fileExistLen=fileDownload.length();
                conn.setRequestProperty("Range", "bytes=" + fileExistLen + "-");
            }
            conn.connect();

            //LogUtils.d("Thread:"+Thread.currentThread().getId()+" download file:"+strUrl);

            int code = conn.getResponseCode();//获取服务器返回的状态码
//            if(code==206){
//                LogUtils.d("file is 206:"+strUrl);
//            }
            if ( code !=200 && code!=206 && code!=506) { // 200 ,206,506
                return ErrorCode.SERVER;
            }

            if(code!=506) {//如果服务器返回506，表示范围错误(一般是存在临时文件，且临时文件和服务器文件一样大)
                output = new RandomAccessFile(fileDownload, "rw");
                if (code == 206 && fileExistLen > 0) {//如果206 续传
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
                }
                output.close();
                output = null;
            }
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

    private AsyncCaller asyncCaller=new AsyncCaller();
    private class Datas{
        Datas(String s, String f){
            urlStr=s;
            pathFile=f;
        }
        private String urlStr;
        private String pathFile;
    }

    private Notify.Receiver mThreadBack=new Notify.Receiver(null){
        @Override
        public void OnNotify ( int arg1, int arg2, Object argObj){
            Datas curTask = (Datas) argObj;
            int code = beginDownload(curTask.urlStr, curTask.pathFile);
            setResLoaded(curTask.urlStr, code, 0, curTask.pathFile);
        }
    };

    private void addTask(String urlStr, final String path, Notify.Receiver nn) {
        if(!Utils.emptystr(urlStr) && !isResLoad(urlStr,nn)) {
            asyncCaller.AddTask(mThreadBack, 0, 0, new Datas(urlStr, path));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    static private HttpDownloader httpDownloader=new HttpDownloader();
    /**
     * 通用 http 下载接口
     * @param strUrl 下载资源的url
     * @param strPath 保存的本地路径
     * @param nn 通知器 [arg1 为 errorcode,argObj 为本地文件地址]
     */
    public static void add(String strUrl, String strPath, Notify.Receiver nn){
        //LogUtils.d("add download task:"+strUrl);
        httpDownloader.addTask(strUrl,strPath,nn);
    }
}
