package cn.jcyh.peephole.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jcyh.peephole.bean.HttpResult;
import cn.jcyh.peephole.bean.User;
import cn.jcyh.peephole.utils.ParseJsonUtil;

/**
 * Created by jogger on 2018/1/10.
 */

public class HttpAction {
    private static HttpAction sHttpAction;
    private ParseJsonUtil mParseJsonUtil;
    private Gson mGson;

    private HttpAction() {
        mParseJsonUtil = ParseJsonUtil.getsParseJsonUtil();
        mGson = new Gson();
    }

    public static HttpAction getHttpAction() {
        if (sHttpAction == null) {
            synchronized (HttpAction.class) {
                if (sHttpAction == null) {
                    sHttpAction = new HttpAction();
                }
            }
        }
        return sHttpAction;
    }

    public void initDoorbell(String sn, final IDataListener<Boolean> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("sn", sn);
        Volley.sendRequest(HttpUrlIble.INIT_DOORBELL_URL, params, HttpResult.class, new IDataListener<HttpResult>() {
            @Override
            public void onSuccess(HttpResult httpResult) {
                if (listener != null) {
                    if (httpResult.getCode() == 200)
                        listener.onSuccess(true);
                    else listener.onFailure(httpResult.getCode());
                }
            }

            @Override
            public void onFailure(int errorCode) {
                if (listener != null)
                    listener.onFailure(errorCode);
            }
        });

    }

    public void getBindUsers(String sn, final IDataListener<List<User>> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("sn", sn);
        Volley.sendRequest(HttpUrlIble.GET_BIND_USERS_URL, params, new IDataListener<HttpResult>() {
            @Override
            public void onSuccess(HttpResult httpResult) {
                if (listener != null) {
                    if (httpResult.getCode() == 200) {
                        Object data = httpResult.getData();
                        TypeToken<List<User>> typeToken = new TypeToken<List<User>>() {
                        };
                        List<User> users = mGson.fromJson(data.toString(), typeToken.getType());
                        listener.onSuccess(users);
                    } else listener.onFailure(httpResult.getCode());
                }
            }

            @Override
            public void onFailure(int errorCode) {
                if (listener != null)
                    listener.onFailure(errorCode);
            }
        });
    }
}

