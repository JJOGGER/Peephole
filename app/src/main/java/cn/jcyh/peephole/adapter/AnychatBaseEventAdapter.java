package cn.jcyh.peephole.adapter;

import android.content.Context;
import android.content.Intent;

import com.bairuitech.anychat.AnyChatBaseEvent;

import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_BASE_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_LOGIN_RESULT_MSG;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_ENTER_ROOM;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_LINK_CLOSE;
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
        Timber.e("------OnAnyChatConnectMessage" + bSuccess);
    }

    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
        Timber.e("------OnAnyChatLoginMessage" + dwErrorCode);
        if (dwErrorCode == 0) {//登录成功
            Timber.e("-----anychat登录成功！客户端dwUserId:" + dwUserId);
        } else {
            Timber.e("-------anychat登录失败！错误码:" + dwErrorCode);
        }
        Intent intent = new Intent(ACTION_ANYCHAT_LOGIN_RESULT_MSG);
        intent.putExtra("dwErrorCode", dwErrorCode);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
        Timber.e("------OnAnyChatEnterRoomMessage--->" + dwRoomId);
        Intent intent = new Intent();
        intent.putExtra("dwRoomId", dwRoomId);
        intent.putExtra("dwErrorCode", dwErrorCode);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_ENTER_ROOM);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
        Timber.e("------OnAnyChatOnlineUserMessage");
        Intent intent = new Intent();
        intent.putExtra("dwUserNum", dwUserNum);
        intent.putExtra("dwRoomId", dwRoomId);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_ONLINE_USER);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        Timber.e("------OnAnyChatUserAtRoomMessage");
        Intent intent = new Intent();
        intent.putExtra("dwUserId", dwUserId);
        intent.putExtra("bEnter", bEnter);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_USER_AT_ROOM);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        Timber.e("------OnAnyChatLinkCloseMessage" + dwErrorCode);
        Intent intent = new Intent();
        intent.putExtra("dwErrorCode", dwErrorCode);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_LINK_CLOSE);
        mContext.sendBroadcast(intent);
//        if (dwErrorCode != 209) {
//            final String uid = SharePreUtil.getInstance(getApplicationContext()).getString
// (ConstansUtil.UID, "");
//            connectAnyChat(uid);
//        }
    }
}
