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

    public static int getErrorCode(String str) {
        int code = 0;
        try {
            JSONObject json = new JSONObject(str);
            code = json.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }
}
