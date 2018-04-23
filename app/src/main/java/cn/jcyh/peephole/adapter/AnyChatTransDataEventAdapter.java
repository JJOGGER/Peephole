package cn.jcyh.peephole.adapter;

import android.content.Context;
import android.content.Intent;

import com.bairuitech.anychat.AnyChatTransDataEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import cn.jcyh.peephole.bean.CommandJson;
import cn.jcyh.peephole.control.DoorBellControlCenter;
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
        mControlCenter = DoorBellControlCenter.getInstance(mContext);
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
        mContext.sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatTransBuffer(int dwUserid, byte[] lpBuf, int dwLen) {
        String result = new String(lpBuf, 0, lpBuf.length);
        Intent intent = new Intent();
        intent.putExtra("dwUserid", dwUserid);
        intent.putExtra("result", result);
        intent.setAction(ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_TRANS_BUFFER);
        CommandJson commandJson = mGson.fromJson(result, CommandJson.class);
        intent.putExtra("command", commandJson);
        Timber.e("-----OnAnyChatTransBuffer" + result + "---dwUserid:" + dwUserid);
        mContext.sendBroadcast(intent);
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
                mControlCenter.sendUnlockResponse(dwUserid);
                break;
            case CommandJson.CommandType.DOORBELL_CALL_IMG_REQUEST:
                //视频呼叫图片请求
                Timber.e("----------视频呼叫图片请求" + dwUserid + "---filepath:" + commandJson.getFlag());
                mControlCenter.sendVideoCallImg(dwUserid, commandJson.getFlag());
                break;
            case CommandJson.CommandType.UNBIND_DOORBELL_COMPLETED:
                // TODO: 2018/2/26 有用户解绑成功
                Timber.e("---------收到用户解绑");
                break;
            case CommandJson.CommandType.CHANGE_CAMERA_REQUEST:
//                由videoservice处理
                mControlCenter.sendChangeCameraResponse(dwUserid);
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
}
