package cn.jcyh.peephole.http;

import android.os.Handler;

import java.util.Map;

import cn.jcyh.peephole.utils.ParseJsonUtil;
import cn.jcyh.peephole.utils.L;

/**
 * Created by jogger on 2018/1/25.
 * 不计较结果的请求
 */
@SuppressWarnings("unchecked")
class HttpTask3 implements Runnable {
    private IHttpService mHttpService;
    private Handler mHandler;
    HttpTask3(String url, Map<String, Object> params, final IDataListener listener) {
        mHandler=new Handler();
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        mHttpService.setParams(params);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                L.e("-------result:" + result);
                if (listener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int code = ParseJsonUtil.getErrorCode(result);
                            if (code == 200) {
                                listener.onSuccess(true);
                            } else {
                                listener.onFailure(code);
                            }
                        }
                    });

                }
            }

            @Override
            public void onFailure() {
                if (listener != null)
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(-1);
                        }
                    });

            }
        });
    }

    @Override
    public void run() {
        //一定在子线程执行请求
        mHttpService.excute();
    }
}
