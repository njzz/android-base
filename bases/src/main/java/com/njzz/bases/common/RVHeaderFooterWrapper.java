package com.njzz.bases.common;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//适配器包裹类，实现头部控件，尾部控件，空控件
public class RVHeaderFooterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int EMPTY_ITEM_TYPE=999998;//空显示控件，定义为 (真实数据为空时显示，如果有头控件，显示在头控件后，如果有尾控件，显示在尾控件前)
    private static final int FIRST_ITEM_TYPE_HEADER = 999999;//第一个view 的type，只有一个
    private static final int BASE_ITEM_TYPE_HEADER = 1000000;//头部控件type,10000~19999
    private static final int BASE_ITEM_TYPE_FOOTER = 2000000;//尾部控件type,20000~29999
    private static final int LAST_ITEM_TYPE_FOOTER = 3000000;//末尾控件
    //头集合 尾集合
    private View mEmptyView;//空控件
    private SparseArray<View> mHeaderViews = new SparseArray<>();//头部集合，包括第一个
    private SparseArray<View> mFootViews = new SparseArray<>();//尾部集合，包括最后一个

    //数据适配器
    private RecyclerView.Adapter mInnerAdapter;


    /**
     * 把传进来的adapter赋值给成员变量
     * @param adapter
     */
    public RVHeaderFooterWrapper(RecyclerView.Adapter adapter) {
        mInnerAdapter = adapter;
    }

    public <T extends RecyclerView.Adapter> T getInner(){
        if(mInnerAdapter==null)
            return null;
        return (T)mInnerAdapter;
    }

    private boolean isHeaderViewPos(int position) {
        return position < getHeadersCount();
    }

    private boolean isEmptyViewPos(int position) {return isEmptyShow() && position==getHeadersCount();}

    private boolean isFooterViewPos(int position) {
        return position >= getHeadersCount() + getRealItemCount() + getEmptyCount();
    }
    private boolean isEmptyShow(){
        return getRealItemCount()==0 && mEmptyView!=null;
    }
    private int getEmptyCount(){
        if(isEmptyShow())
            return 1;
        return 0;
    }

    /**
     * 添加头部方法
     * @param view
     */
    public void addFirstView(View view){//确保是添加到第一个，只能有一个
        mHeaderViews.put(FIRST_ITEM_TYPE_HEADER, view);
    }

    public void addHeaderView(View view) {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view);
    }

    public void removeHeadView(View view){
        int index=mHeaderViews.indexOfValue(view);
        if(index>=0)
            mHeaderViews.removeAt(index);
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
    public void addLastView(View view){//确保是添加到最后一个，只能有一个
        mFootViews.put(LAST_ITEM_TYPE_FOOTER, view);
    }

    public void addFootView(View view) {
        mFootViews.put(mFootViews.size() + BASE_ITEM_TYPE_FOOTER, view);
    }

    public void removeFootView(View view){
        int index=mFootViews.indexOfValue(view);
        if(index>=0)
            mFootViews.removeAt(index);
    }

    /**
     * 获取头部集合的大小
     */
    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    /**
     * 获取尾部集合的大小
     */
    public int getFootersCount() {
        return mFootViews.size();
    }

    /**
     * 获取adapter的大小
     */
    public int getRealItemCount() {
        return mInnerAdapter.getItemCount();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=mHeaderViews.get(viewType);
        if (view != null) {

//            ViewHolder holder = ViewHolder.createViewHolder(parent.getContext(), mHeaderViews.get(viewType));
//            return holder;
            return new SimpleViewHolder(view);

        } else if ((view=mFootViews.get(viewType)) != null) {
//            ViewHolder holder = ViewHolder.createViewHolder(parent.getContext(), mFootViews.get(viewType));
//            return holder;
            return new SimpleViewHolder(view);
        }else if(viewType==EMPTY_ITEM_TYPE){
            return new SimpleViewHolder(mEmptyView);
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }


    private int getHeadType(int pos){
        int indexStart=mHeaderViews.indexOfKey(FIRST_ITEM_TYPE_HEADER);
        if(pos==0 && indexStart!=-1){//如果是第0个，并且存在first
            return FIRST_ITEM_TYPE_HEADER;
        }

        if(indexStart>=pos){//如果有first,并且first在pos后
            pos-=1;
        }

        return mHeaderViews.keyAt(pos);
    }

    private int getFootType(int pos){
        int indexLast=mFootViews.indexOfKey(LAST_ITEM_TYPE_FOOTER);//确定最后一个的位置
        if(pos==mFootViews.size()-1 && indexLast!=-1){//如果是最后一个，并且存在last
            return LAST_ITEM_TYPE_FOOTER;
        }

        if(indexLast!=-1 && indexLast<=pos){//如果有last,并且last在pos前
            pos+=1;
        }

        return mFootViews.keyAt(pos);
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewPos(position)) {
            return getHeadType(position);
        }else if(isEmptyViewPos(position)){
            return EMPTY_ITEM_TYPE;
        }else if (isFooterViewPos(position)) {
            return getFootType(position - getHeadersCount() - getRealItemCount() -getEmptyCount() );
        }

        return mInnerAdapter.getItemViewType(position - getHeadersCount() -getEmptyCount());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderViewPos(position) || isEmptyViewPos(position) || isFooterViewPos(position)) {
            return;
        }

        mInnerAdapter.onBindViewHolder(holder, position - getHeadersCount() - getEmptyCount());
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + getFootersCount() + getRealItemCount() + getEmptyCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mInnerAdapter.onAttachedToRecyclerView(recyclerView);

        // 解决网格布局问题
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType = getItemViewType(position);
                    if (mHeaderViews.get(viewType) != null || viewType==EMPTY_ITEM_TYPE || mFootViews.get(viewType) != null) {
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
