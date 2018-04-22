package cn.jcyh.peephole.http;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.jcyh.peephole.bean.DoorbellParam;
import cn.jcyh.peephole.bean.HttpResult;
import cn.jcyh.peephole.bean.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Created by jogger on 2018/1/10.
 */

public class HttpAction {
    private static HttpAction sHttpAction;
    private Gson mGson;
    private Handler mHandler;//全局处理子线程和M主线程通信
    private OkHttpClient mOkHttpClient;
    private Context mContext;

    private HttpAction(Context context) {
        mGson = new Gson();
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(10, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        mHandler = new Handler();
        mContext = context.getApplicationContext();
    }

    public static HttpAction getHttpAction(Context context) {
        if (sHttpAction == null) {
            synchronized (HttpAction.class) {
                if (sHttpAction == null) {
                    sHttpAction = new HttpAction(context);
                }
            }
        }
        return sHttpAction;
    }

    public void initDoorbell(String deviceId, final IDataListener<Boolean> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        request2(HttpUrlIble.INIT_DOORBELL_URL, params, listener);

    }

    /**
     * 获取猫眼绑定的用户列表
     */
    public void getBindUsers(String deviceId, final IDataListener<List<User>> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        Timber.e("------deviceId:" + deviceId);
        request(HttpUrlIble.GET_BIND_USERS_URL, params, new IDataListener<HttpResult>() {
            @Override
            public void onSuccess(HttpResult httpResult) {

                TypeToken<List<User>> typeToken = new TypeToken<List<User>>() {
                };
                List<User> users = mGson.fromJson(httpResult.getData().toString(), typeToken
                        .getType());
                if (listener != null) {
                    listener.onSuccess(users);
                }
            }

            @Override
            public void onFailure(int errorCode) {
                if (listener != null) {
                    listener.onFailure(errorCode);
                }
            }
        });
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
        Timber.e("-------value:" + mGson.toJson(value));
        request2(HttpUrlIble.DOORBELL_PARAMS_SET_UTL, params, listener);
    }

    public void getDoorbellParams(String deviceId, String type, final
    IDataListener<DoorbellParam> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceId);
        params.put("type", type);
        request(HttpUrlIble.DOORBELL_PARAMS_GET_UTL, params, new IDataListener<HttpResult>() {
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

    private void request(final String url, Map<String, Object> params, final
    IDataListener<HttpResult> listener) {
        HttpUtil.getInstance(mContext).sendPostRequest(url, params, new HttpUtil
                .OnRequestListener() {
            @Override
            public void success(String result) {
                Timber.e("-------result:" + result + "-->" + url);
                if (listener != null) {
                    try {
                        HttpResult httpResult = mGson.fromJson(result, HttpResult.class);
                        if (httpResult != null) {
                            if (httpResult.getCode() == 200) {
                                listener.onSuccess(httpResult);
                            } else {
                                listener.onFailure(httpResult.getCode());
                            }
                        }
                    } catch (Exception e) {
                        listener.onFailure(-1);
                    }

                }
            }

            @Override
            public void failure() {
                listener.onFailure(-1);
            }
        });
    }

    private void request2(final String url, Map<String, Object> params, final
    IDataListener<Boolean> listener) {
        HttpUtil.getInstance(mContext).sendPostRequest(url, params, new HttpUtil
                .OnRequestListener() {
            @Override
            public void success(String result) {
                Timber.e("-----------result:" + result + "-->" + url);
                HttpResult httpResult = mGson.fromJson(result, HttpResult.class);
                if (listener != null) {
                    if (httpResult != null) {
                        if (httpResult.getCode() == 200) {
                            listener.onSuccess(true);
                        } else {
                            listener.onFailure(httpResult.getCode());
                        }
                    }
                }
            }

            @Override
            public void failure() {
                if (listener != null)
                    listener.onFailure(-1);
            }
        });
    }

    public void sendPostImg(final String url, String filePath, final Map<String, Object> params,
                            final IDataListener listener) {
        MediaType type = MediaType.parse("image/jpeg");//"text/xml;charset=utf-8"
        File file = new File(filePath);
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
        Timber.e("-----------url" + url);
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Timber.e("-------onFailure" + e.getMessage());
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
                Timber.e("--------result:" + result);
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

