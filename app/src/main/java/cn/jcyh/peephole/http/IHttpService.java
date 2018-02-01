package cn.jcyh.peephole.http;

/**
 * Created by jogger on 2018/1/25.
 */

public interface IHttpService {
    void setUrl(String url);

    /**
     * 请求参数->bytep[]
     */
    void setRequest(byte[] requestData);

    /**
     *
     */
    void excute();

    void setHttpListener(IHttpListener listener);
}
