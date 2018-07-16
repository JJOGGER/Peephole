package cn.jcyh.peephole.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bairuitech.anychat.AnyChatBaseEvent;

import cn.jcyh.eaglelock.constant.Constant;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.utils.L;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_BASE_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_ENTER_ROOM;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_LINK_CLOSE;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_LOGIN_STATE;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_ONLINE_USER;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_USER_AT_ROOM;

/**
 * Created by jogger on 2018/3/7.
 */

public class AnychatBaseEventAdapter implements AnyChatBaseEvent {
    private Context mContext;

    public AnychatBaseEventAdapter(Context context) {
        mContext = context;
    }

    @Override
    public void OnAnyChatConnectMessage(boolean bSuccess) {
        L.e("------OnAnyChatConnectMessage" + bSuccess);
        Intent intent = new Intent(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra(Constant.DW_ERROR_CODE, bSuccess ? 1 : -1);
        intent.putExtra(Constant.TYPE, TYPE_ANYCHAT_LOGIN_STATE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
//        mContext.sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
        L.e("------OnAnyChatLoginMessage" + dwErrorCode);
        if (dwErrorCode == 0) {//登录成功
            DoorBellControlCenter.sIsAnychatLogin = true;
            L.e("-----anychat登录成功！客户端dwUserId:" + dwUserId);
        } else {
            DoorBellControlCenter.sIsAnychatLogin = false;
            L.e("-------anychat登录失败！错误码:" + dwErrorCode);
            if (dwErrorCode == 205) {
                HttpAction.getHttpAction().initDoorbell(DoorBellControlCenter.getIMEI(), null);
            }
        }
        Intent intent = new Intent(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra(Constant.TYPE, TYPE_ANYCHAT_LOGIN_STATE);
        intent.putExtra(Constant.DW_ERROR_CODE, dwErrorCode);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
        L.e("------OnAnyChatEnterRoomMessage--->" + dwRoomId);
        Intent intent = new Intent();
        intent.putExtra(Constant.DW_ROOM_ID, dwRoomId);
        intent.putExtra(Constant.DW_ERROR_CODE, dwErrorCode);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra(Constant.TYPE, TYPE_ANYCHAT_ENTER_ROOM);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
        L.e("------OnAnyChatOnlineUserMessage");
        Intent intent = new Intent();
        intent.putExtra(Constant.DW_USER_NUM, dwUserNum);
        intent.putExtra(Constant.DW_ROOM_ID, dwRoomId);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra(Constant.TYPE, TYPE_ANYCHAT_ONLINE_USER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        L.e("------OnAnyChatUserAtRoomMessage");
        Intent intent = new Intent();
        intent.putExtra(Constant.DW_USERID, dwUserId);
        intent.putExtra(Constant.ENTER, bEnter);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra(Constant.TYPE, TYPE_ANYCHAT_USER_AT_ROOM);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        L.e("------OnAnyChatLinkCloseMessage" + dwErrorCode);
        DoorBellControlCenter.sIsAnychatLogin = false;
        Intent intent = new Intent();
        intent.putExtra(Constant.DW_ERROR_CODE, dwErrorCode);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra(Constant.TYPE, TYPE_ANYCHAT_LINK_CLOSE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
