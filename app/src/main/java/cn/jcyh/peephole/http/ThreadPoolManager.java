package cn.jcyh.peephole.http;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jogger on 2018/1/25.
 */

public class ThreadPoolManager {
    private static ThreadPoolManager sThreadPoolManager;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private LinkedBlockingDeque<Future<?>> mRequestQueue = new LinkedBlockingDeque<>();//请求队列

    private ThreadPoolManager() {
        mThreadPoolExecutor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4));
        mThreadPoolExecutor.execute(mRunnable);
        mThreadPoolExecutor.setRejectedExecutionHandler(new RejectedHandler());
    }

    public static ThreadPoolManager getThreadPoolManager() {
        if (sThreadPoolManager == null) {
            synchronized (ThreadPoolManager.class) {
                if (sThreadPoolManager == null) {
                    sThreadPoolManager = new ThreadPoolManager();
                }
            }
        }
        return sThreadPoolManager;
    }

    /**
     * 给外部调用
     */
    public <T> void excute(FutureTask<T> futureTask) {
        if (futureTask != null) {
            try {
                mRequestQueue.put(futureTask);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                FutureTask futureTask = null;
                try {
                    //在请求队列去取请求，阻塞式
                    futureTask = (FutureTask) mRequestQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //拿到了请求
                if (futureTask != null) {
                    mThreadPoolExecutor.execute(futureTask);
                }
            }
        }
    };

    //拒绝策略
    private class RejectedHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                //将线程池拒绝的网络请求，重新丢到请求队列中
                mRequestQueue.put(new FutureTask<>(r, null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
