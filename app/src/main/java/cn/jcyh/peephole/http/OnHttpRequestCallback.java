package cn.jcyh.peephole.http;

/**
 * Created by jogger on 2018/1/10.
 */

public interface OnHttpRequestCallback<T> {
    void onFailure();

    void onSuccess(T t);
}
