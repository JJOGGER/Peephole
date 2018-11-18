package cn.jcyh.peephole.http.download;

import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.jcyh.peephole.http.IHttpListener;
import cn.jcyh.peephole.http.IHttpService;
import cn.jcyh.peephole.utils.L;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jogger on 2018/7/17.
 */
public class DownloadService implements IHttpService {
    private IHttpListener mHttpListener;
    private String mUrl;
    private Map<String, Object> mParams;
    private RequestBody mRequestBody;
    private OkHttpClient mOkHttpClient;
    private String mFilePath;//下载存储路径

    public DownloadService(String filePath, final ProgressHttpListener listener) {
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(60, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(60, TimeUnit.SECONDS)//设置写入超时时间
                //增加拦截器
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response response = chain.proceed(chain.request());
                        return response.newBuilder().body(new ProgressResponseBody(response.body(), listener)).build();
                    }
                }).build();

        mFilePath = filePath;
    }

    private enum DownloadState {
        PENDING,
        DOWNLOADING,
        PAUSE,
        DONE
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
    public void setRequestBody(RequestBody requestBody) {
        mRequestBody = requestBody;
    }

    @Override
    public void excute() {
//        File file = new File(mFilePath);
//        MediaType type = MediaType.parse("multipart/form-data");//"text/xml;charset=utf-8"
////        RequestBody fileBody = RequestBody.create(type, file);
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("img", file.getName(),
//                        RequestBody.create(type, file));
//        if (mParams != null && mParams.keySet().size() != 0) {
//            for (String key : mParams.keySet()) {
//                //追加表单信息
//                builder.addFormDataPart(key, mParams.get(key) + "");
//            }
//        }
//        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(mUrl)
//                .post(requestBody)//传参数、文件或者混合，改一下就行请求体就行
                .build();
        L.e("-------开始下载");
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e("-------onFailure" + e.getMessage());
                mHttpListener.onFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                L.e("----------onResponse");
                if (!response.isSuccessful()) {
                    mHttpListener.onFailure();
                    return;
                }
                int total = Integer.valueOf(response.header("Content-Length"));
                L.e("----------total:" + total);
                //将返回结果转化为流，并写入文件
                File file = new File(mFilePath);
                L.e("------------" + file.exists() + ":" + file.length());
                if (file.exists()) {
                    file.delete();
                }
                int len;
                byte[] buf = new byte[2048];
                InputStream inputStream = response.body().byteStream();
                /**
                 * 写入本地文件
                 */
                L.e("---------filepath:" + mFilePath);
                FileOutputStream fileOutputStream = new FileOutputStream(mFilePath);
                while ((len = inputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, len);
                }
                L.e("----------下载完成");
                mHttpListener.onSuccess("{\"code\":200}");
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
            }
        });
    }

    @Override
    public <T> void setHttpListener(T listener) {
        mHttpListener = (IHttpListener) listener;
    }


    private static String getHeaderFileName(Response response) {
        String dispositionHeader = response.header("Content-Disposition");
        if (!TextUtils.isEmpty(dispositionHeader)) {
            dispositionHeader.replace("attachment;filename=", "");
            dispositionHeader.replace("filename*=utf-8", "");
            String[] strings = dispositionHeader.split("; ");
            if (strings.length > 1) {
                dispositionHeader = strings[1].replace("filename=", "");
                dispositionHeader = dispositionHeader.replace("\"", "");
                return dispositionHeader;
            }
            return "";
        }
        return "";
    }

}
