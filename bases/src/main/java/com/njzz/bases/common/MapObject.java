package com.njzz.bases.common;

import com.njzz.bases.utils.Utils;

import java.util.HashMap;

/**
 * map 封装
 */
public class MapObject extends HashMap<String,Object> {
    public String getString(String strKey){
        String str=Utils.cast(get(strKey));
        return str==null?"":str;
    }

    public int getInt(String strKey){
        Integer obj=Utils.cast(get(strKey));
        return obj==null?0:obj;
    }

    public long getLong(String strKey){
        Long obj=Utils.cast(get(strKey));
        return obj==null?0:obj;
    }

    public float getFloat(String strKey){
        Float obj=Utils.cast(get(strKey));
        return obj==null?0f:obj;
    }

    public double getDouble(String strKey){
        Double obj=Utils.cast(get(strKey));
        return obj==null?0d:obj;
    }

    public boolean getBoolean(String strKey){
        Boolean obj=Utils.cast(get(strKey));
        return obj==null?false:obj;
    }

    public Object getObject(String strKey){
        return get(strKey);
    }

}
