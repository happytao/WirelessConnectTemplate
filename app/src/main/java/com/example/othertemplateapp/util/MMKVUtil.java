package com.example.othertemplateapp.util;

import android.os.Parcelable;

import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MMKVUtil {
    public static volatile MMKVUtil instance;
    private static MMKV mmkv;

    private MMKVUtil() {
        mmkv = MMKV.defaultMMKV();
    }

    public static MMKVUtil getInstance() {
        if (instance == null) {
            synchronized (MMKVUtil.class) {
                if (instance == null) {
                    instance = new MMKVUtil();
                }
            }
        }
        return instance;
    }

    public boolean putString(String key, String value) {
        return mmkv.encode(key, value);
    }

    public boolean putArraylist(String key, ArrayList<String> value) {
        Set<String> stringSet = new HashSet<>(value);
        return mmkv.encode(key, stringSet);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return mmkv.decodeString(key, defaultValue);
    }

    public ArrayList<String> getArraylist(String key) {
        Set<String> stringSet = new HashSet<>();
        stringSet = mmkv.decodeStringSet(key, new HashSet<>());
        return new ArrayList<>(stringSet);
    }

    public boolean putInt(String key, int value) {
        return mmkv.encode(key, value);
    }

    public int getInt(String key, int defValue) {
        return mmkv.decodeInt(key, defValue);
    }

    public boolean putBoolean(String key, boolean value) {
        return mmkv.encode(key, value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mmkv.decodeBool(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean putParcelable(String key, Parcelable obj) {
        return mmkv.encode(key, obj);
    }

    public <T extends Parcelable> T getParcelable(String key, Class<T> tClass) {
        return mmkv.decodeParcelable(key, tClass);
    }
}
