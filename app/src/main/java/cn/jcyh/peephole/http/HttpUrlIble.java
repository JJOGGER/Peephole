package cn.jcyh.peephole.http;


/**
 * Created by jogger on 2017/3/16.
 * 网络请求接口
 */

public interface HttpUrlIble {
    //-----------------url接口--------------------http://192.168.0.127
//    String ANYCHAT_IP = "ihomecn.rollupcn.com";//anychat的Constants类改ihomecn.rollupcn.com
    //119.23.58.28服务器
//    http://mysmart.9cyh.cn/
    String EAGLERKING_IP = "http://119.23.58.28:8083/";
//    String EAGLERKING_IP = "http://192.168.0.134:8083/";
    String INIT_DOORBELL_URL = EAGLERKING_IP + "Doorbell/InsertRegistNim";
    //获取绑定的用户列表
    String GET_BIND_USERS_URL = EAGLERKING_IP + "Doorbell/GetUsersByImei";
    //设置门铃模式
    String DOORBELL_PARAMS_SET_UTL = EAGLERKING_IP + "Doorbell/Setting";
    //获取门铃模式
    String DOORBELL_PARAMS_GET_UTL = EAGLERKING_IP + "Doorbell/GetSetting";
    //文件上传
    String UPLOAD_DOORBELL_ALARM_URL = EAGLERKING_IP + "Doorbell/DoorbellAlarm";
}
