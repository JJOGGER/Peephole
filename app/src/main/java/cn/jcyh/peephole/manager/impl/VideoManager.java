package cn.jcyh.peephole.manager.impl;

import android.content.Context;

import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatData;

import cn.jcyh.nimlib.config.AVChatConfigs;
import cn.jcyh.peephole.manager.IVideoManager;
import cn.jcyh.peephole.service.video.AVChatControllerCallback;

/**
 * Created by jogger on 2018/8/14.
 */
public class VideoManager implements IVideoManager {
    protected Context mContext;
    protected AVChatData mAvChatData;
    private AVChatCameraCapturer mVideoCapturer;
    private AVChatConfigs mAvChatConfigs;

    public VideoManager(Context context, AVChatData avChatData) {
        mContext = context;
        mAvChatData = avChatData;
        mAvChatConfigs = new AVChatConfigs(context);
    }

    @Override
    public void setLastVideoDate(long lastVideoDate) {

    }

    @Override
    public long getLastVideoDate() {
        return 0;
    }

    @Override
    public void acceptVideo(AVChatControllerCallback<Void> callback) {

    }

    @Override
    public void switchCamera() {

    }

    @Override
    public void hangUp(int type) {

    }

    @Override
    public void onHangUp(int exitCode) {

    }

    @Override
    public AVChatData getAvChatData() {
        return null;
    }
}
