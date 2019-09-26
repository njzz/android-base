package com.njzz.bases.common;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.njzz.bases.utils.LogUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//recylcerview，实现头部控件，尾部控件，空控件
public class SpcialItemWrap {

    private class ItemSet{
        ItemSet(View v,boolean fix){
            view=v;
            fixed=fix;
        }
        private View view;
        private boolean fixed;
    }

    private static final int EMPTY_ITEM_TYPE=999999;//空显示控件，定义为 (真实数据为空时显示，如果有头控件，显示在头控件后，如果有尾控件，显示在尾控件前)

    private static final int BASE_ITEM_TYPE_HEADER = 1000000;//头部控件type,100000~199999
    private static final int BASE_ITEM_TYPE_FOOTER = 2000000;//尾部控件type,200000~

    //头集合 尾集合
    private View mEmptyView=null;//空控件
    private int mRealItemCount=0;//真是item个数
    private LinkedList<ItemSet> mHeaderViews = new LinkedList<>();//头部集合，包括第一个
    private LinkedList<ItemSet> mFootViews = new LinkedList<>();//尾部集合，包括最后一个

    /**
     * 设置第一个view，只有一个
     * @param view 设置的view
     */
    public void setFirstView(View view){
        ItemSet is;
        if(!mHeaderViews.isEmpty()){
            is=mHeaderViews.get(0);
            if(is.fixed){//已经存在第一个，修改
                if(view!=null) {
                    is.view = view;
                }else{
                    mHeaderViews.removeFirst();//如果新的为空，删除
                }
                return;
            }
        }
        if(view!=null) { //不存在，增加
            is = new ItemSet(view, true);
            mHeaderViews.addFirst(is);
        }
    }

    /**
     * 添加头部方法
     * @param view 添加的view
     */
    public void addHeaderView(View view) {
        if(view!=null)
            mHeaderViews.add(new ItemSet(view,false));
    }

    /**
     * 移除一个头view
     * @param view 需要移除的view
     * @param removeOne true，只删除一个，false ，删除所有这个view
     */
    public void removeHeadView(View view,boolean removeOne){
        ItemSet is;
        for(int i=0;i<mHeaderViews.size();){
            is=mHeaderViews.get(i);
            if(is.view==view){
                mHeaderViews.remove(i);
                if(removeOne) break;
            }else{
                i++;
            }
        }
    }

    /**
     * 增加空控件
     */
    public void setEmptyView(View v){
        mEmptyView=v;
    }

    /**
     * 添加尾部方法
     * @param view
     */
    public void setLastView(View view){//确保是添加到最后一个，只能有一个
        ItemSet is;
        if(!mFootViews.isEmpty()){
            is=mFootViews.getLast();
            if(is.fixed){//已经存在最后一个，修改
                if(view!=null) {
                    is.view = view;
                }else{
                    mFootViews.removeLast();
                }
                return;
            }

        }
        if(view!=null) { //不存在，增加
            is = new ItemSet(view, true);
            mFootViews.addLast(is);
        }
    }

    public void addFootView(View view) {
        if (view != null) {
            if (!mFootViews.isEmpty() && mFootViews.getLast().fixed) {//如果最后一个为last
                mFootViews.add(mFootViews.size() - 1, new ItemSet(view, false));
                return;
            }
            //没有last
            mFootViews.add(new ItemSet(view, false));
        }
    }

    /**
     * 移除一个尾view
     * @param view 需要移除的view
     * @param removeOne true，只删除一个，false ，删除所有这个view
     */
    public void removeFootView(View view,boolean removeOne){
        ItemSet is;
        for(int i=0;i<mFootViews.size();){
            is=mFootViews.get(i);
            if(is.view==view){
                mFootViews.remove(i);
                if(removeOne) break;
            }else{
                i++;
            }
        }
    }

