package cn.jcyh.peephole.http;

/**
 * Created by jogger on 2018/1/10.
 */

public interface OnHttpRequestListener<T> {
    void onCompleted();

    void onError(Throwable e);

    void onNext(T t);
}
