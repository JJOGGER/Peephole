package cn.jcyh.peephole.command;

import java.util.List;

import cn.jcyh.peephole.control.ControlCenter;

/**
 * Created by jogger on 2018/8/1.
 */
public class CommandControl {
    public static void sendDoorbellModeParamsResponse(String account, int isSuccess) {
        IMMessageCommandImpl.sendDoorbellModeParamsCommand(account, isSuccess);
    }

    public static void sendDoorbellSensorParamsResponse(String account, int isSuccess) {
        IMMessageCommandImpl.sendDoorbellSensorParamsCommand(account, isSuccess);
    }

    /**
     * 图片请求响应
     */
    public static void sendLastedPicsNamesResponse(String account, String command, int requestNum) {
        IMMessageCommandImpl.sendLastedPicsNamesResponse(account, command, requestNum);
    }

    /**
     * 传输图片
     */
    public static void sendLastedPics(String account, String command, List<String> names) {
        IMMessageCommandImpl.sendLastedPics(account, command, names);
    }

    /**
     * 发送视频文件
     *
     * @param fileName .mp4
     */
    public static void sendLastVideo(String account, String fileName) {
        IMMessageCommandImpl.sendLastVideo(account, fileName);
    }

    /**
     * 绑定猫眼响应指令
     *
     * @param fromAccount 用戶id
     * @param flag        是否接收 1接受 0拒绝 2 已绑定 3网络错误
     */
    public static void sendBindResponse(String fromAccount, String flag) {
        IMMessageCommandImpl.sendBindResponse(fromAccount, ControlCenter.getSN(), flag);
    }

    /**
     * 解锁响应指令
     */
    public static void sendUnlockResponse(String fromAccount) {
        IMMessageCommandImpl.sendUnlockResponse(fromAccount);
    }

    /**
     * 猫眼参数获取响应
     */
    public static void sendDoorbellParamsGetResponse(String account) {
        IMMessageCommandImpl.sendDoorbellParamsGetResponse(account);
    }

    /**
     * 人脸识别参数设置
     */
    public static void sendDoorbellFaceValiParamsResponse(String account, int isSuccess) {
        IMMessageCommandImpl.sendDoorbellFaceValiParamsResponse(account, isSuccess);
    }

    public static void sendMultiVideoResponse(String account, int isSuccess) {
        IMMessageCommandImpl.sendMultiVideoResponse(account, isSuccess);
    }

    /**
     * 创建房间
     */
    public static void sendDoorbellCreateRoomResponse(String account, int isSuccess) {
        IMMessageCommandImpl.sendDoorbellCreateRoomResponse(account, isSuccess);
    }
}
