package cn.jcyh.peephole.http;

import android.os.Handler;

import com.google.gson.Gson;

import java.io.File;
import java.util.Map;

import cn.jcyh.peephole.bean.HttpResult;
import cn.jcyh.peephole.utils.ParseJsonUtil;
import timber.log.Timber;

/**
 * Created by jogger on 2018/1/25.
 * 普通对象的请求
 */
@SuppressWarnings("unchecked")
class HttpTask implements Runnable {
    //含有请求服务器的接口引用
    private IHttpService mHttpService;
    private Gson mGson;
    private Handler mHandler;

    /**
     * 上传文件
     */
    HttpTask(String url, File file, IHttpListener httpListener) {
//        mHttpService = new UploadHttpService();
//        mHttpService.setUrl(url);
//        //设置处理结果的接口
//        mHttpService.setHttpListener(httpListener);
//        ((UploadHttpService) mHttpService).setFileName(file.getName());
//        ((UploadHttpService) mHttpService).setFilePath(file.getAbsolutePath());
    }

    <T> HttpTask(String url, Map<String, Object> params, final IDataListener<T> listener) {
        mGson = new Gson();
        mHandler = new Handler();
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        mHttpService.setParams(params);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                Timber.e("------------result:" + result);
                if (listener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            final int code = ParseJsonUtil.getErrorCode(result);
                            if (code == 200) {
                                final HttpResult<T> httpResult = mGson.fromJson(result, HttpResult.class);
                                listener.onSuccess(httpResult.getData());
                            } else {
                                listener.onFailure(code);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure() {
                if (listener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(-1);
                        }
                    });
                }

            }
        });
    }

    @Override
    public void run() {
        //一定在子线程执行请求
        mHttpService.excute();
    }
}
