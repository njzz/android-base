package com.njzz.bases.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import com.njzz.bases.R;
import com.njzz.bases.utils.PermissionsUtils;
import com.njzz.bases.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseActivity extends AppCompatActivity {

    private static final String PARAM_TRANSFER_KEY="activity_param_transfer";
    protected int mFixScreen=0;//是否固定屏幕 0:不设置  >0 竖屏  <0 横屏
    protected int mEnterAnim,mExitAnim;//进入，退出动画
    protected HashMap<String,Object> mParamSet;

    public BaseActivity(){
        setAnim( R.anim.slide_in_bottom,R.anim.slide_out_bottom);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mFixScreen!=0){//如果固定屏幕
            if(mFixScreen>0){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
            }else{
                setRequestedOrientation(ActivityInfo .SCREEN_ORIENTATION_LANDSCAPE);// 横屏
            }
        }

       Init();
    }

    //处理
    @CallSuper
    public void startActivity(Intent intent) {
        startActivityForResult(intent,0,null);
    }
    @CallSuper
    public void startActivity(Intent intent,Bundle options) {
        startActivityForResult(intent,0);
    }
    @CallSuper
    public void startActivityForResult(Intent intent, int requestCode) {
       startActivityForResult(intent,0,null);
    }
    @CallSuper
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent,requestCode,options);
        overridePendingTransition(mEnterAnim,R.anim.alpha_keep);//mExitAnim);
    }

    /**
     * 传输任意参数版本，仅用于本APP
     */
    @CallSuper
    public void startActivity(Intent intent, Map<String,Object> param) {
        startActivityForResult(intent,param,0);
    }
    @CallSuper
    public void startActivityForResult(Intent intent,Map<String,Object> param , int requestCode) {
        if(param!=null) {
            //生成随机数处理
            String strRand = java.util.UUID.randomUUID().toString();
            intent.putExtra(PARAM_TRANSFER_KEY, strRand);
            //参数设置
            GlobalTransfer.set(strRand, param);
        }
        startActivityForResult(intent,requestCode,null);
    }

    @CallSuper
    public void finish(){
        super.finish();
        overridePendingTransition(/*mEnterAnim*/0,mExitAnim);
        System.gc();
    }

    //oncreate 后初始化
    void Init(){

        //不要自带标题栏
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar!= null)
            supportActionBar.hide();
        //参数处理
        Intent intent = getIntent();
        String strKey = intent.getStringExtra(PARAM_TRANSFER_KEY);
        if(!Utils.emptystr(strKey))
            mParamSet= GlobalTransfer.del(strKey);
    }

    //参数获取
    public <T extends Object> T getParam(String key){
        Object t=null;
        if(mParamSet!=null){
            t=mParamSet.get(key);
        }
        return (T)t;
    }

    //设置切换动画
    @CallSuper
    public void setAnim(int resIDEnter,int resIDExit){
        mEnterAnim=resIDEnter;
        mExitAnim=resIDExit;
    }

    /**
     * 系统状态栏设置
     * @param isFull 当前activity 是否为全屏
     * @param isContentBlack 系统内容(文字,图标)是否为深色
     * @param backgroundColor 系统状态栏颜色，可以透明
     */
    public void initStatusBar(boolean isFull, boolean isContentBlack, int backgroundColor) {
        /*设置状态栏*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int vis=0;
            //View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR：设置状态栏中的文字颜色和图标颜色为深色，不设置默认为白色，需要android系统6.0以上。
            if(isContentBlack)
                vis|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(isFull)
                vis|=View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

            decorView.setSystemUiVisibility(vis);
            //设置状态栏规定的颜色
            getWindow().setStatusBarColor(backgroundColor);
        }
    }



    /**
     * Description：监听收起或者展开评论输入键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {//触摸其他地方，关闭输入框
            hideInputWhenTouchOtherView(this, ev, null);
        }
        return super.dispatchTouchEvent(ev);
    }

    //权限请求回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.onResult(requestCode,permissions,grantResults);
    }

    /**
     * 当点击其他View时隐藏软键盘
     *
     * @param activity
     * @param ev
     * @param excludeViews 点击这些View不会触发隐藏软键盘动作
     */
    public final void hideInputWhenTouchOtherView(Activity activity, MotionEvent ev, List<View> excludeViews) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (excludeViews != null && !excludeViews.isEmpty()) {
                for (int i = 0; i < excludeViews.size(); i++) {
                    if (isTouchView(excludeViews.get(i), ev)) {
                        return;
                    }
                }
            }

            View v = activity.getCurrentFocus();
            if (v!=null && !touchInEdit(v, ev)) {
                InputMethodManager inputMethodManager = (InputMethodManager)
                        activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
    }

    /**
     * Description：判断是否是触碰的View
     */
    public final boolean isTouchView(View view, MotionEvent event) {
        if (view == null || event == null) {
            return false;
        }
        Rect rtT=new Rect();
        view.getGlobalVisibleRect(rtT);
        return rtT.contains((int)event.getRawX(),(int)event.getRawY());
    }

    //是否隐藏该焦点窗口
    public final boolean touchInEdit(View v, MotionEvent event) {
        if (v instanceof EditText) {
            return isTouchView(v, event);
        }
        return false;
    }
}
