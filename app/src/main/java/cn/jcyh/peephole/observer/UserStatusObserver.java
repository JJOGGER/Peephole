package cn.jcyh.peephole.observer;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;

import cn.jcyh.peephole.utils.L;

/**
 * Created by jogger on 2018/7/26.用户状态
 */
public class UserStatusObserver implements Observer<StatusCode> {
    @Override
    public void onEvent(StatusCode statusCode) {
        if (statusCode == StatusCode.UNLOGIN) {
            //未登录
            L.e("----------未登录");
//            NIMClient.getService(AuthService.class).logout();
        } else if (statusCode == StatusCode.NET_BROKEN) {
            //当前网络不可用
            L.e("----------当前网络不可用");
        } else if (statusCode == StatusCode.CONNECTING) {
            //连接中
            L.e("----------连接中");
        } else if (statusCode == StatusCode.LOGINING) {
            //登录中
            L.e("----------登录中");
        } else if (statusCode == StatusCode.SYNCING) {
            L.e("----------正在同步数据");
        } else if (statusCode == StatusCode.LOGINED) {
            L.e("----------已成功登录");
        } else if (statusCode == StatusCode.KICKOUT) {
            L.e("----------被其他端的登录踢掉");
        } else if (statusCode == StatusCode.KICK_BY_OTHER_CLIENT) {
            L.e("----------被同时在线的其他端主动踢掉");
        } else if (statusCode == StatusCode.FORBIDDEN) {
            L.e("----------被服务器禁止登录");
        } else if (statusCode == StatusCode.VER_ERROR) {
            L.e("----------客户端版本错误");
        } else if (statusCode == StatusCode.PWD_ERROR) {
            L.e("----------用户名或密码错误");
        } else {
            L.e("----------未定义");
        }
    }

}
