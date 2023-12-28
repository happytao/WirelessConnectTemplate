package com.example.othertemplateapp.threadpool;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: fa.rao@sunyard.com
 * @date: 2020/04/27 10:07
 * @description: 线程派遣类
 */
public class DispatchTask {
    /*
     * Using it for Background Tasks
     */
    public static void doSomeBackgroundWork(Runnable task) {
        ThreadPoolExecutor threadPoolExecutor = DefaultExecutorSupplier.getInstance().forBackgroundTasks();
        Log.d("DispatchTask", "ActiveCount: "+threadPoolExecutor.getActiveCount());
        threadPoolExecutor.execute(task);
    }

    public static <T> Future<T> doSomeBackgroundWork(Callable<T> task) {
        return DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .submit(task);
    }

    public static void doSomeBackgroundWorkSingleThread(Runnable task) {
        DefaultExecutorSupplier.getInstance().forSingleThreadBackgroundTask()
                .execute(task);
    }

    public static Future doSomeBackgroundWorkCancel(Runnable task) {
        return DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .submit(task);
    }

    public static <T> Future<T> doSomeBackgroundWorkCancel(Callable<T> task) {
        return DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .submit(task);
    }
    
    /*
     * Using it for Light-Weight Background Tasks
     */
    public static void doSomeLightWeightBackgroundWork(Runnable task) {
        DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks()
                .execute(task);
    }

    /*
     * Using it for MainThread Tasks
     */
    public static void doSomeMainThreadWork(Runnable task) {
        DefaultExecutorSupplier.getInstance().forMainThreadTasks()
                .execute(task);
    }
    
    /*
     * ThreadSleep
     */
    public static void threadSleep(long millis){
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
