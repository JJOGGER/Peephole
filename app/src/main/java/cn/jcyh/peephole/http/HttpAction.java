package cn.jcyh.peephole.http;

import android.os.Handler;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.entity.Advert;
import cn.jcyh.peephole.entity.DoorbellParam;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.utils.L;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jogger on 2018/1/10.
 */

public class HttpAction {
    private static HttpAction sHttpAction;
    private Gson mGson;
    private Handler mHandler;//全局处理子线程和M主线程通信
    private OkHttpClient mOkHttpClient;

    private HttpAction() {
        mGson = new Gson();
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(10, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        mHandler = new Handler();
    }

    public static HttpAction getHttpAction() {
        if (sHttpAction == null) {
            synchronized (HttpAction.class) {
                if (sHttpAction == null) {
                    sHttpAction = new HttpAction();
                }
            }
        }
        return sHttpAction;
    }

    public void initDoorbell(String deviceId, final IDataListener<Boolean> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
//        request2(HttpUrlIble.INIT_DOORBELL_URL, params, listener);
        request3(HttpUrlIble.INIT_DOORBELL_URL, params, listener);

    }

    /**
     * 获取猫眼绑定的用户列表
     */
    public void getBindUsers(String deviceId, final IDataListener<List<User>> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        L.e("------deviceId:" + deviceId);
        request2(HttpUrlIble.GET_BIND_USERS_URL, params, User.class, listener);
    }

    /**
     * 设置参数
     *
     * @param deviceId 猫眼id
     * @param type     设置类型 mode/monitor/sensor
     */
    public void setDoorbellParams(String deviceId, String type, DoorbellParam value, final
    IDataListener<Boolean> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        params.put("type", type);
        params.put("value", mGson.toJson(value));
        L.e("-------value:" + mGson.toJson(value));
        request3(HttpUrlIble.DOORBELL_PARAMS_SET_UTL, params, listener);
    }

    public void getDoorbellParams(String deviceId, String type, final
    IDataListener<DoorbellParam> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        params.put("type", type);
        request1(HttpUrlIble.DOORBELL_PARAMS_GET_UTL, params, new IDataListener<HttpResult>() {
            @Override
            public void onSuccess(HttpResult httpResult) {
                DoorbellParam doorbellParam = mGson.fromJson(httpResult.getData().toString(),
                        DoorbellParam.class);
                if (listener != null) {
                    listener.onSuccess(doorbellParam);
                }

            }

            @Override
            public void onFailure(int errorCode) {
                if (listener != null)
                    listener.onFailure(errorCode);
            }
        });
    }

    /**
     * 设置猫眼参数
     */
    public void setDoorbellConfig(String deviceId, DoorbellConfig config, IDataListener<Boolean> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        params.put("config", mGson.toJson(config));
        request3(HttpUrlIble.DOORBELL_SET_CONFIG_URL, params, listener);
    }

    public void getDoorbellConfig(String deviceId, final IDataListener<DoorbellConfig> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        request1(HttpUrlIble.DOORBELL_GET_CONFIG_URL, params, listener);
    }

    /**
     * 广告图
     */
    public void getADPictures(final IDataListener<List<Advert>> listener) {
        request2(HttpUrlIble.DOORBELL_AD_GET_URL, null, Advert.class, listener);
    }

    /**
     * 普通对象请求
     */
    public void request1(String url, Map<String, Object> params, IDataListener listener) {
        HttpTask httpTask = new HttpTask(url, params, listener);
        ThreadPoolManager.getThreadPoolManager().excute(new FutureTask<>(httpTask, listener));
    }

    /**
     * 带列表数据请求
     */
    public <M, T> void request2(String url, Map<String, Object> params, Class<M> clazz, IDataListener<T> listener) {
        HttpTask2 httpTask = new HttpTask2(url, params, clazz, listener);
        ThreadPoolManager.getThreadPoolManager().excute(new FutureTask<>(httpTask, listener));
    }

    /**
     * 不关心返回结果请求
     */
    public void request3(String url, Map<String, Object> params, IDataListener<Boolean> listener) {
        HttpTask3 httpTask = new HttpTask3(url, params, listener);
        ThreadPoolManager.getThreadPoolManager().excute(new FutureTask<>(httpTask, listener));
    }

    public void sendDoorbellImg(String deviceId, int type, String filePath, final IDataListener<Boolean> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        params.put("type", type);
        sendPostImg(HttpUrlIble.UPLOAD_DOORBELL_ALARM_URL, filePath, params, listener);
    }

    private void sendPostImg(final String url, String filePath, final Map<String, Object> params,
                             final IDataListener listener) {
        MediaType type = MediaType.parse("image/jpeg");//"text/xml;charset=utf-8"
        File file = new File(filePath);
        if (!file.exists()) return;
//        RequestBody fileBody = RequestBody.create(type, file);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("img", file.getName(),
                        RequestBody.create(type, file));
        if (params != null && params.keySet().size() != 0) {
            for (String key : params.keySet()) {
                //追加表单信息
                builder.addFormDataPart(key, params.get(key) + "");
            }
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "image/jpeg; charset=utf-8;")
                .post(requestBody)//传参数、文件或者混合，改一下就行请求体就行
                .build();
        L.e("-----------url" + url);
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(200, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(200, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(200, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e("-------onFailure" + e.getMessage());
                L.e("-----------url" + url);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFailure(-1);
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                L.e("--------result:" + result + "-->" + url);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
//                            listener.success(result);
                        }
                    }
                });
            }
        });
    }
}

