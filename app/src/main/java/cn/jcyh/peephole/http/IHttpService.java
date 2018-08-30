package cn.jcyh.peephole.http;

import java.util.Map;

/**
 * Created by jogger on 2018/1/25.
 */

public interface IHttpService {
    void setUrl(String url);

    void setParams(Map<String, Object> params);

    /**
     *
     */
    void excute();

    <T> void setHttpListener(T listener);
}
