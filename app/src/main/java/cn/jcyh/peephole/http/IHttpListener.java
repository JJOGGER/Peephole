package cn.jcyh.peephole.http;

/**
 * Created by jogger on 2018/1/25.
 */

public interface IHttpListener {
    void onSuccess(String result);

    void onFailure();
}
