package com.librarys.tools.common;

public class NDKTools {

    static {
        try {
            System.loadLibrary("ndktools");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static native String getStringFromNDK(String str1,String str2);
}
