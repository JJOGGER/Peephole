package cn.jcyh.peephole.http;

import android.os.Handler;

import java.util.Map;

import cn.jcyh.peephole.http.download.DownloadService;
import cn.jcyh.peephole.http.download.ProgressHttpListener;
import cn.jcyh.peephole.utils.ParseJsonUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by jogger on 2018/1/25.
 * 不计较结果的请求
 */
@SuppressWarnings("unchecked")
class HttpTaskVoid implements Runnable {
    private IHttpService mHttpService;
    private Handler mHandler;

    HttpTaskVoid(String url, Map<String, Object> params, final IDataListener listener) {
        mHandler = new Handler();
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        mHttpService.setParams(params);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                if (listener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int code = ParseJsonUtil.getErrorCode(result);
                            String desc = ParseJsonUtil.getErrorDesc(result);
                            if (code == 200) {
                                listener.onSuccess(true);
                            } else {
                                listener.onFailure(code, desc);
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
                            listener.onFailure(-1, "");
                        }
                    });

            }
        });
    }

    HttpTaskVoid(String url, String json, final IDataListener listener) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        mHandler = new Handler();
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        mHttpService.setRequestBody(body);
//        mHttpService.setParams(params);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                if (listener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int code = ParseJsonUtil.getErrorCode(result);
                            String desc = ParseJsonUtil.getErrorDesc(result);
                            if (code == 200) {
                                listener.onSuccess(true);
                            } else {
                                listener.onFailure(code, desc);
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
                            listener.onFailure(-1, "");
                        }
                    });

            }
        });
    }

    /**
     * 上传图片
     */
    HttpTaskVoid(final String url, String filePath, Map<String, Object> params, final IDataListener listener) {
        mHandler = new Handler();
        mHttpService = new UploadService(filePath);
        mHttpService.setUrl(url);
        mHttpService.setParams(params);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                if (listener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int code = ParseJsonUtil.getErrorCode(result);
                            String desc = ParseJsonUtil.getErrorDesc(result);
                            if (code == 200) {
                                listener.onSuccess(true);
                            } else {
                                listener.onFailure(code, desc);
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
                            listener.onFailure(-1, "");
                        }
                    });

            }
        });
    }

    /**
     * 下载
     */
    HttpTaskVoid(final String url, String filePath, ProgressHttpListener progressHttpListener, final IDataListener listener) {
        mHandler = new Handler();
        mHttpService = new DownloadService(filePath, progressHttpListener);
        mHttpService.setUrl(url);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                if (listener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int code = ParseJsonUtil.getErrorCode(result);
                            String desc = ParseJsonUtil.getErrorDesc(result);
                            if (code == 200) {
                                listener.onSuccess(true);
                            } else {
                                listener.onFailure(code, desc);
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
                            listener.onFailure(-1, "");
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
