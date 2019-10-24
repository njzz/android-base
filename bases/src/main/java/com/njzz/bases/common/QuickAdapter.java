package com.njzz.bases.common;

import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.recyclerview.widget.RecyclerView;


import com.njzz.bases.utils.Utils;

import java.util.List;

public abstract class QuickAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<T> mDatas;
    private int mCommonItemLayout;
    private SpcialItemWrap mSpcialItem;
    //通用type 为 0，自定义的 type 需要大于 0
    public QuickAdapter(List<T> datas,int commonItemlayOut){
        this.mDatas = datas;
        mCommonItemLayout=commonItemlayOut;
    }

    public void setSpcial(SpcialItemWrap siw){
        mSpcialItem=siw;
    }

    public SpcialItemWrap getSpcial(){
        return mSpcialItem;
    }

    //这个为final ,如果需要设置，复写 getViewType
    @Override
    final public int getItemViewType(int position) {//
        if(mSpcialItem!=null ){
            int itemViewType = mSpcialItem.getItemViewType(mDatas.size(), position);
            if(itemViewType!=-1){
                return itemViewType;
            }
        }
        return getViewType(position);
    }

    //根据pos获得type 默认不复用
    protected int getViewType(int position){
        return  position;
    }
    //根据type获得布局 默认commonItemLayout
    protected int getLayoutId(int viewType){
        return mCommonItemLayout;
    }

    @Override @CallSuper
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mSpcialItem!=null) {
            RecyclerView.ViewHolder rv = mSpcialItem.onCreateViewHolder(parent,viewType);
            if(rv!=null)
                return rv;
        }
        return VH.get(parent,getLayoutId(viewType),viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(mSpcialItem==null || !mSpcialItem.isItemSpecial(mDatas.size(),position)){
            VH h=(VH)holder;
            h.setCurPos(position);
            convert(h, mDatas.get(position));
        }
    }

    @Override @CallSuper
    public int getItemCount() {
        if(mSpcialItem!=null){
            return mSpcialItem.getItemCount(mDatas.size());
        }
        return mDatas.size();
    }
    public abstract void convert(VH holder, T data);

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        // 解决网格布局问题
        if(mSpcialItem!=null){
            mSpcialItem.onAttachedToRecyclerView(recyclerView);
        }
    }


/////////////////////////////////////////////////////////////////通用的viewHolder
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
            return Utils.cast( view );
        }

        //获取第N个T 类型的 view
        public <T extends View> T getTypedIndexView(int nIndex) {
            View view = null;
            int index=0;
            ViewGroup vg=Utils.cast( mConvertView);
            if(vg!=null) {
                if (nIndex < vg.getChildCount()) {
                    for(int i=0;i<vg.getChildCount();++i){//枚举
                        view = vg.getChildAt(i);
                        T vCast= Utils.cast(view);//如果是需要的类型
                        if(vCast!=null ){
                            if(index==nIndex){
                                break;
                            }else{
                                index++;
                            }
                        }
                    }
                }
            }
            return Utils.cast(view);
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
