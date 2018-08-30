package cn.jcyh.peephole.observer;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.jcyh.peephole.event.NIMFriendAction;

/**
 * Created by jogger on 2018/8/4.
 */
public class FriendServiceObserver implements Observer<FriendChangedNotify> {
    @Override
    public void onEvent(FriendChangedNotify friendChangedNotify) {
        List<Friend> addedOrUpdatedFriends = friendChangedNotify.getAddedOrUpdatedFriends(); // 新增的好友
        if (addedOrUpdatedFriends != null && addedOrUpdatedFriends.size() != 0) {
            NIMFriendAction friendAction = new NIMFriendAction();
            friendAction.setType(NIMFriendAction.TYPE_ADD_DOORBELL);
            EventBus.getDefault().post(friendAction);
        }
        List<String> deletedFriendAccounts = friendChangedNotify.getDeletedFriends(); // 删除好友或者被解除好友
        if (deletedFriendAccounts != null && deletedFriendAccounts.size() != 0) {
            NIMFriendAction friendAction = new NIMFriendAction();
            friendAction.setType(NIMFriendAction.TYPE_DELETE_DOORBELL);
            EventBus.getDefault().post(friendAction);
        }
    }
}
