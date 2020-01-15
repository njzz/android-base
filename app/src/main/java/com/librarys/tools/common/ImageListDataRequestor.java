package com.librarys.tools.common;

import com.njzz.bases.common.HttpServer;
import com.njzz.bases.common.MapObject;
import com.njzz.bases.common.MessageSet;
import com.njzz.bases.common.Notify;
import com.njzz.bases.common.Receiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ImageListDataRequestor implements HttpServer.Response {//implements HttpRequests.Response{//-----------------------------

    public class Result{
        public MapObject outData;
        public List<MapObject> dataSet;
    }

    private String [] mOutWatcher;
    private String [] mDataWatcher;//list resulr
    private int mErrorCode=0;
    private Receiver mNN;

    //通知 ，外层map对象，data Map对象
    public ImageListDataRequestor(String [] outWatch, String [] dataWatch, Receiver nn){
        mOutWatcher=outWatch;
        mDataWatcher=dataWatch;
        mNN=nn;
    }

    public void Post(String strUri,Map<String,Object> param){//-------------------------
        mErrorCode=0;
        HttpServer.Post(strUri,param,this);
    }

    public void Get(String strUri,Map<String,Object> param){//-------------------------
        mErrorCode=0;
        HttpServer.Get(strUri,param,this);
    }

    @Override
    public void OnResult(int httpCode,String strResult){
        mErrorCode=httpCode;
        onResult(strResult);
    }
    @Override
    public void OnConnectError(){
        mErrorCode=-1;
    }


//    @Override //http test----------------------------
//    public void onResult(int code,String strResult,Object param){
//        onResult(strResult);
//    }

    public void onResult(String strResult){
        Result re=new Result();
        if( strResult !=null && mErrorCode==200 ){
            OnDataParse(strResult,re);
        }
        Notify.Send(mNN,new MessageSet(mErrorCode,re));
    }

    private void OnDataParse(String strInfo, Result dataResult){
        try {
            JSONObject objStart = new JSONObject(strInfo);
            if(mOutWatcher!=null) {
                dataResult.outData=new MapObject();
                for (String key : mOutWatcher) {
                    if(objStart.has(key)){
                        dataResult.outData.put(key,objStart.get(key));
                    }
                }
            }

            if(mDataWatcher!=null &&  objStart.has("data") ){
                dataResult.dataSet=new ArrayList<>();
                JSONArray datas=objStart.getJSONArray("data");

                for (int j = 0,count=datas.length(); j < count; j++) {
                    JSONObject info = datas.getJSONObject(j);//当前内容

                    MapObject ds=new MapObject();
                    for (String key : mDataWatcher) {
                        if(info.has(key)){
                            ds.put(key,info.get(key));
                        }
                    }
                    dataResult.dataSet.add(ds);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
