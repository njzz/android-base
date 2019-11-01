package com.njzz.bases.common;

import com.njzz.bases.utils.LogUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AsyncCaller {

    private class ThreadTask {//线程数据封装
        ThreadTask(Receiver nm, MessageSet ms){
            this.ms=ms;
            this.notify=nm;
        }
        int tid;//任务id
        MessageSet ms;
        Receiver notify;
    }

    private class ThreadRunner implements Runnable{
        @Override
        public void run(){
            if(mThreadName!=null){
                Thread.currentThread().setName(mThreadName);
            }
            while(!mStop) {
                ThreadTask task = getTask();
                if (task != null && !mStop ) {
                    try {
                        Notify.Send(task.notify,task.ms);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    private List<ThreadRunner> mListRunner=new ArrayList<>();//小的不变的队列
    private LinkedList<ThreadTask> mListTasks=new LinkedList<>();//经常增删的队列
    private int mMaxTask,mGid=0;//最大任务数，任务id
    private boolean mPause=false,mStop=true;
    private String mThreadName;
    private Receiver mRemoveListener=null;
    public AsyncCaller(){
        this(3,-1);//默认3个，不要获取cpu或者线程个数，可能被多用
    }

    /**
     * 开启线程，自动开启
     * @param maxThread 最大线程数
     * @param maxTask 最大等待任务数(如果等待任务数量超过，则自动移除尾部任务,负数和0不限制任务数)
     */
    public AsyncCaller(int maxThread, int maxTask){
        if(maxThread<1)
            maxThread=1;
        if(maxTask<=0)
            maxTask=-1;

        mMaxTask=maxTask;

        for(int i=0;i<maxThread;++i){
            mListRunner.add(new ThreadRunner());
        }

        Start();
    }

    /**
     * 获取等待任务数量
     * @return
     */
    public int getTaskSize(){
        return mListTasks.size();
    }

    public int getThreadCount() {return mListRunner.size();}

    public boolean isPaused(){
        return mPause;
    }

    public boolean isStoped(){
        return mStop;
    }

    public void setRemoveListener(Receiver nn){
        mRemoveListener=nn;
    }

    public void setName(String strName){
        mThreadName=strName;
    }

    /**
     * 暂停后续执行，正在执行的无法暂停
     */
    public void Pause(){
        mPause=true;
    }

    /**
     * 暂停恢复执行
     */
    public void Resume(){
        mPause=false;
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * 停止，如果任务已经正在运行，则无法停止。但并不移除任务，如果要移除任务，需要调用removeAll
     */
    public void Stop(){
        synchronized (this) {
            mStop=true;
            notifyAll();
        }
    }

    /**
     * 开始，创建后自动调用，只有停止后才需要重新Start
     */
    public void Start(){
        boolean doStart=false;
        synchronized (this){
            if(mStop){
                mStop=false;
                doStart=true;
            }
        }

        if(doStart) {
            for (ThreadRunner tr : mListRunner) {
                new Thread(tr).start();
            }
        }
    }

    /**
     * 增加任务,增加到最后
     * @param nm 通知对象
     * @param ms 透传参数1
     * @return 任务id ，可以根据id操作任务，-1为失败
     */
    public int AddTask(Receiver nm, MessageSet ms){
        return addAtPos(nm, ms,-1);
    }

    /**
     *参数同上
     * @param index 任务插入的位置，负数为尾，0为头
     * @return
     */
    public int AddTask(Receiver nm, MessageSet ms, int index){
        return addAtPos(nm, ms,index);
    }

    /**
     * 取消等待的任务，正在运行的无法取消，需要控制任务本身
     */
    public boolean removeTask(int id){
        synchronized (this){
            for(ThreadTask td:mListTasks){
                if(td.tid==id){
                    mListTasks.remove(td);
                    return true;
                }
            }
        }
        return false;
    }

    public void removeAll(){
        synchronized (this){
            mListTasks.clear();
        }
    }

    private int addAtPos(Receiver nm,MessageSet ms, int index){
        int rt=-1;
        ThreadTask tdsRemoved=null;
        if(nm!=null) {
            ThreadTask tds=new ThreadTask(nm,ms);
            synchronized (this) {//必须在lock里等待，不然会异常
                rt=++mGid;
                tds.tid=rt;//任务id
                if(mGid==Integer.MAX_VALUE){//归0 (超过限制的可能很小，如果每个后台线程任务耗时100ms[即每100ms分配一个id]，则大约6.8年后归0)
                    mGid=0;
                }
                //将任务放到适当位置
                if(index==0)
                    mListTasks.addFirst(tds);//插入到开始
                else if(index>0 && index<mListTasks.size())
                    mListTasks.add(index,tds);//插入到适当位置
                else
                    mListTasks.addLast(tds);//插入到队尾

                //如果有任务数限制，并且任务数超出，移除尾部任务
                if(mMaxTask!=-1 && mListTasks.size()>mMaxTask){
                    tdsRemoved=mListTasks.removeLast();
                }

                if(!mPause)
                    notify();//java 对象自带方法
            }
        }

        if(tdsRemoved!=null) {
            if (mRemoveListener != null) {//如果有则通知
                Notify.Send(mRemoveListener, tdsRemoved.ms);
            }else {//不要通知 tdsRemoved.notify ，没有意义，参数也没有意义
                LogUtils.w("AsyncCaller Task removed,but no remove listener exist.");
            }
        }

        return rt;
    }

    /**
     * 获取任务
     * @return
     */
    private ThreadTask getTask(){
        ThreadTask td=null;
        try {
            synchronized (this) {//必须在lock里等待，不然会异常
                if ( mPause || mListTasks.size() == 0) {
                    wait();//会解锁进入等待，激活后获得锁
                }

                if (mStop) return null;

                if (!mPause && mListTasks.size() > 0) {
                    td = mListTasks.remove(0);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return td;
    }
}
