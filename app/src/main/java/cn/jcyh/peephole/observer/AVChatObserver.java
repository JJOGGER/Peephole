package cn.jcyh.peephole.observer;


import android.content.Intent;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.model.AVChatData;

import cn.jcyh.peephole.constant.AVChatExitCode;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ActivityCollector;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.ConfigData;
import cn.jcyh.peephole.service.AVChatService;
import cn.jcyh.peephole.ui.activity.CameraActivity;
import cn.jcyh.peephole.ui.activity.DoorbellLookActivity;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.ServiceUtil;
import cn.jcyh.peephole.utils.Util;
import cn.jcyh.peephole.video.AVChatController;
import cn.jcyh.peephole.video.AVChatProfile;

/**
 * Created by jogger on 2018/7/27.
 */
public class AVChatObserver implements Observer<AVChatData> {
    @Override
    public void onEvent(final AVChatData avChatData) {
        L.e("--------------------AVChatObserver:" + avChatData.getAccount() + ":" + avChatData.getExtra());
        if (PhoneCallStateObserver.getInstance().getPhoneCallState() != PhoneCallStateObserver.PhoneCallStateEnum.IDLE
                || AVChatProfile.getInstance().isAVChatting()
                || AVChatManager.getInstance().getCurrentChatId() != 0) {
            AVChatManager.getInstance().sendControlCommand(avChatData.getChatId(), AVChatControlCommand.BUSY, null);
            return;
        }
        //判断是否超出频率
        ConfigData.VideoConfig videoConfig = ControlCenter.getDoorbellManager().getDoorbellConfig().getVideoConfig();
        long currentTimeMillis = System.currentTimeMillis();
        long lastVideoTime = ControlCenter.getDoorbellManager().getLastVideoTime();
        L.e("-----------通话频率:" + (currentTimeMillis - lastVideoTime) + ":" + videoConfig.getVideoFrequencyLimit());
        if (currentTimeMillis - lastVideoTime < videoConfig.getVideoFrequencyLimit() * 1000) {
            //通话频率太快，超出服务器限制
            AVChatController avChatController = new AVChatController(Util.getApp(), avChatData);
            avChatController.hangUp(AVChatExitCode.FREQUENCY_LIMIT);
            return;
        }
        // 有网络来电打开视频服务
        if (ServiceUtil.isServiceRunning(AVChatService.class)) {
            ServiceUtil.stopService(AVChatService.class);
            return;//避免重复调用
        }
        ActivityCollector.finishActivity(DoorbellLookActivity.class);//先结束相机界面
        ActivityCollector.finishActivity(CameraActivity.class);
        Intent intent = new Intent(Util.getApp(), AVChatService.class);
        intent.putExtra(Constant.AVCHAT_DATA, avChatData);
        Util.getApp().startService(intent);
    }
}
