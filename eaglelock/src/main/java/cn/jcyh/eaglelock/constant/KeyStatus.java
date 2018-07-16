package cn.jcyh.eaglelock.constant;

/**
 * Created by jogger on 2018/5/8.
 */

public class KeyStatus {
    public static final String KEY_ENABLE = "110401";//正常使用
    public static final String KEY_WAIT_RECEIVED = "110402";//待接收
    public static final String KEY_FROZEN = "110405";//已冻结
    public static final String KEY_UNFROZENING = "110406";//解除冻结中
    public static final String KEY_DELETING = "110407";//解除冻结中
    public static final String KEY_DELETED = "110408";//已删除
    public static final String KEY_RESET = "110410";//已重置

    public static String getStatus(String status) {
        switch (status) {
            case KEY_ENABLE:
                return "正常使用";
            case KEY_WAIT_RECEIVED:
                return "待接收";
            case KEY_FROZEN:
                return "已冻结";
            case KEY_DELETED:
                return "已删除";
            case KEY_RESET:
                return "已重置";
        }
        return "";
    }
}
