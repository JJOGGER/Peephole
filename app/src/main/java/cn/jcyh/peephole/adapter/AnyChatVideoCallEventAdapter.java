package cn.jcyh.peephole.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatVideoCallEvent;

import cn.jcyh.eaglelock.constant.Constant;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.service.VideoService;
import cn.jcyh.peephole.utils.L;

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
        mControlCenter = DoorBellControlCenter.getInstance();
    }

    @Override
    public void OnAnyChatVideoCallEvent(int dwEventType, int dwUserId, int dwErrorCode, int dwFlags, int dwParam, String userStr) {
        L.e("---------OnAnyChatVideoCallEvent");
        Intent intent = new Intent(ACTION_ANYCHAT_VIDEO_CALL_EVENT);
        intent.putExtra(Constant.DW_USERID, dwUserId);
        intent.putExtra(Constant.DW_ERROR_CODE, dwErrorCode);
        intent.putExtra(Constant.DW_FLAGS, dwFlags);
        intent.putExtra(Constant.DW_PARAM, dwParam);
        String type = "";
        intent.putExtra(Constant.USER_STR, userStr);
        switch (dwEventType) {
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST:// < 呼叫请求
                L.e("----有人发呼叫请求过来了" + DoorBellControlCenter.sIsBinding);
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
                L.e("--------->开始进入会话窗口");
                Intent videoIntent = new Intent(mContext, VideoService.class);
                videoIntent.putExtra(Constant.DW_ROOM_ID, dwParam);
                videoIntent.putExtra(Constant.DW_USERID, dwUserId);
                mContext.startService(videoIntent);
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH:// < 挂断（结束）呼叫会话
//                        DialogFactory.getDialogFactory().dismiss();
                L.e("--------结束通话");
                mContext.stopService(new Intent(mContext, VideoService.class));
                break;
        }

        switch (dwEventType) {
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST:// < 呼叫请求
                L.e("----有人发呼叫请求过来了");
                type = TYPE_BRAC_VIDEOCALL_EVENT_REQUEST;
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY:// < 呼叫请求回复 开始向设备端发送视频请求
                L.e("------呼叫请求得到回复");
                type = TYPE_BRAC_VIDEOCALL_EVENT_REPLY;
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_START:// 视频呼叫会话开始事件
                L.e("-----视频呼叫会话开始事件");
                type = TYPE_BRAC_VIDEOCALL_EVENT_START;
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH:// < 挂断（结束）呼叫会话
                L.e(" -------挂断（结束）呼叫会话");
                type = TYPE_BRAC_VIDEOCALL_EVENT_FINISH;
                break;
            default:
                L.i(" -------?????");
                break;
        }
        intent.putExtra(Constant.TYPE, type);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
