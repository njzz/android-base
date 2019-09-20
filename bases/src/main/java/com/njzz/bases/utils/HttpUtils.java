package com.njzz.bases.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
}
