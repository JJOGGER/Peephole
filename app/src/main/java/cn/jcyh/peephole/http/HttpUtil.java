package cn.jcyh.peephole.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Created by it on 2017/2/22.
 * 网络请求类
 */

public class HttpUtil {
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final String TAG = "HttpUtil";
    //------------------------------------------
    private static Context mContext;
    private OkHttpClient mOkHttpClient;
    private static HttpUtil mInstance;
    private Handler mHandler;//全局处理子线程和M主线程通信
    private IHttpListener mHttpListener;


    private HttpUtil() {
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(10, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        mHandler = new Handler();
    }

    public static HttpUtil getInstance(Context context) {
        mContext = context.getApplicationContext();
        if (mInstance == null) {
            synchronized (HttpUtil.class) {
                if (mInstance == null) {
                    mInstance = new HttpUtil();
                }
            }
        }
        return mInstance;
    }

    public interface OnRequestListener {
        void success(String result);

        void failure();
    }

    /**
     * post请求
     *
     * @param url      网址
     * @param params   参数
     * @param listener 回调
     */
    public void sendPostRequest(final String url, final Map<String, Object> params, final OnRequestListener listener) {
        try {
            if (IsHaveInternet()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "----URL:" + url);
                        //创建一个FormBody.Builder
                        FormBody.Builder builder = new FormBody.Builder();
                        if (params != null && params.keySet().size() != 0) {
                            for (String key : params.keySet()) {
                                //追加表单信息
                                builder.add(key, params.get(key) + "");
                            }
                        }
                        //生成表单实体对象
                        RequestBody formBody = builder.build();
                        //补全请求地址
                        final Request request = new Request.Builder()
                                .url(url)
                                .post(formBody)
//                                .header("cookie", SharePreUtil.getInstance(mContext).getString("cookie", "no_cookie"))
                                .build();
                        mOkHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Timber.e("---------onFailure");
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (listener != null) {
                                            listener.failure();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
//                                String cookie = response.headers().get("Set-Cookie");
//                                if (!TextUtils.isEmpty(cookie))
//                                    SharePreUtil.getInstance(mContext).setString("cookie", cookie);
                                final String result = response.body().string();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (listener != null) {
                                            mHttpListener.onSuccess(response.body().byteStream());
                                            listener.success(result);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).start();
            } else {
                if (listener != null) {
                    listener.failure();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.i("---error" + e);
        }
    }

    /**
     * get请求
     *
     * @param url      网址
     * @param params   参数
     * @param listener 回调
     */

    public void sendGetRequest(final String url, final Map<String, Object> params, final OnRequestListener listener) {
        if (IsHaveInternet()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder sb = null;
                    if (params != null) {
                        sb = new StringBuilder();
                        sb.append("?");
                        Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, Object> entry = iterator.next();
                            sb.append(entry.getKey() + "=" + entry.getValue());
                            sb.append("&");
                        }
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    //补全请求地址
                    Request request;
                    if (sb != null) {
                        request = new Request.Builder()
                                .url(url + sb.toString())
                                .get()
//                                .header("cookie", SharePreUtil.getInstance(mContext).getString("cookie", "no_cookie"))
                                .build();

                    } else {
                        request = new Request.Builder()
                                .url(url)
                                .get()
//                                .header("cookie", SharePreUtil.getInstance(mContext).getString("cookie", "no_cookie"))
                                .build();
                    }
                    mOkHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) {
                                        listener.failure();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String result = response.body().string();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) {
                                        listener.success(result);
                                    }
                                }
                            });
                        }
                    });
                }
            }).start();
        } else {
            if (listener != null) {
                listener.failure();
            }
        }
    }


    /**
     * 判断是否有网络
     */
    private boolean IsHaveInternet() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager
                .getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    public void sendPostImg(final String url, String filePath, final Map<String, Object> params, final OnRequestListener listener) {
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

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Timber.e("-------onFailure");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.failure();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.success(result);
                        }
                    }
                });
            }
        });
    }

    /**
     * 不带参数下载文件
     */
    public void downloadFile(String url, final String destFilePath, final OnRequestListener listener) {
        Request request = new Request.Builder().url(url).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.failure();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                byte[] buf = new byte[1024];
                int ret;
                final InputStream is = response.body().byteStream();
                File file = new File(destFilePath);
                if (!file.exists())
                    file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                while ((ret = is.read(buf)) != -1) {
                    fos.write(buf, 0, ret);
                }
                fos.flush();
                is.close();
                fos.close();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.success("success");
                        }
                    }
                });

            }
        });
    }
}