    /**
     * 根据pos判断是否为特殊项
     * @param realCount 真实数据数
     * @param position 位置
     * @return 是否特殊项
     */
    public boolean isItemSpecial(int realCount,int position){
        setRealItemCount(realCount);
        return isEmptyViewPos(position) || isHeaderViewPos(position) || isFooterViewPos(position);
    }

    /**
     * 获取item类型
     * @param realCount 真实数据数
     * @param position 位置
     * @return type，如果不是特殊项，返回-1
     */
    public int getItemViewType(int realCount,int position) {
        setRealItemCount(realCount);
        if (isHeaderViewPos(position)) {
            return getHeadType(position);
        }else if(isEmptyViewPos(position)){
            return EMPTY_ITEM_TYPE;
        }else if (isFooterViewPos(position)) {
            return getFootType(position - getHeadersCount() - getRealItemCount() -getEmptyCount() );
        }
        return -1;
    }

    public int getItemCount(int realCount) {
        setRealItemCount(realCount);
        return getHeadersCount() + getFootersCount() + getRealItemCount() + getEmptyCount();
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=null;
        if(viewType==EMPTY_ITEM_TYPE)
            view=mEmptyView;
        if(view==null){//查找头控件
            view = getHeadByType(viewType);
        }
        if(view==null){//查找尾控件
            view = getFootByType(viewType);
        }

        if (view != null) {//存在
            return new SimpleViewHolder(view);
        }
        return null;
    }

    /**
     * 获取头部集合的大小
     */
    private int getHeadersCount() {
        return mHeaderViews.size();
    }

    /**
     * 获取尾部集合的大小
     */
    private int getFootersCount() {
        return mFootViews.size();
    }

    /**
     * 获取adapter的大小
     */
    private int getRealItemCount() {
        return mRealItemCount;
    }

    private View getHeadByType(int viewType){
        int typeOffset=viewType-BASE_ITEM_TYPE_HEADER;
        if(typeOffset>=0 && typeOffset<=mHeaderViews.size()){
            return mHeaderViews.get(typeOffset).view;
        }
        return null;
    }

    private View getFootByType(int viewType){
        int typeOffset=viewType-BASE_ITEM_TYPE_FOOTER;
        if(typeOffset>=0 && typeOffset<=mFootViews.size()){
            return mFootViews.get(typeOffset).view;
        }
        return null;
    }

    private int getHeadType(int pos){
        return BASE_ITEM_TYPE_HEADER+pos;
    }

    private int getFootType(int pos){
        return BASE_ITEM_TYPE_FOOTER+pos;
    }

    private void setRealItemCount(int items){
        if(items>=0){
            mRealItemCount=items;
        }else{
            mRealItemCount = 0;
            LogUtils.e("recylcer view item set error!");
        }
    }

    private boolean isEmptyShow(){ return getRealItemCount()==0 && mEmptyView!=null;}
    private boolean isHeaderViewPos(int position) { return position < getHeadersCount();    }
    private boolean isEmptyViewPos(int position) {return isEmptyShow() && position==getHeadersCount();}
    private boolean isFooterViewPos(int position) {
        return position >= getHeadersCount() + getRealItemCount() + getEmptyCount();
    }
    private int getEmptyCount(){ return isEmptyShow()?1:0; }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        // 解决网格布局问题
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if ( isItemSpecial(mRealItemCount,position)) {
                        return gridLayoutManager.getSpanCount();
                    } else{
                        return 1;
                    }
                }
            });
        }
    }

//    /**
//     * 解决 StaggeredGridLayoutManager样式的加头部问题,暂时没用
//     * @param holder
//     */
//    @Override
//    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
//        mInnerAdapter.onViewAttachedToWindow(holder);
//        int position = holder.getLayoutPosition();
//        if (isHeaderViewPos(position) || isFooterViewPos(position)) {
//            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
//
//            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
//
//                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
//
//                p.setFullSpan(true);
//            }
//        }
//    }

    private class SimpleViewHolder extends RecyclerView.ViewHolder {
        SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }
}
