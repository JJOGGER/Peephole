package cn.jcyh.peephole.observer;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.command.CommandControl;
import cn.jcyh.peephole.command.IMMessageCommandImpl;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.CommandJson;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.DoorbellParam;
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
            NIMMessageAction nimMessageAction = new NIMMessageAction();
            if (imMessage.getMsgType() == MsgTypeEnum.text) {
                L.e("----->内容：" + imMessages.get(i).getContent() + "，来自：" + imMessages.get(i).getFromAccount()
                        + ",消息类型：" + imMessages.get(i).getMsgType());
                //指令消息处理
                CommandJson commandJson = null;
                try {
                    commandJson = GsonUtil.fromJson(imMessage.getContent(), CommandJson.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (commandJson == null) return;
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
                    case CommandJson.CommandType.DOORBELL_CALL_IMG_REQUEST:
                        //视频呼叫图片请求
//                L.e("----------视频呼叫图片请求" + dwUserid + "---filepath:" + commandJson.getFlag());
                        break;
                    case CommandJson.CommandType.CHANGE_CAMERA_REQUEST://切换摄像头
                        break;
                    case CommandJson.CommandType.DOORBELL_PARAMS_GET_REQUEST://参数请求
                        L.e("--------CommandJson.CommandType.DOORBELL_PARAMS_GET_REQUEST");
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
        }
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
        DoorbellParam doorbellParam;
        if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_MODE.equals(command)) {
            doorbellParam = GsonUtil.fromJson(commandJson.getFlag(), DoorbellParam.class);
            doorbellConfig.setDoorbellParams(doorbellParam);
        } else if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_SENSOR.equals(command)) {
            doorbellParam = GsonUtil.fromJson(commandJson.getFlag(), DoorbellParam.class);
            doorbellConfig.setMonitorParams(doorbellParam);
        }
        ControlCenter.getDoorbellManager().setDoorbellConfig(doorbellConfig);
        if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_MODE.equals(command)) {
            CommandControl.sendDoorbellModeParamsResponse(account, 1);
            T.show(R.string.doorbell_set_changed);
        } else if (IMMessageCommandImpl.DOORBELL_PARAMS_TYPE_SENSOR.equals(command)) {
            T.show(R.string.sensor_set_changed);
            ControlCenter.getBCManager().setPIRSensorOn(doorbellConfig.getMonitorSwitch() == 1);
            CommandControl.sendDoorbellSensorParamsResponse(account, 1);
        }
        ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getSN(), doorbellConfig, null);
    }
}
