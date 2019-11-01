package com.njzz.bases.utils;

import com.njzz.bases.common.ErrorCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HttpUtils {
    public static final String METHOD_GET="GET";
    public static final String METHOD_POST="POST";

    public static String getUrlFix(String strUrl,Map<String,Object> param){
        if(param!=null){
            String s = formatGetParam(param);
            if(!Utils.emptystr(s)) {
                if (hasParam(strUrl)) {
                    return strUrl + "&" + s;
                } else {
                    return strUrl + "?" + s;
                }
            }
        }
        return strUrl;
    }
    //返回url是否带参数
    public static boolean hasParam(String url){
        return url!=null && url.contains("?");
    }

    //格式化 get 参数
    public static String formatGetParam(Map<String,Object> param){
        StringBuilder bufString = new StringBuilder(1024);
        if(param!=null) {//get 不能传
            for (String s : param.keySet()) {
                Object parSet = param.get(s);
                if (parSet == null || s == null || s.isEmpty()) continue;


                if (parSet instanceof String || parSet instanceof Number) {//只处理数字和字符串
                    if (bufString.length() != 0)
                        bufString.append('&');

                    bufString.append(s);
                    bufString.append('=');
                    bufString.append(  parSet );//URLEncoder.encode(parSet,"UTF-8")
                }
            }
        }
        return bufString.toString();
    }

    //post 定义
    private final static String BOUNDARY = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");// 边界标识
    private final static String PREFIX = "--";// 必须存在
    private final static String LINE_END = "\r\n";
    public static void beginPost(HttpURLConnection conn,Map<String,Object> param){
        if(conn!=null && param!=null && param.size()>0){

            //参数分离
            Map<String,String> normalParam=null;
            Map<String, File> fileParam=null;
            for(String s:param.keySet()){
                if(!Utils.emptystr(s)) {
                    Object p = param.get(s);
                    if (p instanceof String || p instanceof Number) {
                        if (normalParam == null)
                            normalParam = new HashMap<>();
                        normalParam.put(s, String.valueOf(p));
                    } else if (p instanceof File) {
                        if(fileParam==null)
                            fileParam=new HashMap<>();
                        fileParam.put(s,(File)p);
                    }
                }
            }

            try {
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                conn.connect();

                OutputStream os = conn.getOutputStream();
                conn.setChunkedStreamingMode(1024*1024);
                writePostParams(normalParam,os);
                writePostFile(fileParam,os);
                String endTarget = PREFIX + BOUNDARY + PREFIX + LINE_END;
                os.write(endTarget.getBytes());
                os.flush();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 对post参数进行编码处理并写入数据流中
     *
     * @throws Exception
     * @throws IOException
     */
    private static void writePostParams(Map<String, String> requestText,
                                    OutputStream os) throws Exception {
        try {
            String msg = "post请求参数部分:\n";
            if (requestText == null || requestText.isEmpty()) {
                msg += "空";
            } else {
                StringBuilder requestParams = new StringBuilder();
                Set<Map.Entry<String, String>> set = requestText.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(LINE_END);
                    requestParams.append("Content-Type: text/plain; charset=utf-8").append(LINE_END);
                    //requestParams.append("Content-Transfer-Encoding: 8bit").append(LINE_END);
                    requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容
                    requestParams.append(entry.getValue());
                    requestParams.append(LINE_END);
                }
                os.write(requestParams.toString().getBytes());
                os.flush();

                msg += requestParams.toString();
            }

            LogUtils.d(msg);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * 对post上传的文件进行编码处理并写入数据流中
     *
     * @throws IOException
     */
    private static void writePostFile(Map<String, File> requestFile,
                                  OutputStream os) throws Exception {
        InputStream is = null;
        try {
            String msg = "请求上传文件部分:\n";
            if (requestFile == null || requestFile.isEmpty()) {
                msg += "空";
            } else {
                StringBuilder requestParams = new StringBuilder();
                Set<Map.Entry<String, File>> set = requestFile.entrySet();
                Iterator<Map.Entry<String, File>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<String, File> entry = it.next();
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"; filename=\"")
                            .append(entry.getValue().getName()).append("\"").append(LINE_END);
                    requestParams.append("Content-Type:").append(getContentType(entry.getValue())).append(LINE_END);
                    //requestParams.append("Content-Transfer-Encoding: 8bit").append(LINE_END);
                    requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容

                    os.write(requestParams.toString().getBytes());

                    is = new FileInputStream(entry.getValue());

                    byte[] buffer = new byte[4 * 1024];
                    int len ;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    os.write(LINE_END.getBytes());
                    os.flush();

                    msg += requestParams.toString();
                }
            }
            LogUtils.d(msg);
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
    }

    private static String getContentType(File file) {
        String strContentType = "application/octet-stream";
        int i = file.getName().indexOf('.');
        String strExt = null;
        if (i != -1) {
            strExt = file.getName().substring(i + 1).toLowerCase();
        }
        if (strExt != null && (strExt.equals("jpeg") || strExt.equals("jpg") || strExt.equals("png"))) {
            strContentType = "image/" + strExt;
        }
        return strContentType;
    }

    ////////////////////////////////////////////////////////下载
    public static final class downloadParam{
        public downloadParam(String strUrl,String strLocalFile){
            this(strUrl,strLocalFile,10000);
        }
        public downloadParam(String strUrl,String strLocalFile,int connectTimeOut){
            this.strUrl=strUrl;
            this.strLocalFile=strLocalFile;
            this.timeOut=connectTimeOut;
            cancel=false;
        }
        //取消下载
        public void setCancel(boolean bSet){
            cancel=bSet;
        }
        public boolean isCancel(){return cancel;}
        public String getUrl(){return strUrl;}
        public String getLocalFile() {return strLocalFile;}

        private String strUrl;
        private String strLocalFile;
        private int timeOut;
        private boolean cancel;
        //进度
    }
    public static int download(downloadParam dp){
        if(dp==null || Utils.emptystr(dp.strUrl) || Utils.emptystr(dp.strLocalFile) )
            return ErrorCode.PARAM;
        if(dp.cancel)
            return ErrorCode.CANCELED;
        RandomAccessFile output=null;
        try {
            File file=new File(dp.strLocalFile);
            if(file.exists())  {//本地文件存在
                if(file.length()==0) {//文件大小为0，错误文件删除
                    file.delete();
                }else {
                    return ErrorCode.EXIST;
                }
            }

            if(!PathUtil.isHttpFile(dp.strUrl)){//如果下载目标是本地文件
                file=new File(dp.strUrl);
                if(file.exists())  return ErrorCode.EXIST;//返回已经存在
                return ErrorCode.CONNECT;//不存在直接报错
            }

            URL url=new URL(dp.strUrl);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            if(dp.timeOut>0)
                conn.setConnectTimeout(dp.timeOut);

            //建立临时文件
            File fileDownload=new File(dp.strLocalFile+".loading");
            fileDownload.getParentFile().mkdirs();//新建文件夹
            long fileExistLen=0;
            if(fileDownload.exists()) {
                fileExistLen=fileDownload.length();
                conn.setRequestProperty("Range", "bytes=" + fileExistLen + "-");
            }
            conn.connect();

            int code = conn.getResponseCode();//获取服务器返回的状态码
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

                    if(dp.cancel){
                        LogUtils.w("download canceled:"+dp.strUrl);
                        return ErrorCode.CANCELED;
                    }
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
}
