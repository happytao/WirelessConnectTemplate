package com.example.othertemplateapp.threadpool;

import android.os.Process;

import java.util.concurrent.ThreadFactory;

/**
 * @author: fa.rao@sunyard.com
 * @date: 2020/04/27 9:49
 * @description: 带优先级的线程工厂
 */
public class PriorityThreadFactory implements ThreadFactory {
    private final int mThreadPriority;

    public PriorityThreadFactory(int threadPriority) {
        mThreadPriority = threadPriority;
    }
    
    @Override
    public Thread newThread(Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                runnable.run();
            }
        };
        return new Thread(wrapperRunnable);
    }
}
