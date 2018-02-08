package cn.jcyh.peephole.http;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Created by jogger on 2018/1/25.
 */

public class JsonHttpListener<M> implements IHttpListener {
    private Class<M> mResponseClass;
    private IDataListener<M> mDataListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Gson mGson;

    public JsonHttpListener(Class<M> responeseClass, IDataListener<M> jsonListener) {
        this.mResponseClass = responeseClass;
        this.mDataListener = jsonListener;
        mGson = new Gson();
    }

    @Override
    public void onSuccess(InputStream inputStream) {
        String content = getContent(inputStream);
        Timber.e("----------content:"+content);
        final M response = mGson.fromJson(content, mResponseClass);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDataListener.onSuccess(response);
            }
        });
    }


    @Override
    public void onFailure() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDataListener.onFailure(-1);
            }
        });
    }

    private String getContent(InputStream inputStream) {
//        String content = null;
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            StringBuilder sb = new StringBuilder();
//            String line;
//            try {
//                while ((line = reader.readLine()) != null) {
//                    sb.append(line + "\n");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e("JsonHttpListener", "Error=" + e.toString());
//            } finally {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.e("JsonHttpListener", "Error=" + e.toString());
//                }
//            }
//            return sb.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return content;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int ret;
            while ((ret = inputStream.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, ret);
            }
            baos.flush();
            return baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
