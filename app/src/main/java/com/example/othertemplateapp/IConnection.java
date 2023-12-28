package com.example.othertemplateapp;

import android.content.Context;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author xingl.shan
 * @description 用于规范设备连接方式的接口
 * @date 2023/5/5
 **/
public interface IConnection {
    int CONNECT_TIMEOUT = 10_000;


    void init(Context context);

    /**
     * 监听连接事件，同步方法直至连接成功、取消、异常情况出现
     *
     * @return 是否连接成功
     */
    boolean listenConnect();

    /**
     * 停止连接，某一种方式连接成功后，停止其他方式的连接
     */
    void stopConnect();

    void disConnect();

    //获取输出流
    OutputStream getOutputStream();

    //获取输入流
    InputStream getInputStream();

    void setStateListener(StateListener listener);

    interface StateListener {
        void onStateOn();

        void onStateOff();

        void onConnected();

        void onDisconnect();
    }
}
