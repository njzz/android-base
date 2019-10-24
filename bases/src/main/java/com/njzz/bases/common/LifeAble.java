package com.njzz.bases.common;

import android.app.Activity;

public class LifeAble {
    private Activity mActivityBind;
    public LifeAble(Activity activity){
        mActivityBind=activity;
    }
    public Activity getLifeBind(){return mActivityBind;}
}
