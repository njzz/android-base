package com.njzz.bases.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.njzz.bases.R;
import com.njzz.bases.utils.DensityUtils;
import com.njzz.bases.utils.LogUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

//自定义recyclerview 实现刷新。
public class RecylerViewPlus extends RecyclerView {

    public interface  onRefreshListener{
        boolean canRefresh();//返回是否可以刷新
        boolean canLoadMore();//返回是否可以加载更多
        void doRefresh();//开始刷新
        void doLoadMore();//开始加载更多
    }

    private static final int  PROCESS_NONE=0;//没有处理
    private static final int  PROCESS_PULL_REFRESH=1;//处理刷新
    private static final int  PROCESS_PULL_LOADMORE=2;//处理加载更多
    private static final int PROCESS_DO_WORKING =3;//正在刷新处理

    public static final int SET_REFRESH=1;
    public static final int SET_LOADMORE=2;

    private float mLastY = -1; // save event y
    private int [] mStagSpinCount;//瀑布流显示获取最后的显示
    //头控件
    private RefreshHead mHeaderView;
    //尾控件
    private RefreshFeet mFooterView;
    //adapter的装饰类
    private SpcialItemWrap mHeaderAndFooterWrapper;

    PagerSnapHelper mSnapHelper;//整屏滚动
    private int mInProcessing=PROCESS_NONE,mEnableSet;
    private onRefreshListener mListener;
    private boolean mLoadmoreNotifyed;

    public RecylerViewPlus(Context context) {
        super(context);
    }

