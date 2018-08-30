package cn.jcyh.peephole.http;

import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import cn.jcyh.peephole.utils.ParseJsonUtil;
import cn.jcyh.peephole.utils.L;

/**
 * Created by jogger on 2018/1/25.
 * 列表数据的请求
 */
@SuppressWarnings("unchecked")
class HttpTaskList implements Runnable {
    private IHttpService mHttpService;
    private Gson mGson;
    private Handler mHandler;

    <M, T> HttpTaskList(String url, Map<String, Object> params, final Class<M> clazz, final IDataListener<T> listener) {
        mGson = new Gson();
        mHandler = new Handler();
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        mHttpService.setParams(params);
        mHttpService.setHttpListener(new IHttpListener() {
            @Override
            public void onSuccess(final String result) {
                L.e("-------result:" + result);
                if (listener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int code = ParseJsonUtil.getErrorCode(result);
                            String desc = ParseJsonUtil.getErrorDesc(result);
                            if (code == 200) {
                                final HttpResult httpResult = new HttpResult();
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    JSONArray list_array = jsonObject.getJSONArray("data");
                                    ArrayList<M> list = new ArrayList<>();
                                    JsonArray array = new JsonParser().parse(list_array.toString()).getAsJsonArray();
                                    for (final JsonElement elem : array) {
                                        list.add(mGson.fromJson(elem, clazz));
                                    }
                                    httpResult.setData(list);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                listener.onSuccess((T) httpResult.getData());
                            } else {
                                listener.onFailure(code, desc);
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
