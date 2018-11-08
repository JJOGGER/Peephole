package cn.jcyh.peephole.observer;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.command.CommandControl;
import cn.jcyh.peephole.command.IMMessageCommandImpl;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.CommandJson;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.DoorbellModelParam;
import cn.jcyh.peephole.entity.DoorbellSensorParam;
import cn.jcyh.peephole.event.NIMMessageAction;
import cn.jcyh.peephole.event.online.OnlineStateEventManager;
import cn.jcyh.peephole.utils.GsonUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.T;

/**
 * Created by jogger on 2018/7/26.
 */
public class MessageReceiveObserver implements Observer<List<IMMessage>> {

    @Override
    public void onEvent(List<IMMessage> imMessages) {
        for (int i = 0; i < imMessages.size(); i++) {
            IMMessage imMessage = imMessages.get(i);
            if (imMessage.getMsgType() != MsgTypeEnum.text) return;//文本以外的消息暂不处理
            // TODO: 2018/9/30 必须是猫眼的绑定用户才可以做操作
            L.e("----->内容：" + imMessages.get(i).getContent() + "，来自：" + imMessages.get(i).getFromAccount()
                    + ",消息类型：" + imMessages.get(i).getMsgType());
            //指令消息处理
            CommandJson commandJson = null;
            try {
                commandJson = GsonUtil.fromJson(imMessage.getContent(), CommandJson.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
//                        mControlCenter.sendLastedPicsNamesResponse(dwUserid, commandJson.getCommand(),
//                                requestNum);
                break;
            case CommandJson.CommandType.DOORBELL_LASTED_IMG_REQUEST:
                //发送图片
                String namesJson = commandJson.getFlag();
//                        List<String> names = mGson.fromJson(namesJson, new TypeToken<List<String>>() {
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
        }
        nimMessageAction.putExtra(Constant.COMMAND, commandJson);
        nimMessageAction.putExtra(Constant.FROM_ACCOUNT, imMessage.getFromAccount());
        EventBus.getDefault().post(nimMessageAction);
    }

    /**
     * 发送参数设置
     */
    private void sendParams(String account) {
        CommandControl.sendDoorbellParamsGetResponse(account);
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
        final DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_MODE.equals(command)) {
            DoorbellModelParam doorbellModelParam = GsonUtil.fromJson(commandJson.getFlag(), DoorbellModelParam.class);
            L.e("-------doorbellModelParam:" + doorbellModelParam);
            doorbellConfig.setDoorbellModelParam(doorbellModelParam);
            CommandControl.sendDoorbellModeParamsResponse(account, 1);
            T.show(R.string.doorbell_set_changed);
        } else if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_SENSOR.equals(command)) {
            DoorbellSensorParam doorbellSensorParam = GsonUtil.fromJson(commandJson.getFlag(), DoorbellSensorParam.class);
            doorbellConfig.setDoorbellSensorParam(doorbellSensorParam);
            T.show(R.string.sensor_set_changed);
            ControlCenter.getBCManager().setPIRSensorOn(doorbellConfig.getDoorbellSensorParam().getMonitor() == 1);
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
        ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getSN(), doorbellConfig, null);
    }
}
