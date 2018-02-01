package cn.jcyh.peephole.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseJsonUtil {
    private static ParseJsonUtil sParseJsonUtil;

    private ParseJsonUtil() {
    }

    public static ParseJsonUtil getsParseJsonUtil() {
        if (sParseJsonUtil == null) {
            synchronized (ParseJsonUtil.class) {
                if (sParseJsonUtil == null) {
                    sParseJsonUtil = new ParseJsonUtil();
                }
            }
        }
        return sParseJsonUtil;
    }

    //整体状态
    public static String praseJson(String str) {
        String status = null;
        try {
            JSONObject json = new JSONObject(str);
            status = json.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;
    }

    public static String getErrorCode(String str) {
        String code = "";
        try {
            JSONObject json = new JSONObject(str);
            code = json.getString("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }
}
