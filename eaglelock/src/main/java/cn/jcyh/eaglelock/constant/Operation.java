package cn.jcyh.eaglelock.constant;

/**
 * 锁的操作
 */
public class Operation {
    /**
     * 添加管理员
     */
    public static final String ADD_ADMIN = "ADD_ADMIN";
    /**
     * 恢复出厂设置
     */
    public static final String RESET_LOCK = "RESET_LOCK";
    /**
     * 车位锁升起（关锁）
     */
    public static final String LOCKCAR_UP = "LOCKCAR_UP";
    /**
     * 车位锁降下（开锁）
     */
    public static final String LOCKCAR_DOWN = "LOCKCAR_DOWN";
    /**
     * 门锁的闭锁指令
     */
    public static final String LOCK = "LOCK";
    /**
     * 校准时间
     */
    public static final String SET_LOCK_TIME = "SET_LOCK_TIME";
    /**
     * 重置键盘密码（初始化）
     */
    public static final String RESET_KEYBOARD_PASSWORD = "RESET_KEYBOARD_PASSWORD";
    /**
     * 重置电子钥匙
     */
    public static final String RESET_EKEY = "RESET_EKEY";
    /**
     * 获取操作日志
     */
    public static final String GET_OPERATE_LOG = "GET_OPERATE_LOG";
    /**
     * 自定义密码
     */
    public static final String CUSTOM_PWD = "CUSTOM_PWD";
    /**
     * 设置删除单个键盘密码
     */
    public static final String DELETE_ONE_KEYBOARDPASSWORD = "DELETE_ONE_KEYBOARDPASSWORD";
    /**
     * 修改键盘密码
     */
    public static final String MODIFY_KEYBOARDPASSWORD = "MODIFY_KEYBOARDPASSWORD";
    /**
     * 获取锁的时间
     */
    public static final String GET_LOCK_TIME = "GET_LOCK_TIME";
    /**
     * 获取设备特征值(用于判断所支持的设备)
     */
    public static final String SEARCH_DEVICE_FEATURE = "SEARCH_DEVICE_FEATURE";
    /**
     * 添加IC卡
     */
    public static final String ADD_IC_CARD = "ADD_IC_CARD";
    /**
     * 查询IC卡号
     */
    public static final String SEARCH_IC_NUMBER = "SEARCH_IC_NUMBER";
    /**
     * 修改IC卡有效期
     */
    public static final String MODIFY_IC_PERIOD = "MODIFY_IC_PERIOD";
    /**
     * 删除IC卡
     */
    public static final String DELETE_IC_CARD = "DELETE_IC_CARD";
    /**
     * 清空IC卡
     */
    public static final String CLEAR_IC_CARD = "CLEAR_IC_CARD";
    /**
     * 添加指纹
     */
    public static final String ADD_FINGERPRINT = "ADD_FINGERPRINT";
    /**
     * 修改指纹有效期
     */
    public static final String MODIFY_FINGERPRINT_PERIOD = "MODIFY_FINGERPRINT_PERIOD";

    /**
     * 删除指纹
     */
    public static final String DELETE_FINGERPRINT = "DELETE_FINGERPRINT";
    /**
     * 清空指纹
     */
    public static final String CLEAR_FINGERPRINTS = "CLEAR_FINGERPRINTS";
    /**
     * 获取设备信息
     */
    public static final String READ_DEVICE_INFO = "READ_DEVICE_INFO";
    /**
     * 设置管理员键盘密码
     */
    public static final String SET_ADMIN_KEYBOARD_PASSWORD = "SET_ADMIN_KEYBOARD_PASSWORD";
    /**
     * 获取锁电量
     */
    public static final String GET_ELECTRIC_QUANTITY = "GET_ELECTRIC_QUANTITY";

    /**
     * 设置删除密码
     */
    public static final String SET_DELETE_PASSWORD = "SET_DELETE_PASSWORD";
    /**
     * 修改锁的名称
     */
    public static final String MODIFY_KEYNAME = "MODIFY_KEYNAME";
    /**
     * 获取锁版本信息
     */
    public static final String GET_LOCK_VERSION_INFO = "GET_LOCK_VERSION_INFO";


}