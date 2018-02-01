package cn.jcyh.peephole.http;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by jogger on 2018/1/25.
 */

 class HttpTask implements Runnable {
    //含有请求服务器的接口引用
    private IHttpService mHttpService;

    /**
     * 对象做参数
     */
     <T> HttpTask(String url, T requestBean, IHttpListener httpListener) {
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        //设置处理结果的接口
        mHttpService.setHttpListener(httpListener);
        if (requestBean != null) {
            String requestInfo = new Gson().toJson(requestBean);
            try {
                mHttpService.setRequest(requestInfo.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    HttpTask(String url, Map<String, String> params, IHttpListener httpListener) {
        mHttpService = new JsonHttpService();
        mHttpService.setUrl(url);
        //设置处理结果的接口
        mHttpService.setHttpListener(httpListener);
        try {
            String requestData = parseParams(params);
            mHttpService.setRequest(requestData.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //一定在子线程执行请求
        mHttpService.excute();
    }

    /**
     * 将map转为字符串
     */
    private static String parseParams(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            stringBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8"));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(entry.getValue() == null ? "" : entry.getValue(), "utf-8"));
            stringBuilder.append("&");
        }
        String str = stringBuilder.toString();
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
}
