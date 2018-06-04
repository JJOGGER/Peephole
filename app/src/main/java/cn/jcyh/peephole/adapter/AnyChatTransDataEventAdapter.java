package cn.jcyh.peephole.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bairuitech.anychat.AnyChatTransDataEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.bean.CommandJson;
import cn.jcyh.peephole.bean.DoorbellParam;
import cn.jcyh.peephole.bean.User;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.BcManager;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.ToastUtil;
import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_TRANS_BUFFER;

/**
 * Created by jogger on 2018/3/7.
 */

public class AnyChatTransDataEventAdapter implements AnyChatTransDataEvent {
    private Context mContext;
    private DoorBellControlCenter mControlCenter;
    private Gson mGson;

    public AnyChatTransDataEventAdapter(Context context) {
        mContext = context;
        mControlCenter = DoorBellControlCenter.getInstance();
        mGson = new Gson();
    }

    @Override
    public void OnAnyChatTransFile(int dwUserid, String FileName, String TempFilePath, int
            dwFileLength, int wParam, int lParam, int dwTaskId) {
        Timber.e("---------OnAnyChatTransFile" + lParam + "-->" + FileName + "--" + TempFilePath);
        Intent intent = new Intent();
        intent.putExtra("dwUserid", dwUserid);
        intent.putExtra("targetPath", TempFilePath);
        intent.putExtra("dwFileLength", dwFileLength);
        intent.putExtra("wParam", wParam);
        intent.putExtra("lParam", lParam);
        intent.putExtra("dwTaskId", dwTaskId);
        intent.setAction(ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intent.putExtra("type", ConstantUtil.TYPE_ANYCHAT_TRANS_FILE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatTransBuffer(int dwUserid, byte[] lpBuf, int dwLen) {
        String result = new String(lpBuf, 0, lpBuf.length);
        Intent intent = new Intent();
        intent.putExtra("dwUserid", dwUserid);
        intent.putExtra("result", result);
        Timber.e("-----OnAnyChatTransBuffer" + result + "---dwUserid:" + dwUserid);
        intent.setAction(ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_TRANS_BUFFER);
        CommandJson commandJson = null;
        try {
            commandJson = mGson.fromJson(result, CommandJson.class);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("-----e:" + e.getMessage());
        }
        if (commandJson == null) return;
        intent.putExtra("command", commandJson);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        switch (commandJson.getCommandType()) {
            case CommandJson.CommandType.DOORBELL_LASTED_IMG_NAMES_REQUEST:
                //收到近期图片获取请求
                int requestNum = commandJson.getFlag2();//获取请求数
                //从文件中获取响应数量的图片
                mControlCenter.sendLastedPicsNamesResponse(dwUserid, commandJson.getCommand(),
                        requestNum);
                break;
            case CommandJson.CommandType.DOORBELL_LASTED_IMG_REQUEST:
                //发送图片
                String namesJson = commandJson.getFlag();
                List<String> names = mGson.fromJson(namesJson, new TypeToken<List<String>>() {
                }.getType());
                Timber.e("---------->>commandjson:" + commandJson);
                mControlCenter.sendLastedPics(dwUserid, commandJson.getCommand(), names);
                break;
            case CommandJson.CommandType.DOORBELL_LASTED_VIDEO_REQUEST:
                //发送视频文件
                String fileName = commandJson.getFlag();
                mControlCenter.sendLastVideo(dwUserid, fileName);
                break;
        }
        switch (commandJson.getCommandType()) {
            case CommandJson.CommandType.UNLOCK_DOORBELL_REQUEST:
                ToastUtil.showToast(mContext, "执行解锁操作");
                BcManager.getManager(mContext).setLock(true);
                mControlCenter.sendUnlockResponse(dwUserid);
                break;
            case CommandJson.CommandType.DOORBELL_CALL_IMG_REQUEST:
                //视频呼叫图片请求
                Timber.e("----------视频呼叫图片请求" + dwUserid + "---filepath:" + commandJson.getFlag());
                mControlCenter.sendVideoCallImg(dwUserid, commandJson.getFlag());
                break;
            case CommandJson.CommandType.UNBIND_DOORBELL_COMPLETED:
                HttpAction.getHttpAction().getBindUsers(DoorBellControlCenter.getIMEI(), new IDataListener<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        DoorBellControlCenter.getInstance().saveBindUsers(users);
                    }

                    @Override
                    public void onFailure(int errorCode) {

                    }
                });
                break;
            case CommandJson.CommandType.CHANGE_CAMERA_REQUEST://切换摄像头
//                由videoservice处理
                mControlCenter.sendChangeCameraResponse(dwUserid);
                break;
            case CommandJson.CommandType.DOORBELL_PARAMS_REQUEST://参数设置
                configParams(commandJson, dwUserid);
                break;
        }
    }

    @Override
    public void OnAnyChatTransBufferEx(int dwUserid, byte[] lpBuf, int dwLen, int wparam, int
            lparam, int taskid) {
        Timber.e("-----------OnAnyChatTransBufferEx");
    }

    @Override
    public void OnAnyChatSDKFilterData(byte[] lpBuf, int dwLen) {
        Timber.e("-----------OnAnyChatSDKFilterData");
    }

    /**
     * 设置参数
     */
    private void configParams(CommandJson commandJson, final int dwUserid) {
        final String command = commandJson.getCommand();
        final DoorbellConfig doorbellConfig = mControlCenter.getDoorbellConfig();
        if (DoorBellControlCenter.DOORBELL_PARAMS_TYPE_MODE.equals(command)) {
            DoorbellParam doorbellParam = mGson.fromJson(commandJson.getFlag(), DoorbellParam.class);
            doorbellConfig.setDoorbellParams(doorbellParam);
        } else if (DoorBellControlCenter.DOORBELL_PARAMS_TYPE_SENSOR.equals(command)) {
            DoorbellParam doorbellParam = mGson.fromJson(commandJson.getFlag(), DoorbellParam.class);
            doorbellConfig.setMonitorParams(doorbellParam);
        }
        HttpAction.getHttpAction().setDoorbellConfig(DoorBellControlCenter.getIMEI(), doorbellConfig, new IDataListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                mControlCenter.saveDoorbellConfig(doorbellConfig);
                mControlCenter.sendDoorbellConfigResponse(dwUserid, command, 1);
                ToastUtil.showToast(mContext, R.string.doorbell_set_changed);
                // TODO: 2018/4/25 如果当前在设置界面，应更新
            }

            @Override
            public void onFailure(int errorCode) {
                mControlCenter.sendDoorbellConfigResponse(dwUserid, command, 0);
            }
        });
    }
}
