package cn.jcyh.peephole.http;

import android.os.Handler;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.ParseJsonUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by jogger on 2018/1/25.
 * 普通对象的请求
 */
@SuppressWarnings("unchecked")
class HttpTask implements Runnable {
    //含有请求服务器的接口引用
    private IHttpService mHttpService;
    private static Handler sHandler;
    private Gson mGson;


    <T> HttpTask(String url, final Class<T> clazz, String json, final IDataListener listener) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        if (sHandler == null)
            sHandler = new Handler();
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        mHttpService.setRequestBody(body);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                if (listener != null) {
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int code = ParseJsonUtil.getErrorCode(result);
                            String desc = ParseJsonUtil.getErrorDesc(result);
                            if (code == 200) {
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    mGson = new Gson();
                                    T t = mGson.fromJson(data.toString(), clazz);
                                    listener.onSuccess(t);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(-1, "");
                        }
                    });

            }
        });
    }

    <T> HttpTask(final String url, final Map<String, Object> params, final Class<T> clazz, final IDataListener listener) {
        if (sHandler == null)
            sHandler = new Handler();
//        final Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        mHttpService.setParams(params);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                if (listener != null) {
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int code = ParseJsonUtil.getErrorCode(result);
                            String desc = ParseJsonUtil.getErrorDesc(result);
                            if (code == 200) {
//                                final HttpResult httpResult = new HttpResult();
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    mGson = new Gson();
                                    T t = mGson.fromJson(data.toString(), clazz);
                                    listener.onSuccess(t);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    L.e("--------->e:"+e.getMessage());
                                    listener.onFailure(code, desc);
                                }

                            } else {
                                listener.onFailure(code, desc);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure() {
                if (listener != null) {
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(-1, "");
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
