package com.example.othertemplateapp.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.security.KeyPair;
import java.util.HashMap;

public class GsonUtil<T> {
    private static final String TAG = GsonUtil.class.getName();
    private static GsonUtil instance;
    private static Gson gson;

    private GsonUtil() {
        gson = new Gson();
    }

    public static GsonUtil getInstance() {
        if(instance == null) {
            synchronized (GsonUtil.class){
                if(instance == null) {
                    instance = new GsonUtil();
                }
            }
        }
        return instance;
    }

    public <T> String toJson(T obj) {
        try {
            String result = gson.toJson(obj);
            return result;
        } catch (Exception e) {
            Log.e(TAG,"gson序列化失败");
            e.printStackTrace();
            return null;
        }

    }

    public <T> T fromJson(String json,Class<T> cls) {
        try {
            T obj = gson.fromJson(json,cls);
            return ((T) obj);
        } catch (JsonSyntaxException e) {
            Log.e(TAG,"gson反序列化失败");
            e.printStackTrace();
            return null;
        }

    }

    public Gson getGson() {
        return gson;
    }

    public <T> T fromJson(String json, Type type) {
        try {
            T obj = gson.fromJson(json, type);
            return obj;
        } catch (JsonSyntaxException e) {
            Log.e(TAG,"gson反序列化失败");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 反序列化 json到 RSA 密钥对Map
     * @param json
     * @return
     */
    public HashMap<Byte, KeyPair> keyPairFromJsonMap(String json) {
        Type type = new TypeToken<HashMap<Byte, KeyPair>>(){}.getType();
        try {
            HashMap<Byte, KeyPair> mapObj = gson.fromJson(json, type);
            return mapObj;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            Log.e(TAG,"gson反序列化失败");
            return null;
        }
    }

//    public <K,V> HashMap<K,V> fromJsonMap (String json, Class<K> clsK, Class<V> clsV) {
//        try {
//            HashMap<K,V> mapObj = gson.fromJson(json, TypeToken.getParameterized(HashMap.class,clsK,clsV).getType());
//
//            return mapObj;
//        } catch (JsonSyntaxException e) {
//            Log.e(TAG,"gson反序列化失败");
//            e.printStackTrace();
//            return null;
//        }
//
//    }
}
