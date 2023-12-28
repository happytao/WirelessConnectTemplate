package com.example.othertemplateapp.threadpool;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * @author: fa.rao@sunyard.com
 * @date: 2020/04/27 9:52
 * @description: 主线程
 */
public class MainThreadExecutor implements Executor {

    private final Handler handler = new Handler(Looper.getMainLooper());
    
    @Override
    public void execute(Runnable command) {
        handler.post(command);
    }
}
