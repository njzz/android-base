package com.librarys.tools.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.librarys.tools.R;
import com.njzz.bases.common.ActManager;
import com.njzz.bases.common.ErrorCode;
import com.njzz.bases.common.MessageSet;
import com.njzz.bases.common.QuickAdapter;
import com.njzz.bases.common.ResManager;
import com.njzz.bases.utils.ResUtils;
import com.njzz.bases.common.UIReceiver;
import com.njzz.bases.utils.LogUtils;
import com.njzz.bases.utils.Utils;
import com.librarys.tools.design.bean.Video;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends QuickAdapter<Video> {

    private Context mContext;
    private int mCurShow=-1;

    public VideoAdapter(List<Video> datas, int commonItemlayOut,Context context) {
        super(datas, commonItemlayOut);
        mContext=context;
    }

    public void setCurrent(int current,QuickAdapter.VH holder, Video data){
        if(current!=mCurShow) {
            stopLast();

            mCurShow = current;
            //显示图片
            showBitmap(data.img,holder.getView(R.id.image_show));

            //显示视频
            String vdoPath=null;
            if (!Utils.emptystr(data.video)) {
                vdoPath=RecentVideoMgr.getVideo(data.video,new UIReceiver(ActManager.ins().last()) {
                    @Override
                    public void OnNotify(MessageSet ms) {
                        if (ms.what == ErrorCode.SUCCESS || ms.what == ErrorCode.EXIST) {
                            String diskPath = ResUtils.getDiskPath((String) ms.argObj, ResUtils.ResType.VIDEO_CACHE);
                            VideoPlay(diskPath, holder);
                        }
                    }
                });

                if (vdoPath != null) {
                    VideoPlay(vdoPath, holder);
                }
            }else{
                VideoPlay(null, holder);
            }

            //缓存
            List<String> lstCache=new ArrayList<>(3);
            if(vdoPath==null && !Utils.emptystr(data.video)){
                lstCache.add(data.video);
            }
            for(int i=1;i<3;++i){
                Video vCache=getData(current+i);
                if(vCache!=null && !Utils.emptystr(vCache.video)){
                    lstCache.add(vCache.video);
                }
                else
                    break;
            }
            RecentVideoMgr.cacheVideo(lstCache);
        }
    }

    private void showBitmap(String strBmp,ImageView iv){
        Bitmap bmp = ResManager.getBitmap(strBmp, new UIReceiver(ActManager.ins().last()) {
            @Override
            public void OnNotify(MessageSet ms) {
                if (ms.what == ErrorCode.SUCCESS || ms.what == ErrorCode.EXIST) {
                    iv.setImageBitmap(ResManager.getBitmap(strBmp,null));
                }
            }
        });

        if (bmp != null) {
            iv.setImageBitmap(bmp);
        }
    }

    @Override
    public void convert(QuickAdapter.VH holder, Video data) {
        //LogUtils.d("item set:"+position);
        showBitmap(data.img,holder.getView(R.id.image_show));

        if(mLastPlay ==null){
            setCurrent(holder.getCurPos(),holder,data);
        }
    }

    private WeakReference<VH> mLastPlay;
    private void stopLast(){
        if(mLastPlay !=null) {//隐藏视频
            QuickAdapter.VH lastHolder = mLastPlay.get();

            //隐藏最后的视频
            if(lastHolder!=null) {
                ImageView ivLast=lastHolder.getView(R.id.image_show);
                VideoView vvLast=lastHolder.getView(R.id.video_show);
                vvLast.stopPlayback();//停止播放
                vvLast.setVisibility(View.GONE);//隐藏视频
                ivLast.setVisibility(View.VISIBLE);//显示图片
                //LogUtils.d("item ---hide:"+lastHolder.getCurPos());
            }
        }

    }

    private void VideoPlay(final String file, final QuickAdapter.VH holder) {

        final ImageView iv = holder.getView(R.id.image_show);
        final VideoView v = holder.getView(R.id.video_show);
        iv.setVisibility(View.VISIBLE);

        if (holder.isNew()) {
            MediaController mc = new MediaController(mContext);
            mc.setVisibility(View.GONE);
            v.setMediaController(mc);
        }

        if(Utils.emptystr(file)){
            LogUtils.d("无视频显示." );
        }
        else{
            v.setVisibility(View.VISIBLE);
            v.setVideoPath(file);
            v.requestFocus();
            v.start();

            LogUtils.d("显示视频:" + file);
            mLastPlay = new WeakReference<>(holder);

            v.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //v.start();
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mediaPlayer, int what, int i1) {
                            //开始播放时，就把显示第一帧的ImageView gone 掉
                            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {

//                            new Handler(Looper.getMainLooper(),new Handler.Callback(){
//                                @Override
//                                public boolean handleMessage(@NonNull Message message) {
//                                   iv.setVisibility(View.GONE);
//                                    return false;
//                                }
//                            }).sendEmptyMessageDelayed(1,50);
                                iv.setVisibility(View.GONE);
                                return true;
                            }
                            return false;
                        }
                    });

                }
            });

            v.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    v.start();
                }
            });


            v.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    //v.start();
                    return true;
                }
            });
        }
    }
}
