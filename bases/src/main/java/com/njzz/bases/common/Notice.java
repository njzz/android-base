package com.njzz.bases.common;

//通告消息
public class Notice {
    public static final int ANY=-1;//关注任意通知
    //系统通知
    public static final int ACTIVITY_CREATE=-2;//activity 创建通知 obj为activity
    public static final int ACTIVITY_DESTROY=-3;//activity 销毁通知 obj 为activity
    public static final int NET_AVAILABLE=-4;//网络可用通知
    public static final int NET_LOST=-5;//网络丢失通知

    //业务通知
    public static final int LOGIN=1;//登录通知
    public static final int LOGOUT=2;//登出通知
}
