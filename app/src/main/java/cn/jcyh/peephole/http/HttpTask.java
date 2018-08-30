package cn.jcyh.peephole.http;

import android.os.Handler;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.ParseJsonUtil;

/**
 * Created by jogger on 2018/1/25.
 * 普通对象的请求
 */
@SuppressWarnings("unchecked")
class HttpTask implements Runnable {
    //含有请求服务器的接口引用
    private IHttpService mHttpService;
    private Handler mHandler;

    /**
     * 上传文件
     */

    HttpTask(String url, File file, IHttpListener httpListener) {
//        mHttpService = new UploadHttpService();
//        mHttpService.setUrl(url);
//        //设置处理结果的接口
//        mHttpService.setHttpListener(httpListener);
//        ((UploadHttpService) mHttpService).setFileName(file.getName());
//        ((UploadHttpService) mHttpService).setFilePath(file.getAbsolutePath());
    }

    <T> HttpTask(final String url, final Map<String, Object> params, final Class<T> clazz, final IDataListener listener) {
        mHandler = new Handler();
//        final Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        mHttpService.setParams(params);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                if (listener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            L.e("----------------result:"+result+":"+url);
                            int code = ParseJsonUtil.getErrorCode(result);
                            String desc = ParseJsonUtil.getErrorDesc(result);
                            if (code == 200) {
//                                final HttpResult httpResult = new HttpResult();
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    T t = new Gson().fromJson(data.toString(), clazz);
                                    listener.onSuccess(t);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                listener.onFailure(code, desc);
                                L.e("-------result:" + result + url);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure() {
                if (listener != null) {
                    mHandler.post(new Runnable() {
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
