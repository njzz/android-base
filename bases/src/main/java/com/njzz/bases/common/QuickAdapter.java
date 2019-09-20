package com.njzz.bases.common;

import android.graphics.Bitmap;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public abstract class QuickAdapter<T> extends RecyclerView.Adapter<QuickAdapter.VH>{
    private List<T> mDatas;
    private SparseIntArray mTypeItemSet;//viewtype 和 item 的对应关系
    private SparseIntArray mTypeLayoutSet;//viewtype 和 layout 的对应关系
    //通用type 为 0，自定义的 type 需要大于 0
    public QuickAdapter(List<T> datas,int commonItemlayOut){
        this.mDatas = datas;
        setTypeLayout(0,commonItemlayOut);
    }

    protected int getLayoutId(int viewType){
        if(mTypeLayoutSet!=null){
            int layout=mTypeLayoutSet.get(viewType);
            if(layout!=0)
                return layout;
        }
        assert (false);
        return 0;
    }

    //设置对应的 viewtype 和 layout 对应关系 (多个item可以同一个type)
    //通用type 为 0
    public void setTypeLayout(int type,int layout){
        if(mTypeLayoutSet==null){
            mTypeLayoutSet=new SparseIntArray();
        }
        mTypeLayoutSet.put(type,layout);//设置type和layout对应
    }

    //设置特殊item 的 type  (多个item可以相同的type)
    //通用type 为 0
    public void setSpecItemType(int position,int type){
        if(mTypeItemSet==null){
            mTypeItemSet=new SparseIntArray();
        }
        mTypeItemSet.put(position,type);//设置pos和type对应
    }

    @Override
    public int getItemViewType(int position) {// 瀑布流多布局设置，在这列设置两个不同的item type，以区分不同的布局
        if(mTypeItemSet!=null){
            int type=mTypeItemSet.get(position);
            if(type!=0)
                return type;
        }
        return 0;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return VH.get(parent,getLayoutId(viewType),viewType);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.setCurPos(position);
        convert(holder, mDatas.get(position));
    }

    @Override
    public int getItemCount() { return mDatas.size();}

//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);
//        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
//        if (manager instanceof GridLayoutManager) {//网格模式
//            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
//            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//                @Override
//                public int getSpanSize(int position) {
//                    // 如果当前是footer的位置，那么该item占据整个格子，正常情况下占据1个单元格
//                    return getItemViewType(position) == VIEW_FEET ? gridManager.getSpanCount() : 1;
//                }
//            });
//        }
//    }

    public abstract void convert(VH holder, T data);

    public static class VH extends RecyclerView.ViewHolder{
        private SparseArray<View> mViews;
        private View mConvertView;
        private boolean mIsNewed=true;
        private int mViewType;
        private int mCurPos=-1;//当前pos

        public void setCurPos(int pos){
            mCurPos=pos;
        }
        public int getCurPos(){
            return mCurPos;
        }
        private VH(View v,int type){
            super(v);
            mConvertView = v;
            mViewType=type;
            mViews = new SparseArray<>();
        }

        public static VH get(ViewGroup parent, int layoutId,int viewType){
            View convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new VH(convertView,viewType);
        }

        public boolean isNew(){
            if(mIsNewed){
                mIsNewed=false;
                return true;
            }
            return false;
        }

        public int getViewType(){
            return mViewType;
        }

        public <T extends View> T getView(int id){
            View v = mViews.get(id);
            if(v == null){
                v = mConvertView.findViewById(id);
                mViews.put(id, v);
            }
            return (T)v;
        }

        //获取第N个view
        public <T extends View> T getViewByIndex(int nIndex) {
            View view = null;
            if(mConvertView instanceof ViewGroup){
                ViewGroup ll=(ViewGroup) mConvertView;
                if(nIndex<ll.getChildCount()) {
                    view = ll.getChildAt(nIndex);
                }
            }
            return (T)view;
        }

        public void setText(int id, String value){
            TextView view = getView(id);
            view.setText(value);
        }

        public VH setImageBitmap(int viewId, Bitmap bmp) {
            ImageView iv = getView(viewId);
            if(iv!=null)
                iv.setImageBitmap(bmp);
            return this;
        }

        public VH setClickListener(int viewID,View.OnClickListener c){
            View iv = getView(viewID);
            if(iv!=null)
                iv.setOnClickListener(c);
            return this;
        }
    }
}