    public RecylerViewPlus(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public RecylerViewPlus(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //需要先设置适配器
    public void setOnRefreshListener(onRefreshListener n,int set){
        mListener =n;
        mEnableSet = set;
        init();
    }

    public void setPageScroll(boolean bSet){
        if(bSet) {
            if(mSnapHelper==null) mSnapHelper=new PagerSnapHelper();
            mSnapHelper.attachToRecyclerView(this);
        }else{
            if(mSnapHelper!=null){
                mSnapHelper.attachToRecyclerView(null);
                mSnapHelper=null;
            }
        }
    }

    public int getPagePos(){
        if(mSnapHelper!=null) {
            LayoutManager layoutManager = getLayoutManager();
            if(layoutManager!=null) {
                View snapView = mSnapHelper.findSnapView(layoutManager);
                if(snapView!=null){
                    return layoutManager.getPosition(snapView);
                }
            }
        }
        return -1;
    }

    private void init() {
        Adapter adapter=getAdapter();
        if(mHeaderAndFooterWrapper==null && adapter instanceof QuickAdapter) {
            mHeaderAndFooterWrapper=new SpcialItemWrap();
            ((QuickAdapter)adapter).setSpcial(mHeaderAndFooterWrapper);
            if ((mEnableSet & SET_REFRESH) != 0 && mHeaderView == null) {
                //获取到头布局
                mHeaderView = new RefreshHead(getContext());
            }
            if ((mEnableSet & SET_LOADMORE) != 0 && mFooterView == null) {
                //获取尾布局
                mFooterView = new RefreshFeet(getContext());
            }
            //setClickable(true);
        }
    }

    public boolean isFreshSet(){
        return (mEnableSet & SET_REFRESH) != 0;
    }

    public boolean isLoadmoreSet(){
        return (mEnableSet & SET_LOADMORE)!=0;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if(!(adapter instanceof  QuickAdapter)){
            LogUtils.w("Adapter is not based on QuickAdapter!");
        }
        super.setAdapter(adapter);
    }

    //dispatchTouchEvent  是否分发  onInterceptTouchEvent 是否拦截  onTouchEvent 是否处理

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev){
//        return false;
//    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if(mInProcessing!=PROCESS_NONE)
//            return true;
        // onTouchEvent 经常收不到 ACTION_DOWN
        if ( mListener !=null&& mHeaderAndFooterWrapper!=null && mInProcessing==PROCESS_NONE) {
            if(ev.getAction() == MotionEvent.ACTION_DOWN ) {
                mLastY = ev.getRawY();
            }
            else if(ev.getAction() == MotionEvent.ACTION_MOVE) {
                float yMove = ev.getRawY();
                if (yMove - mLastY > 1) {//下拉
                    if (isFreshSet() && isFirstAllVisable() && mListener.canRefresh() ) {
                        mInProcessing = PROCESS_PULL_REFRESH;
                        return true;
                    }
                }else if(yMove - mLastY < -1){//上拉
                    if ( isLoadmoreSet() && isLastAllVisable()) {
                        if (mListener.canLoadMore()) {//如果可以加载
                            mLoadmoreNotifyed=false;
                            mInProcessing = PROCESS_PULL_LOADMORE;
                            return true;
                        } else if (mFooterView != null) {//支持loadmore，但没有更多，显示加载成功完成
                            mFooterView.showComplete(true);
                        }
                    }
                }
            }
        }

        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if(mInProcessing!=PROCESS_NONE ) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float moveY = e.getRawY();
                    float distanceY = moveY - mLastY;
                    if ( mInProcessing == PROCESS_PULL_REFRESH ) {
                        //顶部显示下拉
                        if(distanceY > 0) {//下滑过程中
                            mHeaderView.showPulling(Math.round(distanceY));
                            scrollToPosition(0);//显示第一个
                            return true;
                        }

                    } else if ( mInProcessing == PROCESS_PULL_LOADMORE ) {
                        //底部直接刷新
                        if(distanceY < 0) {
                            mFooterView.showPulling(Math.round(distanceY));
                            //mFooterView.showLoading();//直接是loading
                            //mInProcessing = PROCESS_DO_WORKING;
                            if(!mLoadmoreNotifyed) {
                                mLoadmoreNotifyed=true;
                                mListener.doLoadMore();//上拉直接开始加载更多
                            }
                            //scrollToPosition(mHeaderAndFooterWrapper.getItemCount()-1);//显示最后一项
                            //return true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //LogUtils.i("touch up");
                    if(mInProcessing==PROCESS_PULL_REFRESH){//顶部下拉，判断放开时状态
                        if(mHeaderView.canDoworking()) {
                            mHeaderView.showLoading();
                            mInProcessing = PROCESS_DO_WORKING;
                            mListener.doRefresh();
                        }else{
                            mHeaderView.hide();
                            mInProcessing =  PROCESS_NONE;
                            //bProcessed=false;
                        }
                    }else if( mInProcessing == PROCESS_PULL_LOADMORE){
                        if(mFooterView.getShowHeight()>mFooterView.getSrcHeight()) {
                            mInProcessing = PROCESS_DO_WORKING;//如果是加载更多，释放后设置为工作状态
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(e);
    }

    private boolean isFirstAllVisable(){
        return !canScrollVertically(-1);
//        RecyclerView.LayoutManager lm= getLayoutManager();
//        if(lm!=null){
//            if(lm instanceof LinearLayoutManager ){//GridLayoutManager 从 LinearLayoutManager 派生
//                int posVAll= ((LinearLayoutManager)lm).findFirstCompletelyVisibleItemPosition();
//                return posVAll<=1;//第0个是刷新
//            }else if(lm instanceof StaggeredGridLayoutManager){
//                if(mStagSpinCount==null){
//                    mStagSpinCount=new int [((StaggeredGridLayoutManager) lm).getSpanCount()];
//                }
//                ((StaggeredGridLayoutManager)lm).findFirstCompletelyVisibleItemPositions(mStagSpinCount);
//                int minShow = findMin(mStagSpinCount);
//                return minShow<=1;
//            }
//        }
//        return true;
    }

    private int findMin(int[] lastPositions) {
        int min = lastPositions[0];
        for (int value : lastPositions) {
            if (value < min)
                min = value;
        }
        return min;
    }

    private boolean isLastAllVisable(){
        return canScrollVertically(1);
//        RecyclerView.LayoutManager lm= getLayoutManager();
//        Adapter adapter=getAdapter();
//        if(lm!=null && adapter!=null){
//            int total=adapter.getItemCount();
//            if(total>0) {
//                if (lm instanceof LinearLayoutManager) {//GridLayoutManager 从 LinearLayoutManager 派生
//                    //int posVAll = ((LinearLayoutManager) lm).findLastCompletelyVisibleItemPosition();
//                    int posVAll=((LinearLayoutManager) lm).findLastVisibleItemPosition();
//                    return posVAll >= total - 2;//最后一个是加载更多
//                } else if (lm instanceof StaggeredGridLayoutManager) {
//                    if (mStagSpinCount == null) {
//                        mStagSpinCount = new int[((StaggeredGridLayoutManager) lm).getSpanCount()];
//                    }
//                    ((StaggeredGridLayoutManager) lm).findLastCompletelyVisibleItemPositions(mStagSpinCount);
//                    int maxShow = findMax(mStagSpinCount);
//                    return maxShow == total - 2;//最后是加载更多
//                }
//            }
//        }
//        return false;
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max)
                max = value;
        }
        return max;
    }

    /**
     * 判断是否到底
     */
    private boolean isSlideToBottom() {
        return computeVerticalScrollExtent() + computeVerticalScrollOffset() >= computeVerticalScrollRange();
    }

    /**
     * 终止下拉刷新
     */
    public void finishRefresh(boolean bSuccess) {
        if(mHeaderView!=null && mHeaderView.isShowed() )
            mHeaderView.showComplete(bSuccess);
    }

    /**
     * 停止加载
     */
    public void finishLoadMore(boolean bSuccess) {
        if(mFooterView!=null && mFooterView.isShowed())
            mFooterView.showComplete(bSuccess);
    }

    public boolean isShowRefresh(){
        return mHeaderView!=null && mHeaderView.isShowed();
    }
    public boolean isShowLoadmore(){
        return mFooterView!=null && mFooterView.isShowed();
    }


//    @Override
//    public void addView(View v,int index){
//        super.addView(v,index);
//        LayoutParams lp=(LayoutParams) v.getLayoutParams();
//        LogUtils.i(  lp.width +"  -------   index:"+index );
//    }


    private class RefreshCtrl {
        ProgressBar mPBar;
        TextView mTextLoading;
        TextView mTextResult;
        View vRootSet;
        int mViewHeight;
        boolean mShowed;
        boolean mCanDoWorking;
        RefreshCtrl(Context context){
            View v = LayoutInflater.from(context).inflate(R.layout.recylerview_feet, null);
            RecyclerView.LayoutParams lp=new RecyclerView.LayoutParams(MATCH_PARENT,WRAP_CONTENT);//预设一个LayoutParams，inflate 的时候没有parent ,LinerLayoutManager 会是 WRAP_CONTENT,WRAP_CONTENT
            v.setLayoutParams(lp);
            mViewHeight=DensityUtils.dp2px(context,25f);

            mPBar = v.findViewById(R.id.item_footer_progress);
            mTextLoading = v.findViewById(R.id.item_footer_message);
            mTextResult = v.findViewById(R.id.item_footer_complete);
            vRootSet=v;
        }

        boolean isShowed() {
            return mShowed;
        }
        boolean canDoworking(){
            return mCanDoWorking;
        }
        int getSrcHeight(){
            return mViewHeight;
        }
        int getMaxHeight() {return mViewHeight*2;}

        int getShowHeight(){
            RecyclerView.LayoutParams mLp=(RecyclerView.LayoutParams) vRootSet.getLayoutParams();
            if(mLp!=null) {
                return mLp.height;
            }
            return 0;
        }

        protected void attach(boolean bHead){
            if (bHead)
                mHeaderAndFooterWrapper.setFirstView(vRootSet);
            else
                mHeaderAndFooterWrapper.setLastView(vRootSet);
            getAdapter().notifyDataSetChanged();
        }

        protected void detach(boolean bHead){
            if(bHead)
                mHeaderAndFooterWrapper.removeHeadView(vRootSet,true);
            else
                mHeaderAndFooterWrapper.removeFootView(vRootSet,true);
            getAdapter().notifyDataSetChanged();
        }

        void showPulling(int offset){
            mShowed=true;
            offset=Math.abs(offset);
            offset=Math.min(offset/3 , getMaxHeight());
            setHeight(offset);
        }

        void showLoading(){
            mShowed=true;
            if(getShowHeight()<getSrcHeight()){
                setHeight(getSrcHeight());
            }
            if(mPBar.getVisibility()!=VISIBLE){
                mPBar.setVisibility(VISIBLE);
                mTextLoading.setVisibility(VISIBLE);
                mTextResult.setVisibility(GONE);
            }
            mTextLoading.setText("加载中…");
        }

        void showComplete(boolean bSuccessed){
            mShowed=true;
            if(getShowHeight()<getSrcHeight()){
                setHeight(getSrcHeight());
            }
            if(mPBar.getVisibility()!=GONE){
                mPBar.setVisibility(GONE);
                mTextLoading.setVisibility(GONE);
                mTextResult.setVisibility(VISIBLE);
            }
            mTextResult.setText(bSuccessed?"加载完成":"加载失败");

            vRootSet.postDelayed(this::hide,1000);
        }

        void hide(){
            mInProcessing=PROCESS_NONE;
            mShowed=false;
            setHeight(5);
        }

        void setHeight(int height){

            RecyclerView.LayoutParams lpSet=(RecyclerView.LayoutParams) vRootSet.getLayoutParams();
            if(lpSet!=null && height!=lpSet.height) {
                lpSet.height = height;
                //vRootSet.requestLayout();
                vRootSet.setLayoutParams(lpSet);

                mCanDoWorking=height>getSrcHeight()+getSrcHeight()/2;

            }
        }

//        private void setMarginTop(int top){
//            RecyclerView.LayoutParams mLp=(RecyclerView.LayoutParams) vRootSet.getLayoutParams();
//            if(mLp!=null) {
//                mLp.topMargin = top;
//                vRootSet.setLayoutParams(mLp);
//            }
//        }
//
//        private void setMarginBottom(int bottom){
//            RecyclerView.LayoutParams mLp=(RecyclerView.LayoutParams) vRootSet.getLayoutParams();
//            if(mLp!=null) {
//                mLp.bottomMargin = bottom;
//                vRootSet.setLayoutParams(mLp);
//            }
//        }
    }

    private class RefreshFeet extends RefreshCtrl{
        RefreshFeet(Context context){
            super(context);
            attach(false);
            hide();
        }

        @Override
        void showPulling(int offset){//只有下拉才有
            if (mPBar.getVisibility() != VISIBLE) {
                mPBar.setVisibility(VISIBLE);
                mTextLoading.setVisibility(VISIBLE);
                mTextResult.setVisibility(GONE);
            }
            mTextLoading.setText("加载中…");
            super.showPulling(offset);
        }
    }


    private class RefreshHead extends RefreshCtrl {

        RefreshHead(Context context){
            super(context);
            attach(true);
            hide();
        }

        @Override
        void showPulling(int offset){//只有下拉才有
            if (mPBar.getVisibility() != GONE) {
                mPBar.setVisibility(GONE);
                mTextLoading.setVisibility(GONE);
                mTextResult.setVisibility(VISIBLE);
            }

            super.showPulling(offset);
            mTextResult.setText(canDoworking()?"释放刷新":"下拉刷新");
        }
    }


//    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
//        @Override
//        public void onChanged() {
//            //Adapter<?> adapter = getAdapter(); //这种写发跟之前我们之前看到的ListView的是一样的，判断数据为空否，再进行显示或者隐藏
//            if ( mEmptyView != null) {
//                if (mHeaderAndFooterWrapper.getRealItemCount() == 0) {
//                    mEmptyView.setVisibility(View.VISIBLE);
//                    RecylerViewPlus.this.setVisibility(View.GONE);
//                } else {
//                    mEmptyView.setVisibility(View.GONE);
//                    RecylerViewPlus.this.setVisibility(View.VISIBLE);
//                }
//            }
//        }
//    };


//    boolean isNestedEnable = true;
//    public boolean isNestedEnable() {
//        return isNestedEnable;
//    }
//    public void setNestedEnable(boolean nestedEnable) {
//        isNestedEnable = nestedEnable;
//    }
//    @Override
//    public boolean startNestedScroll(int axes, int type) {
//        if (isNestedEnable) {
//            return super.startNestedScroll(axes, type);
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public void stopNestedScroll(int type) {
//        if (isNestedEnable) {
//            super.stopNestedScroll(type);
//        }
//    }
//
//    @Override
//    public boolean hasNestedScrollingParent(int type) {
//        if (isNestedEnable) {
//            return super.hasNestedScrollingParent(type);
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type) {
//        if (isNestedEnable) {
//            return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) {
//        if (isNestedEnable) {
//            return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
//        } else {
//            return false;
//        }
//    }
}
