package com.njzz.bases.common;

import com.njzz.bases.utils.HttpUtils;
import com.njzz.bases.utils.LogUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 最近下载模式，只下载当前添加的资源，之前添加的自动取消
 */
public class RecentDownloader extends ResCompetition{

    private List<HttpUtils.downloadParam> mWaitTasks;//等待的任务
    private List<HttpUtils.downloadParam> mDownedTasks=new LinkedList<>();//正在下载的任务
    private AsyncCaller asyncCaller;//线程队列
    private static final int  DOWNLOAD_SET=1;

    public RecentDownloader(int maxBothTask){
        asyncCaller=new AsyncCaller(maxBothTask,-1);
    }

    private Receiver mCallBack=new Receiver(null) {
        @Override
        public void OnNotify(MessageSet ms) {
            HttpUtils.downloadParam curTask = getTask();
            LogUtils.i("download:"+curTask.getUrl());
            int code = HttpUtils.download(curTask);
            finishTask(code,curTask);
        }
    };

    private HttpUtils.downloadParam getTask(){
        HttpUtils.downloadParam dp=null;
        synchronized (this){
            if(mWaitTasks.size()>0) {
                 dp= mWaitTasks.remove(0);
                 mDownedTasks.add(dp);
            }
        }

        if(dp!=null){//如果有任务
            asyncCaller.AddTask(mCallBack, null);//发送信号
        }
        return dp;
    }

    private void finishTask(int code,HttpUtils.downloadParam dp){
        synchronized (this){
            mDownedTasks.remove(dp);
        }
        setResLoaded(dp.getUrl(),new MessageSet( code, dp.getUrl()));
    }

    public void addTask(List<HttpUtils.downloadParam> listCurrent) {
        for(HttpUtils.downloadParam dp:listCurrent){
            LogUtils.i("addTask:"+dp.getUrl());
        }

        //1.暂停新的任务
        asyncCaller.Pause();

        //2.任务修正
        taskFix(listCurrent);

        int nSignals=asyncCaller.getThreadCount();
        while(nSignals-->0)
            asyncCaller.AddTask(mCallBack, null);//发送信号

        asyncCaller.Resume();
    }

    public void addListener(String strUrl,Receiver receiver){
        resNotLoaded(strUrl,receiver);
    }

    //任务修正
    private void taskFix(List<HttpUtils.downloadParam> listCurrent){
        int maxThreads=asyncCaller.getThreadCount();//当前只允许这么多个任务同时下载
        synchronized (this){

            for(int i = 0;i<mDownedTasks.size();++i){//处理正在下载队列
                HttpUtils.downloadParam downloading=mDownedTasks.get(i);

                boolean bstop=true;//不停止的条件，当前下载的任务在新的队列的前  maxThreads 里
                int j=0;//在当前队列查找
                for(;j<listCurrent.size() && j<maxThreads ;++j){//必须在threads 以内
                    HttpUtils.downloadParam dpNow=listCurrent.get(j);
                    if(dpNow.getUrl().equals(downloading.getUrl())){
                        bstop=false;
                        break;
                    }
                }

                if(bstop) {//如果需要停止
                    mDownedTasks.remove(i);//下载队列移除这个任务    这个任务可能在等待任务里，只是优先级不够
                    downloading.setCancel(true);//取消下载
                    --i;
                }else{//如果不
                    listCurrent.remove(j);//保留在download里，从等待队列移除
                    if(--maxThreads==0) break;//最大减去1
                }
            }

            mWaitTasks =listCurrent;//等待队列更换为当前队列
        }
    }
}
