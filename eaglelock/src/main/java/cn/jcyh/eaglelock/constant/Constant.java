package cn.jcyh.eaglelock.constant;

/**
 * Created by jogger on 2018/6/9.
 */
public class Constant {
    public static final String CLIENT_ID = "CLIENT_ID";
    public static final String CLIENT_SECRET = "CLIENT_SECRET";
    public static final String IS_FIRST_INTO = "is_first_into";
    public static final String ACCOUNT = "account";
    public static final String PWD = "pwd";
    public static final String AUTO_LOGIN = "auto_login";
    public static final String USER_INFO = "user_info";
    public static final String KEY_LIST = "key_list";
    public static final String LAST_SYNC_DATE = "last_sync_date";

    private final static String PACKAGE_NAME = "cn.jcyh.eaglelock";
    public static final String ACTION_ADD_ADMIN = PACKAGE_NAME + "lock_add_admin";
    public static final String ACTION_UNLOCK = PACKAGE_NAME + "action_unlock";
    public final static String ACTION_BLE_DEVICE = PACKAGE_NAME + ".ACTION_BLE_DEVICE";
    public final static String ACTION_BLE_DISCONNECTED = PACKAGE_NAME + ".ACTION_BLE_DISCONNECTED";
    public static final String ACTION_CUSTOM_PWD = PACKAGE_NAME + "action_custom_pwd";
    public static final String ACTION_RESET_LOCK = PACKAGE_NAME + "action_reset_lock";
    public static final String ACTION_RESET_PWD = PACKAGE_NAME + "action_reset_pwd";//重置密码
    public static final String ACTION_RESET_KEY = PACKAGE_NAME + "action_reset_key";
    public static final String ACTION_DELETE_PWD = PACKAGE_NAME + "action_delete_pwd";
    public static final String ACTION_SET_ADMIN_PWD = PACKAGE_NAME + "action_set_admin_pwd";//管理员键盘密码
    public static final String ACTION_LOCK_IC_CARD = PACKAGE_NAME + "action_lock_ic_card";
    public static final String ACTION_GET_OPERATE_LOG = PACKAGE_NAME + "action_get_operate_log";//开锁日志
    public static final String TYPE_ADD_IC_CARD = "type_add_ic_card";
    public static final String TYPE_DELETE_IC_CARD = "type_delete_ic_card";
    public static final String TYPE_MODIFY_IC_CARD = "type_modify_ic_card";
    public static final String TYPE_CLEAR_IC_CARD = "type_clear_ic_card";
    public static final String ACTION_LOCK_FINGERPRINT = PACKAGE_NAME + "action_lock_fingerprint";
    public static final String TYPE_ADD_FINGERPRINT = "type_add_fingerprint";
    public static final String TYPE_CLEAR_FINGERPRINT = "type_clear_fingerprint";
    public static final String TYPE_DELETE_FINGERPRINT = "type_delete_fingerprint";
    public static final String TYPE_MODIFY_FINGERPRINT = "type_modify_fingerprint";
    public static final String TYPE_COLLECTION_FINGERPRINT = "type_collection_fingerprint";

    public static final String ACTION_LOCK_GET_TIME = PACKAGE_NAME + "action_lock_get_time";
    public static final String ACTION_LOCK_SYNC_TIME = PACKAGE_NAME + "action_lock_sync_time";


    public final static String DEVICE = "device";
    public final static String LOCK_KEY = "lock_key";
    public final static String ERROR_MSG = "error_msg";
    public static final String POSITION = "position";
    public static final String DATE = "date";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String IC_CARD_NUMBER = "ic_card_number";
    public static final String TYPE = "type";
    public static final String FRNO = "FRNO";
    public static final String STATUS = "status";
    public static final String MAX_VALIDATE = "max_validate";
    public static final String VALIDATE = "validate";
    public static final String NAME = "name";
    public static final String PWD_ID = "pwd_id";
    public static final String PWD_INFO = "pwd_info";
    public static final String PWD_RESET_DATA = "pwd_data";
    public static final String PWD_RESET_TIMESTAMP = "pwd_reset_timestamp";
    public static final String UNIQUE_ID = "unique_id";
    public static final String LOCK_ADDRESS = "lock_address";

    public static final String RECORDS = "records";
}
