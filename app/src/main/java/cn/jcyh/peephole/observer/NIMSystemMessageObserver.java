package cn.jcyh.peephole.observer;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.SystemMessage;

/**
 * Created by jogger on 2018/7/27.
 */
public class NIMSystemMessageObserver implements Observer<SystemMessage> {
    @Override
    public void onEvent(SystemMessage systemMessage) {
        if (systemMessage.getType() == SystemMessageType.AddFriend) {
            AddFriendNotify attachData = (AddFriendNotify) systemMessage.getAttachObject();
            if (attachData != null) {
                // 针对不同的事件做处理
                if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_DIRECT) {
                    // 对方直接添加你为好友
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_AGREE_ADD_FRIEND) {
                    // 对方通过了你的好友验证请求
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_REJECT_ADD_FRIEND) {
                    // 对方拒绝了你的好友验证请求
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                    // 对方请求添加好友，一般场景会让用户选择同意或拒绝对方的好友请求。
                    // 通过message.getContent()获取好友验证请求的附言
                }
            }
        }
    }
}
