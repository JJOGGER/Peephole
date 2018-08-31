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
    String EAGLERKING_IP = "http://mysmartxcateye.9cyh.cn/";

    //    String EAGLERKING_IP = "http://192.168.0.134:8083/";
    String INIT_NIM = EAGLERKING_IP + "Doorbell/CateyeRegister";
    //文件上传
    String UPLOAD_DOORBELL_ALARM_URL = EAGLERKING_IP + "Doorbell/DoorbellAlarm";
    String DOORBELL_SET_NAME_URL = EAGLERKING_IP + "Doorbell/SetDeviceName";
    //设置猫眼配置
    String DOORBELL_SET_CONFIG_URL = EAGLERKING_IP + "Doorbell/Setting";
    String DOORBELL_GET_CONFIG_URL = EAGLERKING_IP + "Doorbell/GetSetting";
    //删除用户
    String DOORBELL_UNBIND_USER_URL = EAGLERKING_IP + "Doorbell/DelDev";

    String DOORBELL_AD_GET_URL = EAGLERKING_IP + "Doorbell/GetPictrue";
    //获取版本
    String DOORBELL_GET_VERSION_URL = EAGLERKING_IP + "Doorbell/GetVersion";
    //上传锁电量
    String DOORBELL_UPLOAD_BATTERY_URL = EAGLERKING_IP + "Doorbell/UploadElectricQuantity";
    //获取banner
    String DOORBELL_GET_BANNERS_URL = EAGLERKING_IP + "Doorbell/GetBanners";
    //获取绑定的用户列表
    String DOORBELL_GET_BIND_USERS_URL = EAGLERKING_IP + "Doorbell/GetDeviceUsers";

    //指定设备的管理员
    String DOORBELL_SET_MANAGER_URL = EAGLERKING_IP + "Doorbell/CateyeManagerSet";

    //检查补丁更新
    String DOORBELL_UPDATE_PATCH_URL = EAGLERKING_IP + "Doorbell/UpdatePatch";
}
