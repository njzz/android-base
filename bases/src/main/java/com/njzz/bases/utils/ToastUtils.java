package com.njzz.bases.utils;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class ToastUtils {
    public static void show(Context context, String messages) {
        Toast toast=Toast.makeText(context,messages,Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);//居中显示
        View view = toast.getView();
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
//            view.setTransitionAlpha(128.0f);
//        }
        toast.show();
    }
}
