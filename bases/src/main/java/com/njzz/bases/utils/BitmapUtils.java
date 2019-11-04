package com.njzz.bases.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BitmapUtils {

    //计算图片
    private static int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);
            inSampleSize = Math.max(widthRadio, heightRadio);
        }
        return inSampleSize;
    }

    //从文件加载图片
    public static Bitmap bitmapFromFile(String str, Size size) {
        Bitmap bitmap = null;
        if (!Utils.emptystr(str)) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
                bitmap = BitmapFactory.decodeFile(str, options);
                options.inSampleSize = caculateInSampleSize(options,size.getWidth(),size.getHeight());//

                options.inPreferredConfig = Bitmap.Config.RGB_565;
                /* 下面两个字段需要组合使用 */
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(str, options);
            } catch (OutOfMemoryError e) {
                LogUtils.e( "decode bitmap OutOfMemoryError");
                e.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
                File ft=new File(str);
                if(ft.exists()) ft.delete();
            }
        }
        return bitmap;
    }

    // bScal 是否缩放
    public static Bitmap bitmapFromFile(String str, boolean bScal) {
        Bitmap bmp = null;
        if (!Utils.emptystr(str)) {
            BitmapFactory.Options bfoOptions = new BitmapFactory.Options();
            bfoOptions.inScaled = bScal;
            bmp = BitmapFactory.decodeFile(str, bfoOptions);//获取bitmap
        }
        return bmp;
    }

    //从资源加载图片
    public static Bitmap bitmapFromRes(Context context, int resid) {
        return bitmapFromRes(context,resid,true);
    }

    // bScal 是否缩放
    public static Bitmap bitmapFromRes(Context context, int resid, boolean bScal) {
        Bitmap bmp = null;
        if (context != null) {
            BitmapFactory.Options bfoOptions = new BitmapFactory.Options();
            bfoOptions.inScaled = bScal;
            bmp = BitmapFactory.decodeResource(context.getResources(), resid, bfoOptions);//获取bitmap
        }
        return bmp;
    }

    /**
     *获取bitmap 大小
     */
    public static int getBitmapSize(Bitmap bitmap){
        if (bitmap == null) {
            return 0;
        }
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //api 19
        return bitmap.getAllocationByteCount();
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){ //api 12
//            return bitmap.getByteCount();
//        }
//        return bitmap.getRowBytes() * bitmap.getHeight(); //other version
    }

    /**
     * @Desc 将bitmap转化为byte[]类型也就是转化为二进制
     */
    public static byte[] toByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * 根据图片路径，把图片转为byte数组
     * @param imgSrc  图片路径
     * @return      byte[]
     */
    public static byte[] toBytes(String imgSrc)
    {
        FileInputStream fin;
        byte[] bytes = null;
        try {
            fin = new FileInputStream(new File(imgSrc));
            bytes  = new byte[fin.available()];
            //将文件内容写入字节数组
            fin.read(bytes);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytes;
    }

    /**
     * @Desc 将二进制转化为bitmap
     */
    public static Bitmap fromByte(byte[] temp) {
        if (temp != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
            return bitmap;
        } else {
            return null;
        }
    }
}
