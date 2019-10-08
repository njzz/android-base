package com.njzz.bases.common;

import com.njzz.bases.utils.HttpUtils;
import com.njzz.bases.utils.LogUtils;
import com.njzz.bases.utils.PathUtil;
import com.njzz.bases.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequests extends ResCompetition {

    private Map<String,Object> mFixedParam;
    private Map<String,Object> mFixedHeader;
    private AsyncCaller asyncCaller;
    public HttpRequests(){
        asyncCaller = new AsyncCaller();
    }

    //设置固定参数
    public void setFixedParam(String strKey,Object params){
        if(mFixedParam==null)
            mFixedParam=new HashMap<>();

        if(!Utils.emptystr(strKey) && params!=null)
            mFixedParam.put(strKey,params);
    }

    //设置固定头
    public void setFixedHeader(String strKey,Object params){
        if(mFixedHeader==null)
            mFixedHeader=new HashMap<>();

        if(!Utils.emptystr(strKey) && params!=null)
            mFixedHeader.put(strKey,params);
    }

    public HttpRequests(int maxThread){
        asyncCaller = new AsyncCaller(maxThread,-1);
    }

    private static class DataU{
        private DataU(String url, String method, Map<String,Object> params, Notify.Receiver nn){
            strMethod=method;
            strUrl=url;
            this.params=params;
            this.nn=nn;
        }
        String strMethod;
        String strUrl;
        Map<String,Object> params;
        Notify.Receiver nn;
    }
    private boolean isGet(String strMethod){
        return strMethod!=null && strMethod.equals(HttpUtils.METHOD_GET);
    }

    private boolean isPost(String strMethod){
        return strMethod!=null && strMethod.equals(HttpUtils.METHOD_POST);
    }

    private Notify.Receiver mCallBack=new Notify.Receiver(null) {
        @Override
        public void OnNotify(int arg1, int arg2, Object argObj) {
            DataU dataSet = (DataU) argObj;
            OnRequest(dataSet);
        }
    };

    private void OnRequest(DataU dateSet) {
        String strData = dateSet.strUrl;
        int code = ErrorCode.CONNECT;
        try {
            //全局参数增加
            if(dateSet.params!=null)
                dateSet.params.putAll(mFixedParam);
            else
                dateSet.params=mFixedParam;

            String strUrlFix=dateSet.strUrl;
            if(isGet(dateSet.strMethod)){//get url 设置
                strUrlFix= HttpUtils.getUrlFix(dateSet.strUrl,dateSet.params);
            }

            URL url = new URL(strUrlFix);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(dateSet.strMethod);
            conn.setConnectTimeout(10000);
            conn.setDoInput(true);

            fixHeader(conn);//全局头

            if(isPost(dateSet.strMethod)){//post 数据处理
                HttpUtils.beginPost(conn,dateSet.params);
            }else if(isGet(dateSet.strMethod)){
                conn.connect();
            }

            code = conn.getResponseCode();

            //取得inputStream，并将流中的信息写入
            InputStream input ;
            if(code>=200 && code<300)
                input= conn.getInputStream();
            else
                input=conn.getErrorStream();

            StringBuilder bufString = new StringBuilder();
            byte[] buffer = new byte[4 * 1024];
            int byteRead;
            while ((byteRead = input.read(buffer)) != -1) {
                if (byteRead > 0) {
                    bufString.append(new String(buffer, 0, byteRead, "UTF-8"));
                }
            }

            strData = bufString.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Notify.Send(dateSet.nn,code,0,strData);

    }

    private void fixHeader(HttpURLConnection connection){
        if(mFixedHeader==null)
            return;
        for(String s:mFixedHeader.keySet()){
            Object param=mFixedHeader.get(s);
            if(param==null) continue;
            if(param instanceof String){//string
                connection.setRequestProperty(s,(String)param);
            }else if(param instanceof  Number){//数字
                connection.setRequestProperty(s,String.valueOf(param));
            }else  {
                LogUtils.e("param type unknow "+s+":"+param.getClass().getName());
            }
        }
    }

    //请求
    public boolean Post(String url,Map<String,Object> params, Notify.Receiver nn) {//请求主页数据 ,type =1
        if (PathUtil.isHttpFile(url)) {
            asyncCaller.AddTask(mCallBack, 0, 0, new DataU(url, HttpUtils.METHOD_POST,params,nn));
            return true;
        }
        return false;
    }

    public boolean Get(String url,Map<String,Object> params, Notify.Receiver nn) {//请求数据
        if (PathUtil.isHttpFile(url)) {
            asyncCaller.AddTask(mCallBack, 0, 0, new DataU(url,HttpUtils.METHOD_GET,params,nn));
            return true;
        }
        return false;
    }
}
