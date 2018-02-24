package cn.jcyh.peephole.http;

/**
 * Created by jogger on 2018/1/25.
 */

public interface IDataListener<T> {
    void onSuccess(T t);

    void onFailure(int errorCode);
}
