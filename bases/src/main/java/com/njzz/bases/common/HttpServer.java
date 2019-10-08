package com.njzz.bases.common;

import com.njzz.bases.utils.LogUtils;
import com.njzz.bases.utils.Utils;

import java.util.Map;

public class HttpServer {
    private static final HttpRequests mHttpRequestor=new HttpRequests();
    private static String mServerUrl="";

    public interface Response{
        void OnConnectError();//连接错误
        void OnResult(int httpCode,String strResult);// 结果
    }

    static private Notify.Receiver getNotify(Response response){
            return new Notify.Receiver(null) {
                @Override
                public void OnNotify(int arg1, int arg2, Object argObj) {
                if(response!=null){
                    if(arg1==ErrorCode.CONNECT){
                        response.OnConnectError();
                        String strExt="";
                        if(argObj instanceof  String){
                            strExt=(String)argObj;
                        }
                        LogUtils.w( "Server Connect Error!" +strExt);
                    }else{
                        LogUtils.d( Utils.jsonFormat( (String)argObj) );
                        response.OnResult(arg1,(String)argObj);
                    }
                }
            }
        };
    }

    public static void setServer(String str){
        if(Utils.emptystr(str))
            mServerUrl="";
        else
            mServerUrl=str;
    }

    //设置固定参数
    public static void setGlobalParam(String strKey,Object params){
        mHttpRequestor.setFixedParam(strKey,params);
    }

    //发起get请求
    public static void Get(String strUri, Map<String,Object > param,final Response response){
        mHttpRequestor.Get(mServerUrl+strUri, param, getNotify(response));
    }
    //发起post请求
    public static void Post(String strUri, Map<String,Object > param,final Response response){
        mHttpRequestor.Post(mServerUrl+strUri, param, getNotify(response));
    }
}
