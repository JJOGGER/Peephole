package cn.jcyh.peephole.http.download;

/**
 * Created by jogger on 2018/8/22.
 */
public interface ProgressHttpListener {
    void onProgress(long currentBytes, long contentLength, boolean done);
}
