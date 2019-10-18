package com.njzz.bases.common;

import android.app.Activity;

import java.util.List;

/**
 * 涉及到生命周期管理的基类，需要移除回调
 */
public abstract class LifeBasedClass {
    LifeBasedClass() {
        GlobalNotice.setListener(SysNotice.ACTIVITY_DESTROY, new SignalSlot.Slot(null) {
            @Override
            public void onSignal(int what, int arg1, int agr2, Object argObj) {
                onActivityLifeEnd((Activity) argObj);
            }
        });
    }

    protected abstract void onActivityLifeEnd(Activity activity);

    protected void listProcess(List<?extends LifeAble> list, Activity activity){
        if(list!=null && activity!=null ) {
            for (int i = 0; i < list.size(); ) {
                if(activity.equals(list.get(i).getAttached())){
                    list.remove(i);
                }else{
                    ++i;
                }
            }
        }
    }

}
