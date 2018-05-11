package cn.jcyh.peephole.utils;

/**
 * Created by jogger on 2018/1/15.
 */

public class ConstantUtil {
    public static final String NIM_ID = "nim_id";
    public static final String NIM_TOKEN = "nim_token";
    public static final String ACCOUNT = "account";
    public static final String DOORBELL_CONFIG="doorbell_config";
    public static final String PWD = "pwd";
    public static final String UID = "uid";//登录猫眼的帐号
    public static final String AUTO_LOGIN = "save_account_pwd";
    public static final String LAST_DOOR_BELL_NO = "last_door_bell_no";
    public static final String DOORBELL_RING_PARAMS = "doorbell_ring_params";
    public static final String DOORBELL_SENSOR_PARAMS = "doorbell_sensor_params";
    public static final String DOORBELL_BIND_USERS="doorbell_bind_users";

    //猫眼动作
    public static final String REQUEST_SWITCH_CAMERA = "requestSwitchCamera";//请求切换摄像头
    public static final String CHANGE_CAMERA = "SwitchCamera";//响应切换摄像头
    public static final String REQUEST_LASTED_PICS_NAME = "requestLastedPicsNames";//请求图片名称
    public static final String LASTED_PICS_NAMES = "LastedPicsNames";//响应图片名称请求
    public static final String REQUEST_MEDIA_FILE = "requestMediaFile";//请求文件传输
    public static final String MEDIA_FILE = "MediaFile";//传输文件响应
    public static final String REQUEST_VIDEO_NAMES = "requestVideoNames";//请求视频名称
    public static final String VIDEO_NAMES = "VideoNames";//响应请求视频名称
    public static final String REQUEST_VIDEO_THUMBNAIL = "requestVideoThumbnail";//请求视频缩略图
    public static final String VIDEO_THUNBNAIL = "VideoThumbnail";//视频缩略图响应

    //猫眼系统回调
    public static final String ACTION_DOORBELL_SYSTEM_EVENT = "cn.jcyh.eagleking.doorbell_system_event";//猫眼系统回调
    public static final String TYPE_DOORBELL_SYSTEM_RING = "type_doorbell_system_ring";//有人按门铃
    public static final String TYPE_DOORBELL_SYSTEM_ALARM = "type_doorbell_system_alarm";
    //anychat操作
    public static final String ACTION_DOORBELL_LOGIN_RESULT = "cn.jcyh.eagleking.doorbell_login_result";//首次登录
    public static final String ACTION_ANYCHAT_LOGIN_RESULT_MSG = "cn.jcyh.eagleking.login_result_msg";//登录anychat
    public static final String ACTION_ANYCHAT_BASE_EVENT = "cn.jcyh.eagleking.base_event";
    public static final String TYPE_ANYCHAT_LOGIN_STATE = "type_anychat_login_state";
    public static final String TYPE_ANYCHAT_ENTER_ROOM = "type_anychat_enter_room";
    public static final String TYPE_ANYCHAT_ONLINE_USER = "type_anychat_online_user";
    public static final String TYPE_ANYCHAT_LINK_CLOSE = "type_anychat_link_close";
    public static final String TYPE_ANYCHAT_USER_AT_ROOM = "type_anychat_user_at_room";


    public static final String ACTION_ANYCHAT_VIDEO_CALL_EVENT = "cn.jcyh.eagleking.video_call_event";
    public static final String TYPE_BRAC_VIDEOCALL_EVENT_REQUEST = "type_brac_videocall_event_request";
    public static final String TYPE_BRAC_VIDEOCALL_EVENT_REPLY = "type_brac_videocall_event_reply";
    public static final String TYPE_BRAC_VIDEOCALL_EVENT_START = "type_brac_videocall_event_start";
    public static final String TYPE_BRAC_VIDEOCALL_EVENT_FINISH = "type_brac_videocall_event_finish";

    public static final String ACTION_ANYCHAT_USER_INFO_EVENT = "cn.jcyh.eagleking.user_info_event";
    public static final String TYPE_ANYCHAT_FRIEND_STATUS = "type_anychat_friend_status";
    public static final String TYPE_ANYCHAT_USER_INFO_UPDATE = "type_anychat_user_info_update";

    public static final String ACTION_ANYCHAT_TRANS_DATA_EVENT = "cn.jcyh.eagleking.trans_data_event";
    public static final String TYPE_ANYCHAT_TRANS_FILE = "type_anychat_trans_file";
    public static final String TYPE_ANYCHAT_TRANS_BUFFER = "type_anychat_trans_buffer";

    public static final String ACTION_ANYCHAT_RECORD_EVENT = "cn.jcyh.eagleking.record_event";
    public static final String TYPE_ANYCHAT_RECORD = "type_anychat_record";
    public static final String TYPE_ANYCHAT_SNAP_SHOT = "type_anychat_snap_shot";
    public static final String SYSTEM_IMEI = "system_imei";
}
