package cn.jcyh.peephole.manager;

import com.netease.nimlib.sdk.avchat.model.AVChatData;

import cn.jcyh.peephole.service.video.AVChatControllerCallback;

/**
 * Created by jogger on 2018/8/14.
 */
public interface IVideoManager {
    void setLastVideoDate(long lastVideoDate);

    long getLastVideoDate();

    void acceptVideo(AVChatControllerCallback<Void> callback);

    void switchCamera();

    void hangUp(int type);

    void onHangUp(int exitCode);

    AVChatData getAvChatData();
}
