package cn.jcyh.peephole.http;

import java.util.HashMap;
import java.util.Map;

import cn.jcyh.peephole.bean.HttpResult;
import cn.jcyh.peephole.utils.ParseJsonUtil;
import timber.log.Timber;

/**
 * Created by jogger on 2018/1/10.
 */

public class HttpAction {
    private static HttpAction sHttpAction;
    private ParseJsonUtil mParseJsonUtil;

    private HttpAction() {
        mParseJsonUtil = ParseJsonUtil.getsParseJsonUtil();
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
        Volley.sendRequest(HttpUrlIble.INIT_DOORBELL_URL, params, new IDataListener<HttpResult>() {
            @Override
            public void onSuccess(HttpResult httpResult) {
                Timber.e("------->httpres:" + httpResult);
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

}
