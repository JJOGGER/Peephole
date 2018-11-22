package cn.jcyh.peephole.observer;

import android.content.Intent;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

import cn.jcyh.eaglelock.api.MyLockAPI;
import cn.jcyh.eaglelock.constant.MyLockKey;
import cn.jcyh.eaglelock.constant.Operation;
import cn.jcyh.eaglelock.entity.LockKey;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.command.CommandControl;
import cn.jcyh.peephole.command.IMMessageCommandImpl;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ActivityCollector;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.CommandJson;
import cn.jcyh.peephole.entity.ConfigData;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.DoorbellModelParam;
import cn.jcyh.peephole.entity.DoorbellSensorParam;
import cn.jcyh.peephole.event.NIMMessageAction;
import cn.jcyh.peephole.event.online.OnlineStateEventManager;
import cn.jcyh.peephole.service.MultiAVChatService;
import cn.jcyh.peephole.ui.activity.CameraActivity;
import cn.jcyh.peephole.ui.activity.DoorbellLookActivity;
import cn.jcyh.peephole.utils.GsonUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.ServiceUtil;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.utils.Util;

/**
 * Created by jogger on 2018/7/26.
 */
public class MessageReceiveObserver implements Observer<List<IMMessage>> {

    @Override
    public void onEvent(List<IMMessage> imMessages) {
        for (int i = 0; i < imMessages.size(); i++) {
            IMMessage imMessage = imMessages.get(i);
            if (imMessage.getMsgType() != MsgTypeEnum.text) return;//文本以外的消息暂不处理
            L.e("----->内容：" + imMessages.get(i).getContent() + "，来自：" + imMessages.get(i)
                    .getFromAccount()
                    + ",消息类型：" + imMessages.get(i).getMsgType());
            //指令消息处理
            CommandJson commandJson = null;
            try {
                commandJson = GsonUtil.fromJson(imMessage.getContent(), CommandJson.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            L.i("------commandJson:" + commandJson);
            if (commandJson == null) {
                //服务器指令
                Map<String, Object> remoteExtension = imMessage.getRemoteExtension();
                String s = GsonUtil.toJson(remoteExtension);
                L.i("------------:entries" + s);
                try {
                    commandJson = GsonUtil.fromJson(s, CommandJson.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (commandJson == null)
                    return;
                remoteCommandExec(imMessage, commandJson);
            } else {
                localCommandExec(imMessage, commandJson);
            }

        }

    }

    /**
     * 服务器定义指令
     */
    private void remoteCommandExec(IMMessage imMessage, CommandJson commandJson) {
        NIMMessageAction nimMessageAction = new NIMMessageAction();
        switch (commandJson.getCommand()) {
            case CommandJson.ServerCommand.DOORBELL_BANNER_UPDATE:
                nimMessageAction.setType(CommandJson.ServerCommand.DOORBELL_BANNER_UPDATE);
                break;
            case CommandJson.ServerCommand.DOORBELL_POPUP_UPDATE:
                if (!ControlCenter.getSN().startsWith(Constant.SIYE_SN)) return;//四叶草定制弹窗
                L.e("------------commandJson:" + commandJson);
                nimMessageAction.setType(CommandJson.ServerCommand.DOORBELL_POPUP_UPDATE);
                break;
        }
        nimMessageAction.putExtra(Constant.COMMAND, commandJson);
        nimMessageAction.putExtra(Constant.FROM_ACCOUNT, imMessage.getFromAccount());
        EventBus.getDefault().post(nimMessageAction);
    }

    /**
     * 执行本地指令
     */
    private void localCommandExec(IMMessage imMessage, CommandJson commandJson) {
        NIMMessageAction nimMessageAction = new NIMMessageAction();
        switch (commandJson.getCommandType()) {
            case CommandJson.CommandType.DOORBELL_LASTED_IMG_NAMES_REQUEST:
                //收到近期图片获取请求
                int requestNum = commandJson.getFlag2();//获取请求数
                //从文件中获取响应数量的图片
//                        mControlCenter.sendLastedPicsNamesResponse(dwUserid, commandJson
// .getCommand(),
//                                requestNum);
                break;
            case CommandJson.CommandType.DOORBELL_LASTED_IMG_REQUEST:
                //发送图片
                String namesJson = commandJson.getFlag();
//                        List<String> names = mGson.fromJson(namesJson, new
// TypeToken<List<String>>() {
//                        }.getType());
//                        L.e("---------->>commandjson:" + commandJson);
//                        mControlCenter.sendLastedPics(dwUserid, commandJson.getCommand(), names);
                break;
            case CommandJson.CommandType.DOORBELL_LASTED_VIDEO_REQUEST:
                //发送视频文件
                String fileName = commandJson.getFlag();
//                        mControlCenter.sendLastVideo(dwUserid, fileName);
                break;
            case CommandJson.CommandType.UNLOCK_DOORBELL_REQUEST:
                T.show("执行解锁操作");
                ControlCenter.getBCManager().setLock(true);
                CommandControl.sendUnlockResponse(imMessage.getFromAccount());
                break;
            case CommandJson.CommandType.UNLOCK_BLUETOOTH_REQUEST:
                //蓝牙解锁
                bluetoothUnlock(commandJson);
                break;
            case CommandJson.CommandType.CHANGE_CAMERA_REQUEST://切换摄像头
                break;
            case CommandJson.CommandType.DOORBELL_PARAMS_GET_REQUEST://参数请求
                sendParams(imMessage.getFromAccount());
                break;
            case CommandJson.CommandType.DOORBELL_PARAMS_REQUEST://参数设置
                nimMessageAction.setType(NIMMessageAction.NIMMESSAGE_DOORBELL_CONFIG);
                configParams(imMessage.getFromAccount(), commandJson);
                break;
            case CommandJson.CommandType.BIND_DOORBELL_REFRESH:
                OnlineStateEventManager.publishOnlineStateEvent();
                break;

            case CommandJson.CommandType.MULTI_VIDEO_DOORBELL_REQUEST:
                //收到多人视频请求，加入房间
                nimMessageAction.setType(NIMMessageAction.NIMMESSAGE_MULTI_VIDEO);
                multiVideoReuqest(imMessage.getFromAccount(), commandJson);
                break;
            case CommandJson.CommandType.DOORBELL_CREATE_ROOM_REQUEST:
                createRoom(imMessage.getFromAccount());
                break;
            case CommandJson.CommandType.DOORBELL_SWITCH_CAMERA_REQUEST:
                nimMessageAction.setType(NIMMessageAction.NIMMESSAGE_SWITCH_CAMERA);
                break;
        }
        nimMessageAction.putExtra(Constant.COMMAND, commandJson);
        nimMessageAction.putExtra(Constant.FROM_ACCOUNT, imMessage.getFromAccount());
        EventBus.getDefault().post(nimMessageAction);
    }

    private void createRoom(final String account) {
        AVChatManager.getInstance().createRoom(ControlCenter.getSN(), null, new
                AVChatCallback<AVChatChannelInfo>() {
                    @Override
                    public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                        L.i("----------创建房间成功");
                        //启动播放服务后且服务未结束、抓拍界面未关闭时，不再重复抓拍
                        CommandControl.sendDoorbellCreateRoomResponse(account, 1);
                    }

                    @Override
                    public void onFailed(int i) {
                        L.e("----------创建房间失败" + i);
                        if (i != 417) {
                            CommandControl.sendDoorbellCreateRoomResponse(account, 0);
                            return;
                        }
                        CommandControl.sendDoorbellCreateRoomResponse(account, 1);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        L.e("----------创建房间失败" + throwable.getMessage());
                        CommandControl.sendDoorbellCreateRoomResponse(account, 0);
                    }
                });
    }

    /**
     * 多人视频请求
     */
    private void multiVideoReuqest(String account, CommandJson commandJson) {
        if (PhoneCallStateObserver.getInstance().getPhoneCallState() != PhoneCallStateObserver
                .PhoneCallStateEnum.IDLE) {
//            AVChatManager.getInstance().sendControlCommand(avChatData.getChatId(),
// AVChatControlCommand.BUSY, null);
            CommandControl.sendMultiVideoResponse(account, 0);
            return;
        }
        //判断是否超出频率
        ConfigData.VideoConfig videoConfig = ControlCenter.getDoorbellManager().getDoorbellConfig
                ().getVideoConfig();
        long currentTimeMillis = System.currentTimeMillis();
        long lastVideoTime = ControlCenter.getDoorbellManager().getLastVideoTime();
        if (currentTimeMillis - lastVideoTime < videoConfig.getVideoFrequencyLimit() * 1000) {
            //通话频率太快，超出服务器限制
            L.e("------------>拒絕2");
            CommandControl.sendMultiVideoResponse(account, 0);
            return;
        }
        // 有网络来电打开视频服务
        if (ServiceUtil.isServiceRunning(MultiAVChatService.class)) {
            CommandControl.sendMultiVideoResponse(account, 1);
            return;//避免重复调用
        }
        ActivityCollector.finishActivity(DoorbellLookActivity.class);//先结束相机界面
        ActivityCollector.finishActivity(CameraActivity.class);
        Intent intent = new Intent(Util.getApp(), MultiAVChatService.class);
        Util.getApp().startService(intent);
    }

    /**
     * 蓝牙解锁
     */
    private void bluetoothUnlock(CommandJson commandJson) {
        L.e("-------------UNLOCK_BLUETOOTH_REQUEST:");
        LockKey lockKey = null;
        try {
            lockKey = GsonUtil.fromJson(commandJson.getFlag(), LockKey.class);
        } catch (Exception ignore) {
        }
        L.e("-------------UNLOCK_BLUETOOTH_REQUEST:" + lockKey);
        if (lockKey == null) return;
        MyLockKey.sCurrentKey = lockKey;
        MyLockAPI lockAPI = MyLockAPI.getLockAPI();
        lockAPI.unlockByUser(null, lockKey);
        if (lockAPI.isConnected(lockKey.getLockMac())) {
            if (lockKey.isAdmin())
                lockAPI.unlockByAdministrator(null, lockKey);
            else
                lockAPI.unlockByUser(null, lockKey);
        } else {
            lockAPI.connect(lockKey.getLockMac(), Operation.LOCKCAR_DOWN);
        }
    }

    /**
     * 发送参数设置
     */
    private void sendParams(String account) {
        CommandControl.sendDoorbellParamsGetResponse(account);
        L.e("-------------->>sendDoorbellParamsGetResponse");
    }

    /**
     * 设置参数
     */
    private void configParams(final String account, CommandJson commandJson) {
        final String command = commandJson.getCommand();
        if (!ControlCenter.getUserManager().isBinded(account)) {
            //未绑定用户不予处理
            if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_MODE.equals(command)) {
                CommandControl.sendDoorbellModeParamsResponse(account, 0);
            } else if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_SENSOR.equals(command)) {
                CommandControl.sendDoorbellSensorParamsResponse(account, 0);
            }
            return;
        }
        final DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager()
                .getDoorbellConfig();
        if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_MODE.equals(command)) {
            DoorbellModelParam doorbellModelParam = GsonUtil.fromJson(commandJson.getFlag(),
                    DoorbellModelParam.class);
            L.e("-------doorbellModelParam:" + doorbellModelParam);
            doorbellConfig.setDoorbellModelParam(doorbellModelParam);
            CommandControl.sendDoorbellModeParamsResponse(account, 1);
            T.show(R.string.doorbell_set_changed);
        } else if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_SENSOR.equals(command)) {
            DoorbellSensorParam doorbellSensorParam = GsonUtil.fromJson(commandJson.getFlag(),
                    DoorbellSensorParam.class);
            doorbellConfig.setDoorbellSensorParam(doorbellSensorParam);
            T.show(R.string.sensor_set_changed);
            ControlCenter.getBCManager().setPIRSensorOn(doorbellConfig.getDoorbellSensorParam()
                    .getMonitor() == 1);
            CommandControl.sendDoorbellSensorParamsResponse(account, 1);
        } else if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_FACE_VALI.equals(command)) {
            int isSwitch = 0;
            try {
                isSwitch = Integer.parseInt(commandJson.getFlag());
                doorbellConfig.setFaceRecognize(isSwitch);
            } catch (Exception ignore) {
            }
            if (isSwitch == 1) {
                T.show(R.string.face_set_opened);
            } else {
                T.show(R.string.face_set_closed);
            }
            CommandControl.sendDoorbellFaceValiParamsResponse(account, 1);
        }
        ControlCenter.getDoorbellManager().setDoorbellConfig(doorbellConfig);
        ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getSN(),
                doorbellConfig, null);
    }
}
