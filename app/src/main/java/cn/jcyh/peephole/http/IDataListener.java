package cn.jcyh.peephole.http;

/**
 * Created by jogger on 2018/1/25.
 */

public interface IDataListener<M> {
    void onSuccess(M m);

    void onFailure(int errorCode);
}
