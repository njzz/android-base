package com.librarys.tools.common;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.librarys.tools.common.ITelephony;

import java.lang.reflect.Method;

public class TelephonyTool {

    /** 获取ITelephony实例对象 */
    public static ITelephony getITelephony(Context context)
    {
        ITelephony itelephony = null;

        try
        {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // 调用TelephonyManager.getITelephony()获取ITelephony实例对象
            // Class c = Class.forName(telephony.getClass().getName());
            Method m = telephony.getClass().getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            itelephony = (ITelephony) m.invoke(telephony);
        }
        catch (Exception ex)
        {

        }

        return itelephony;
    }

    /** 挂断 */
    public static boolean endCall(Context context)
    {
        ITelephony I = getITelephony(context);
        try
        {
            return I.endCall();
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /** 接听 */
    public static void answerRingingCall(Context context)
    {
        ITelephony I = getITelephony(context);
        try
        {
            I.answerRingingCall();
        }
        catch (Exception ex)
        {}
    }

    /** 静音 */
    public static void silenceRinger(Context context)
    {
        ITelephony I = getITelephony(context);
        try
        {
            I.silenceRinger();
        }
        catch (Exception ex)
        {}
    }
}
