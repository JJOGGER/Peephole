package cn.jcyh.peephole.manager.impl;

import android.text.TextUtils;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.List;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.manager.IUserManager;
import cn.jcyh.peephole.utils.DESUtil;
import cn.jcyh.peephole.utils.NetworkUtil;
import cn.jcyh.peephole.utils.T;

/**
 * Created by jogger on 2018/8/4.
 */
public class UserManager implements IUserManager {

    /**
     * 获取绑定设备的用户列表
     */
    @Override
    public List<NimUserInfo> getBindUsers() {
        List<NimUserInfo> userInfoList = NIMClient.getService(UserService.class).getUserInfoList(getBindUserIDs());
        return userInfoList == null ? new ArrayList<NimUserInfo>() : userInfoList;
    }

    /**
     * 获取绑定设备的用户帐号列表
     */
    @Override
    public List<String> getBindUserIDs() {
        List<String> friendAccounts = NIMClient.getService(FriendService.class).getFriendAccounts();
        return friendAccounts == null ? new ArrayList<String>() : friendAccounts;
    }

    /**
     * 判断是否已绑定
     */
    @Override
    public boolean isBinded(String account) {
        return NIMClient.getService(FriendService.class).isMyFriend(account);
    }

    /**
     * 根据账户获取用户信息
     */
    @Override
    public NimUserInfo getBindUserByAccount(String account) {
        if (TextUtils.isEmpty(account)) return null;
        List<NimUserInfo> bindUsers = getBindUsers();
        for (int i = 0; i < bindUsers.size(); i++) {
            if (account.equals(bindUsers.get(i).getAccount()))
                return bindUsers.get(i);
        }
        return null;
    }

    /**
     * 获取绑定的用户信息
     */
    @Override
    public List<User> getUsers() {
        List<NimUserInfo> bindUsers = getBindUsers();
        return getUsersData(bindUsers);
    }

    @Override
    public void getUserSync(final IDataListener<List<User>> listener) {
        HttpAction.getHttpAction().getBindUsers(ControlCenter.getSN(), listener);
//        NIMClient.getService(UserService.class).fetchUserInfo(ControlCenter.getUserManager().getBindUserIDs())
//                .setCallback(new RequestCallbackWrapper<List<NimUserInfo>>() {
//                    @Override
//                    public void onResult(int code, List<NimUserInfo> bindUsers, Throwable throwable) {
//                        listener.onSuccess(getUsersData(bindUsers));
//
//                    }
//                });
    }

    private List<User> getUsersData(List<NimUserInfo> bindUsers) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < bindUsers.size(); i++) {
            String name = bindUsers.get(i).getName();
            //des解密获取
            String result = "";
            try {
                result = java.net.URLDecoder.decode(DESUtil.decrypt(name, DESUtil.KEY), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            String[] string = result.split("_");
            User user = new User();
            user.setUserId(bindUsers.get(i).getAccount());//网易的帐号即userid
            try {
                user.setUserName(string[0]);
                user.setNickname(string[1]);
            } catch (Exception e) {
                user.setUserName(bindUsers.get(i).getAccount());
                user.setNickname("");
            }
            users.add(user);
        }
        return users;
    }

    /**
     * 通过帐号获取信息
     */
    @Override
    public User getUserByUserID(String userID) {
        if (TextUtils.isEmpty(userID)) return null;
        List<User> users = getUsers();
        for (int i = 0; i < users.size(); i++) {
            if (userID.equals(users.get(i).getUserId())) {
                return users.get(i);
            }
        }
        return null;
    }

    /**
     * 解绑用户
     */
    @Override
    public void unbindUser(String userID, String deviceID, String authorizationCode, IDataListener<Boolean> listener) {
        if (!NetworkUtil.isConnected()) {
            T.show(R.string.network_is_not_available);
            return;
        }
        HttpAction.getHttpAction().unbindUser(userID, deviceID, authorizationCode, listener);
    }
}
