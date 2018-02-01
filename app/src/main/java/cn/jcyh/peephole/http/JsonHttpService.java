package cn.jcyh.peephole.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import timber.log.Timber;

/**
 * Created by jogger on 2018/1/25.
 */

public class JsonHttpService implements IHttpService {
    private IHttpListener mHttpListener;
    private HttpURLConnection mURLConnection;
    private String url;
    private byte[] requestData;


    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setRequest(byte[] requestData) {
        this.requestData = requestData;
    }

    @Override
    public void excute() {
        post();
    }


    @Override
    public void setHttpListener(IHttpListener listener) {
        this.mHttpListener = listener;
    }

    public void post() {
        mURLConnection = null;
        try {
            URL urlConn = new URL(url);
            Timber.e("---请求路径 : " + url);
            mURLConnection = (HttpURLConnection) urlConn.openConnection();
            mURLConnection.setRequestMethod("POST");
            mURLConnection.setDoInput(true);
            mURLConnection.setDoOutput(true);
            mURLConnection.setUseCaches(false);
            mURLConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            mURLConnection.setConnectTimeout(10 * 1000);
//            //添加请求头
//            connection.setRequestProperty("token", token);
            mURLConnection.connect();
            //post数据
            if (requestData != null) {
                OutputStream outputStream = mURLConnection.getOutputStream();
                outputStream.write(requestData);
                outputStream.close();
            }
            //获取返回结果
            if (mURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                String responeStr = readStream();
//                Timber.e("服务端返回的数据：" + responeStr);
                if (mHttpListener != null)
                    mHttpListener.onSuccess(mURLConnection.getInputStream());
            } else {
                int responseCode = mURLConnection.getResponseCode();
                String message = readStream(mURLConnection.getErrorStream());
                Timber.e("---请求失败： code=" + responseCode + ", msg=" + message);
            }
        } catch (Exception e) {
            Timber.e("---请求异常：" + e.toString());
//            e.printStackTrace();
        } finally {
            if (mURLConnection != null) {
                mURLConnection.disconnect();
            }
        }
    }

    private static String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        StringBuffer sb = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}
