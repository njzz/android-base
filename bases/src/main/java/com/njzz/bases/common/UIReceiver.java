package com.njzz.bases.common;

import android.app.Activity;

import com.njzz.bases.utils.Utils;

public abstract class UIReceiver extends Receiver {
    public UIReceiver(Activity activity){
        super(activity);
    }

    @Override
    protected void finalAction(MessageSet ms){
        Utils.UIRun(()->super.finalAction(ms));
    }
}
