package cn.jcyh.peephole.adapter;

import android.content.Context;
import android.content.Intent;

import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatVideoCallEvent;

import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.service.VideoService;
import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_FINISH;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_REPLY;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_REQUEST;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_START;

/**
 * Created by jogger on 2018/3/7.
 */

public class AnyChatVideoCallEventAdapter implements AnyChatVideoCallEvent {
    private Context mContext;
    private DoorBellControlCenter mControlCenter;

    public AnyChatVideoCallEventAdapter(Context context) {
        mContext = context;
        mControlCenter = DoorBellControlCenter.getInstance(mContext);
    }

    @Override
    public void OnAnyChatVideoCallEvent(int dwEventType, int dwUserId, int dwErrorCode, int dwFlags, int dwParam, String userStr) {
        Timber.e("---------OnAnyChatVideoCallEvent");
        Intent intent = new Intent(ACTION_ANYCHAT_VIDEO_CALL_EVENT);
        intent.putExtra("dwUserId", dwUserId);
        intent.putExtra("dwErrorCode", dwErrorCode);
        intent.putExtra("dwFlags", dwFlags);
        intent.putExtra("dwParam", dwParam);
        String type = "";
        intent.putExtra("userStr", userStr);
        switch (dwEventType) {
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST:// < 呼叫请求
                Timber.e("----有人发呼叫请求过来了"+DoorBellControlCenter.sIsBinding);
                if (DoorBellControlCenter.sIsBinding) {
                    //正在绑定中，不能会话
                    mControlCenter.rejectVideoCall(dwUserId);
                } else {
                    //猫眼端未打开摄像头/未在通话中，则接受请求
                    mControlCenter.acceptVideoCall(dwUserId);
                }
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY:// < 呼叫请求回复

                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_START:// 视频呼叫会话开始事件
                Timber.e("--------->开始进入会话窗口");
                Intent videoIntent = new Intent(mContext, VideoService.class);
                videoIntent.putExtra("roomId", dwParam);
                videoIntent.putExtra("userId", dwUserId);
                mContext.startService(videoIntent);
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH:// < 挂断（结束）呼叫会话
//                        DialogFactory.getDialogFactory().dismiss();
                Timber.e("--------结束通话");
                mContext.stopService(new Intent(mContext, VideoService.class));
                break;
        }

        switch (dwEventType) {
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST:// < 呼叫请求
                Timber.e("----有人发呼叫请求过来了");
                type = TYPE_BRAC_VIDEOCALL_EVENT_REQUEST;
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY:// < 呼叫请求回复 开始向设备端发送视频请求
                Timber.e("------呼叫请求得到回复");
                type = TYPE_BRAC_VIDEOCALL_EVENT_REPLY;
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_START:// 视频呼叫会话开始事件
                Timber.e("-----视频呼叫会话开始事件");
                type = TYPE_BRAC_VIDEOCALL_EVENT_START;
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH:// < 挂断（结束）呼叫会话
                Timber.e(" -------挂断（结束）呼叫会话");
                type = TYPE_BRAC_VIDEOCALL_EVENT_FINISH;
                break;
            default:
                Timber.i(" -------?????");
                break;
        }
        intent.putExtra("type", type);
        mContext.sendBroadcast(intent);
    }
}
