package cn.jcyh.peephole.http;

/**
 * Created by jogger on 2018/7/17.
 */
 abstract class BaseHttpAction {
    IHttpRequest mHttpRequest;

    abstract IHttpRequest getHttpRequest();

    BaseHttpAction() {
        mHttpRequest = getHttpRequest();
    }
}
