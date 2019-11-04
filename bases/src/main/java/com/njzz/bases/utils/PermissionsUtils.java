package com.njzz.bases.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.SparseArray;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.njzz.bases.common.AppSet;

public class PermissionsUtils {

    public interface Listener {
        /**
         * 通过授权
         * @param permission 权限
         */
        void permissionGranted(String[] permission);
        /**
         * 拒绝授权
         * @param permission 权限
         */
        void permissionDenied(String[] permission);
    }

    private SparseArray<Listener> listenerMap = new SparseArray<>();
    private int requestNum=1;
    private static PermissionsUtils permissionsUtil;

    private static PermissionsUtils ins() {
        if (permissionsUtil == null) {
            synchronized (PermissionsUtils.class) {
                if (permissionsUtil == null) {
                    permissionsUtil = new PermissionsUtils();
                }
            }
        }
        return permissionsUtil;
    }

    /**
     * 申请授权，当用户拒绝时，会显示默认一个默认的Dialog提示用户
     *
     * @param activity 上下文
     * @param listener 监听
     * @param permission 要申请的权限
     */
    public  static void  request(Activity activity, String[] permission, Listener listener) {
        ins().request(activity,listener, permission);
    }

    /**
     *该函数只能由BaseActivity调用
     */
    public static void onResult(int reqID,String [] p,int [] result){
        ins().onRequestResult(reqID,p,result);
    }

    /**
     * 申请授权，当用户拒绝时，可以设置是否显示Dialog提示用户，也可以设置提示用户的文本内容
     *
     * @param activity 请求的activity
     * @param listener 监听回调
     * @param permission 需要申请授权的权限
     */
    private  void request(Activity activity, Listener listener, String[] permission) {

        if (hasPermission(activity, permission)) {
            listener.permissionGranted(permission);//通过授权
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                listener.permissionDenied(permission);
            } else {
                int reqID=0;
                if(listener!=null) {
                    synchronized (this) {
                        reqID=requestNum++;
                        if(requestNum==Integer.MAX_VALUE){
                            requestNum=1;
                        }
                        listenerMap.put(reqID, listener);
                    }
                }
                ActivityCompat.requestPermissions(activity, permission, reqID);
            }
        }
    }

    private void onRequestResult(int requestCode,String[] permissions,int[] grantResults) {
        if(requestCode!=0) {//为 0 没有listener
            Listener listener;
            synchronized (this) {
                listener = listenerMap.get(requestCode);
                if (listener != null)
                    listenerMap.delete(requestCode);
            }

            if (listener != null) {//回调
                if (isGranted(grantResults)) {
                    listener.permissionGranted(permissions);
                } else {
                    listener.permissionDenied(permissions);
                }
            }
        }
    }


    /**
     * 判断权限是否授权
     *
     */
    private  boolean hasPermission(Context context, String [] permissions) {

        if (permissions.length == 0) {
            return false;
        }

        for (String per : permissions) {
            int result = PermissionChecker.checkSelfPermission(context, per);
            if (result != PermissionChecker.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断一组授权结果是否为授权通过
     *
     * @param grantResult
     * @return
     */
    private  boolean isGranted(int [] grantResult) {
        for (int result : grantResult) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 跳转到当前应用对应的设置页面
     */
    private  void gotoSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + AppSet.app().getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AppSet.app().startActivity(intent);
    }

}
