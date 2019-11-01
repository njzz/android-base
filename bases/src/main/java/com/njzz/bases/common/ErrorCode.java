package com.njzz.bases.common;

public class ErrorCode {
    public static final int SUCCESS=0;//成功

    private static final int ERROR_BASE=-100000;
    public static final int PARAM=ERROR_BASE;//参数错误
    public static final int NOT_FOUND=ERROR_BASE-1;//找不到资源
    public static final int PERMISSION=ERROR_BASE-2;//没有权限
    public static final int CONNECT=ERROR_BASE-3;//连接错误
    public static final int IO=ERROR_BASE-4;//IO错误，读写错误

    public static final int CANCELED =ERROR_BASE-5;//取消
    public static final int ABOART=ERROR_BASE-6;//终止
    public static final int EXIST=ERROR_BASE-7;//已经存在
    public static final int SERVER=ERROR_BASE-8;//服务错误

}
