package cn.jcyh.peephole.http;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.jcyh.peephole.constant.Config;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.SystemUtil;
import cn.jcyh.peephole.utils.Util;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by jogger on 2018/1/25.
 */

public class JsonHttpService implements IHttpService {
    private IHttpListener mHttpListener;
    private String mUrl;
    private Map<String, Object> mParams;
    private OkHttpClient mOkHttpClient;
    private RequestBody mRequestBody;

    @Override
    public void setUrl(String url) {
        this.mUrl = url;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        mParams = params;
    }

    @Override
    public void setRequestBody(RequestBody requestBody) {
        mRequestBody = requestBody;
    }

    JsonHttpService() {
        //声明日志类
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                L.i("----------message:" + message);
            }
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(10, TimeUnit.SECONDS)//设置写入超时时间
                .addInterceptor(httpLoggingInterceptor)
                .build();

    }

    @Override
    public void excute() {
        post();
    }

    @Override
    public <T> void setHttpListener(T listener) {
        mHttpListener = (IHttpListener) listener;
    }

    private void post() {
        try {
//            if (IsHaveInternet()) {
            //创建一个FormBody.Builder
            FormBody.Builder builder = new FormBody.Builder();
            if (mParams != null && mParams.keySet().size() != 0) {
                for (String key : mParams.keySet()) {
                    //追加表单信息
                    builder.add(key, mParams.get(key) + "");
                }
            }
            //生成表单实体对象
            if (mRequestBody == null)
                mRequestBody = builder.build();
            //补全请求地址
            Config.HeaderConfig headerConfig = Config.getHeaderConfig();
            Locale locale = Util.getApp().getResources().getConfiguration().locale;
            String language = locale.getLanguage();
            final Request request = new Request.Builder()
                    .url(mUrl)
                    .post(mRequestBody)
//                                .header("cookie", SharePreUtil.getInstance(mContext).getString("cookie", "no_cookie"))
                    .addHeader("Version", String.valueOf(SystemUtil.getVersionCode()))
                    .addHeader("DeviceId", ControlCenter.getSN())
                    .addHeader("AppKey", headerConfig.getAppkey())
                    .addHeader("Nonce", headerConfig.getNonce())
                    .addHeader("Timestamp", headerConfig.getTimestamp())
                    .addHeader("Sign", headerConfig.getSign())
                    .addHeader("language", language)
                    .build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mHttpListener.onFailure();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
//                                String cookie = response.headers().get("Set-Cookie");
//                                if (!TextUtils.isEmpty(cookie))
//                                    SharePreUtil.getInstance(mContext).setString("cookie", cookie);
//                    final String result = response.body().string();
                    mHttpListener.onSuccess(response.body().string());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
