package cn.jcyh.peephole.http;


/**
 * Created by jogger on 2017/3/16.
 * 网络请求接口
 */

public interface HttpUrlIble {
    //-----------------url接口--------------------
//    String ANYCHAT_IP = "ihomecn.rollupcn.com";//anychat的Constants类改ihomecn.rollupcn.com
    //119.23.58.28服务器
//    http://mysmart.9cyh.cn/
    String EAGLERKING_IP = "http://192.168.0.127:8083/";//http://119.23.58.28:8088/
    String INIT_DOORBELL_URL = EAGLERKING_IP + "Doorbell/InsertRegistNim";
}
