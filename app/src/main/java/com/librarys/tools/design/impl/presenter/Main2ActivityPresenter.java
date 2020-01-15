package com.librarys.tools.design.impl.presenter;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.librarys.tools.R;
import com.librarys.tools.common.VideoAdapter;
import com.librarys.tools.constant.ListKey;
import com.njzz.bases.common.ErrorCode;
import com.njzz.bases.common.HttpServer;
import com.njzz.bases.common.MapObject;
import com.njzz.bases.common.MessageSet;
import com.njzz.bases.common.QuickAdapter;
import com.njzz.bases.common.RecylerViewPlus;
import com.njzz.bases.common.ResManager;
import com.njzz.bases.common.UIReceiver;
import com.njzz.bases.common.ViewPagerLayoutManager;
import com.njzz.bases.utils.LogUtils;
import com.njzz.bases.utils.Utils;
import com.librarys.tools.common.ImageListDataRequestor;
import com.librarys.tools.design.bean.Video;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Main2ActivityPresenter {
    private static boolean mGlobalSet=false;
    private Context mContext;
    RecylerViewPlus mRecylerView;
    List<Video> mDataset;
    VideoAdapter mAdapter;
    int mPage=0;
    boolean mFinished=false;
    boolean mRequestDatas=false;

    public Main2ActivityPresenter(Context context,RecylerViewPlus rvp){
        mContext =context;
        mRecylerView=rvp;
    }

    private void initGlobalSet(){
        if(!mGlobalSet){
            mGlobalSet=true;
            HttpServer.setServer("http://appts.17boom.com/");
            HttpServer.setGlobalParam("version", "123");
            HttpServer.setGlobalParam("platform", "1");
            HttpServer.setGlobalParam("timestamp", Utils.getCurrentTime());
            HttpServer.setGlobalParam("system", "1");
            HttpServer.setGlobalParam("md5", Utils.md5(""));
            HttpServer.setGlobalParam("token","");
            HttpServer.setGlobalParam("machine", "test_machine_set");
        }
    }

    private void initView(){
        mDataset=new LinkedList<>();
        mAdapter= new VideoAdapter(mDataset, R.layout.recyler_item, mContext);

        mRecylerView.setAdapter(mAdapter);

        ViewPagerLayoutManager layoutManager = new ViewPagerLayoutManager(mContext);
        layoutManager.setListener(new ViewPagerLayoutManager.Listener() {

            @Override
            public void onPageSelected(int position, boolean isBottom) {
                onPagerSelected(position );

                //加载更多数据
                if(position>=mDataset.size()-8){
                    requestData();
                }
            }
        });

        mRecylerView.setLayoutManager(layoutManager);
        mRecylerView.setHasFixedSize(true);



        mRecylerView.setOnRefreshListener(new RecylerViewPlus.onRefreshListener() {
            @Override
            public boolean canRefresh() {
                return true;
            }

            @Override
            public boolean canLoadMore() {
                return false;
            }

            @Override
            public void doRefresh() {
                Utils.UIRun(()->mRecylerView.finishRefresh(true),3000);
            }

            @Override
            public void doLoadMore() {

            }
        },RecylerViewPlus.SET_REFRESH);
    }

    public void start(){
        initGlobalSet();
        initView();
        requestData();
    }

    private void requestData(){
        if(mFinished || mRequestDatas ) return;
        mRequestDatas=true;

        ImageListDataRequestor req=new ImageListDataRequestor(null, ListKey.videoAll(), new UIReceiver(null) {
            @Override
            public void OnNotify(MessageSet ms) {
                if(ms.what<300 && ms.what!= ErrorCode.CONNECT){
                    ImageListDataRequestor.Result obj=Utils.cast(ms.argObj);
                    if(obj!=null){
                        int sizeAdded=obj.dataSet.size();
                        int lastData=mDataset.size();
                        for(MapObject d:obj.dataSet) {
                            mDataset.add(new Video(d.getString(ListKey.VIDEO_IMG),d.getString(ListKey.VIDEO_URL)));
                        }

                        if(sizeAdded>0){
                            //mAdapter.notifyDataSetChanged();
                            mAdapter.notifyItemRangeInserted(mAdapter.getItemCount()-sizeAdded,sizeAdded);
                            loadImageData(lastData);
                        }else{
                            mFinished=true;
                        }

                        if(mDataset.size()<5){//页数据太少
                            requestData();
                        }
                    }
                }
                mRequestDatas=false;
            }
        });

        LogUtils.d("request data page:"+ mPage );

        HashMap<String, Object> param = new HashMap<>();
        param.put("page",mPage++);
        param.put("type","time");
        param.put("timestep",Utils.getCurrentTime());

        req.Get("api/homepage",param);
    }

    private void onPagerSelected(int where){
        RecyclerView.ViewHolder viewHolderForAdapterPosition = mRecylerView.findViewHolderForAdapterPosition(where);
        if (viewHolderForAdapterPosition instanceof QuickAdapter.VH) {//当前显示
            //设置当前
            mAdapter.setCurrent(where-1, (QuickAdapter.VH) viewHolderForAdapterPosition, mDataset.get(where-1));
        }

//        if(where+1<mDataset.size()){
//            RecentVideoMgr.getVideo(mDataset.get(where+1).video,null);
//        }
    }

    private void loadImageData(int start){
        if(start>=0){
            int end=mDataset.size()-1;//逆序
            while(end>=start){

                Video v=mDataset.get(end);
                //下载图片
                ResManager.getBitmap(v.img, null);
                end--;
            }
        }
    }
}
