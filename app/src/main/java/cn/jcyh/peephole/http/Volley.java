package cn.jcyh.peephole.http;

import java.util.Map;
import java.util.concurrent.FutureTask;

import cn.jcyh.peephole.bean.HttpResult;

/**
 * Created by jogger on 2018/1/25.
 */

public class Volley {
    //暴露给调用层请求
    public static <T, M> void sendRequest(String url, T requestBean, Class<M> responseClass, IDataListener<M> listener) {
        IHttpListener httpListener = new JsonHttpListener<>(responseClass, listener);
        HttpTask httpTask = new HttpTask(url, requestBean, httpListener);
        //请求任务丢到请求队列中
        ThreadPoolManager.getThreadPoolManager().excute(new FutureTask<>(httpTask, null));
    }

    public static <M> void sendRequest(String url, Map<String, String> params, Class<M> responseClass, IDataListener<M> listener) {
        IHttpListener httpListener = new JsonHttpListener<>(responseClass, listener);
        HttpTask httpTask = new HttpTask(url, params, httpListener);
        //请求任务丢到请求队列中
        ThreadPoolManager.getThreadPoolManager().excute(new FutureTask<>(httpTask, null));
    }

    public static void sendRequest(String url, Map<String, String> params, IDataListener<HttpResult> listener) {
        IHttpListener httpListener = new JsonHttpListener<>(HttpResult.class, listener);
        HttpTask httpTask = new HttpTask(url, params, httpListener);
        //请求任务丢到请求队列中
        ThreadPoolManager.getThreadPoolManager().excute(new FutureTask<>(httpTask, null));
    }

}
