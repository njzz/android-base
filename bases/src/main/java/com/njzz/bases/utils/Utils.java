package com.njzz.bases.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Base64;
import android.view.Display;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class Utils {
    /**
     * @return 获取当前时间戳，秒为单位
     */
    public static String getCurrentTime() {
        long time = System.currentTimeMillis() / 1000;//获取系统时间的10位的时间戳
        return String.valueOf(time);
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @param format
     * @return
     */
    public static String seconds2date(String seconds, String format) {
        if (emptystr(seconds)) {
            return "";
        }
        if (emptystr(format)) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds) * 1000));
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2seconds(String dateStr, String format) {
        if(!emptystr(dateStr)) {
            if (emptystr(format)) {
                format = "yyyy-MM-dd HH:mm:ss";
            }
            try {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                Date date=sdf.parse(dateStr);
                if(date!=null){
                    return String.valueOf(date.getTime() / 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * SHA1摘要
     */
    public static String sha1(String info) {
        byte[] digesta = null;
        try {
            // 得到一个SHA-1的消息摘要
            MessageDigest alga = MessageDigest.getInstance("SHA-1");
            // 添加要进行计算摘要的信息
            alga.update(info.getBytes());
            // 得到该摘要
            digesta = alga.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // 将摘要转为字符串
        String rs = null;
        if (digesta != null) {
            rs = byte2hex(digesta);
        }
        return rs;
    }

    /**
     * byte数组转为字符串
     *
     * @param b
     * @return
     */
    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs.append("0").append(stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString();
    }

    /**
     * MD5摘要
     */
    public static String md5(String string) {
        if (!emptystr(string)) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
                byte[] bytes = md5.digest(string.getBytes());
                StringBuilder result = new StringBuilder();
                for (byte b : bytes) {
                    String temp = Integer.toHexString(b & 0xff);
                    if (temp.length() == 1) {
                        temp = "0" + temp;
                    }
                    result.append(temp);
                }
                return result.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 获取手机型号
     */
    public static String getPhoneModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机Android 版本（4.4、5.0、5.1 …）
     */
    public static String getAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取随机数 产生 [0-max)的随机数
     */
    public static int random(int max) {
        return (int)(Math.random() *max);
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context) {

        try {//获取gprs地址
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            //是wifi地址
        }

        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "" ;
        }
        // return null;
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    //base64编码解码  decode :true 解码   false:编码
    public static String base64(String strSrc, boolean decode) {
        if (!emptystr(strSrc)) {
            if (decode) {//解码
                return new String(Base64.decode(strSrc.getBytes(), Base64.DEFAULT));
            } else {
                return Base64.encodeToString(strSrc.getBytes(), Base64.DEFAULT);
            }
        }

        return "";
    }

    /**
     * 判断一个动态壁纸是否已经在运行
     *
     * @param context          :上下文
     * @param tagetPackageName :要判断的动态壁纸的包名
     * @return true:正在运行，false:未运行
     */
    public static boolean isLiveWallpaperRunning(Context context, String tagetPackageName) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);// 得到壁纸管理器
        WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();// 如果系统使用的壁纸是动态壁纸话则返回该动态壁纸的信息,否则会返回null
        if (wallpaperInfo != null) { // 如果是动态壁纸,则得到该动态壁纸的包名,并与想知道的动态壁纸包名做比较
            String currentLiveWallpaperPackageName = wallpaperInfo.getPackageName();
            if (currentLiveWallpaperPackageName.equals(tagetPackageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @Desc JSON字符串格式化
     */
    public static String jsonFormat(String JSONString) {
        int level = 0;
        //存放格式化的json字符串
        StringBuffer jsonForMatStr = new StringBuffer();
        for (int index = 0; index < JSONString.length(); index++)//将字符串中的字符逐个按行输出
        {
            //获取s中的每个字符
            char c = JSONString.charAt(index);

            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
            }
            //遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c).append("\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c).append("\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return String.valueOf(jsonForMatStr);
    }
    private static String getLevelStr(int level) {
        StringBuilder levelStr = new StringBuilder();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }

    /**
     * @Desc Android判断当前App应用处于前台(可见)或后台(不可见)
     */
    public static boolean isAppForeground(Context mContext) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = null;
        if (activityManager != null) {
            appProcesses = activityManager.getRunningAppProcesses();
        }
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(mContext.getPackageName()) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    //字符串空判断  null , "" ," \r\n\t" 都为空
    public static boolean emptystr(String str) {
        return str == null || str.isEmpty() || str.trim().isEmpty();
    }

    //字符串比较  null 和 "" 相等
    public static boolean equalstr(String str1, String str2) {
        //两个都为空
        boolean e1 = emptystr(str1);
        boolean e2 = emptystr(str2);
        if (e1 && e2) { //都为空
            return true;
        }

        if (e1 || e2) { // 一个为空一个不为空
            return false;
        }

        return str1.equals(str2);//两个都不为空
    }

    //获取activety 屏幕长宽
    public static Point getScreenSize(Activity activity) {
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        return point;
    }

    public static int mapInt(Map<String, Object> map, String key) {
        if (map != null && key != null) {
            Integer i = (Integer) map.get(key);
            if (i != null) return i;
        }
        return 0;
    }

    public static boolean mapBool(Map<String, Object> map, String key) {
        if (map != null && key != null) {
            Boolean b = (Boolean) map.get(key);
            if (b != null) return b;
        }
        return false;
    }

    public static String mapString(Map<String, Object> map, String key) {
        if (map != null && key != null) {
            String s = (String) map.get(key);
            if (s != null) return s;
        }
        return "";
    }

    //数字显示
    @SuppressLint("DefaultLocale")
    public static String NumShow(int num){
        if(num<10000){
            return String.valueOf(num);
        }else {
            float numf=num/10000f;
            return String.format("%.2fw",numf);
        }
    }
}

