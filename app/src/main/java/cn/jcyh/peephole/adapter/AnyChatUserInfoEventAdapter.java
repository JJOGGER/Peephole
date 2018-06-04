package cn.jcyh.peephole.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bairuitech.anychat.AnyChatUserInfoEvent;

import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_USER_INFO_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_FRIEND_STATUS;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_USER_INFO_UPDATE;

/**
 * Created by jogger on 2018/3/7.
 */

public class AnyChatUserInfoEventAdapter implements AnyChatUserInfoEvent {
    private Context mContext;

    public AnyChatUserInfoEventAdapter(Context context) {
        mContext = context;
    }

    @Override
    public void OnAnyChatUserInfoUpdate(int dwUserId, int dwType) {
        Timber.e("------OnAnyChatUserInfoUpdate" + dwUserId);
//        if (dwUserId == 0 && dwType == 0) {
//            DoorBellControlCenter.getInstance(getApplicationContext()).getFriendDatas();
// mOnFriendItem第一次在此取到值
//        }
        Intent intent = new Intent();
        intent.putExtra("dwUserId", dwUserId);
        intent.putExtra("dwType", dwType);
        intent.setAction(ACTION_ANYCHAT_USER_INFO_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_USER_INFO_UPDATE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatFriendStatus(int dwUserId, int dwStatus) {
        Timber.e("------OnAnyChatFriendStatus" + dwUserId);
//        DoorBellControlCenter.getInstance(getApplicationContext()).getFriendDatas();//重新获取好友数据
        Intent intent = new Intent();
        intent.putExtra("dwUserId", dwUserId);
        intent.putExtra("dwStatus", dwStatus);
        intent.setAction(ACTION_ANYCHAT_USER_INFO_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_FRIEND_STATUS);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
