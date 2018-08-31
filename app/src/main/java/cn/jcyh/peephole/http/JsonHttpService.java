package cn.jcyh.peephole.http;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.jcyh.peephole.constant.Config;
import cn.jcyh.peephole.utils.L;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jogger on 2018/1/25.
 */

public class JsonHttpService implements IHttpService {
    private IHttpListener mHttpListener;
    private String mUrl;
    private Map<String, Object> mParams;
    private OkHttpClient mOkHttpClient;

    @Override
    public void setUrl(String url) {
        this.mUrl = url;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        mParams = params;
    }

    JsonHttpService() {
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(10, TimeUnit.SECONDS)//设置写入超时时间
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
            RequestBody formBody = builder.build();
            //补全请求地址
            Config.HeaderConfig headerConfig = Config.getHeaderConfig();
            final Request request = new Request.Builder()
                    .url(mUrl)
                    .post(formBody)
//                                .header("cookie", SharePreUtil.getInstance(mContext).getString("cookie", "no_cookie"))
                    .addHeader("AppKey", headerConfig.getAppkey())
                    .addHeader("Nonce", headerConfig.getNonce())
                    .addHeader("Timestamp", headerConfig.getTimestamp())
                    .addHeader("Sign", headerConfig.getSign())
                    .build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    L.e("---------onFailure" + e.getMessage() + ":" + mUrl);
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
            L.i("---error" + e);
        }
    }
}
