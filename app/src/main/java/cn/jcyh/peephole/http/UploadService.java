package cn.jcyh.peephole.http;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.jcyh.peephole.constant.Config;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.SystemUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jogger on 2018/7/17.
 */
public class UploadService implements IHttpService {
    private IHttpListener mHttpListener;
    private String mUrl;
    private Map<String, Object> mParams;
    private OkHttpClient mOkHttpClient;
    private String mFilePath;

    UploadService(String filePath) {
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(15, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        mFilePath = filePath;
    }

    @Override
    public void setUrl(String url) {
        this.mUrl = url;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        mParams = params;
    }

    @Override
    public void excute() {
        MediaType type = MediaType.parse("image/jpeg");//"text/xml;charset=utf-8"
        File file = new File(mFilePath);
        if (!file.exists()) return;
//        RequestBody fileBody = RequestBody.create(type, file);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("img", file.getName(),
                        RequestBody.create(type, file));
        if (mParams != null && mParams.keySet().size() != 0) {
            for (String key : mParams.keySet()) {
                //追加表单信息
                builder.addFormDataPart(key, mParams.get(key) + "");
            }
        }
        RequestBody requestBody = builder.build();
        Config.HeaderConfig headerConfig = Config.getHeaderConfig();
        Request request = new Request.Builder()
                .url(mUrl)
                .header("Content-Type", "image/jpeg; charset=utf-8;")
                .addHeader("Version", String.valueOf(SystemUtil.getVersionCode()))
                .addHeader("DeviceId", ControlCenter.getSN())
                .addHeader("AppKey", headerConfig.getAppkey())
                .addHeader("Nonce", headerConfig.getNonce())
                .addHeader("Timestamp", headerConfig.getTimestamp())
                .addHeader("Sign", headerConfig.getSign())
                .post(requestBody)//传参数、文件或者混合，改一下就行请求体就行
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e("-------onFailure" + e.getMessage());
                mHttpListener.onFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                L.e("--------------result:" + result);
                mHttpListener.onSuccess(result);
            }
        });
    }

    @Override
    public <T> void setHttpListener(T listener) {
        mHttpListener = (IHttpListener) listener;
    }
}
