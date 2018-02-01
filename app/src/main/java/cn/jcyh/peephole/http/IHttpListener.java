package cn.jcyh.peephole.http;

import java.io.InputStream;

/**
 * Created by jogger on 2018/1/25.
 */

public interface IHttpListener {
    void onSuccess(InputStream inputStream);

    void onFailure();
}
