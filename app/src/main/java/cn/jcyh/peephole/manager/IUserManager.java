package cn.jcyh.peephole.manager;

import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.List;

import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.http.IDataListener;

/**
 * Created by jogger on 2018/8/4.
 */
public interface IUserManager {
    List<NimUserInfo> getBindUsers();

    List<String> getBindUserIDs();

    boolean isBinded(String account);

    NimUserInfo getBindUserByAccount(String account);

    List<User> getUsers();

    void getUserSync(IDataListener<List<User>> listener);

    User getUserByUserID(String account);

    void unbindUser(String userID, String deviceID, String authorizationCode, IDataListener<Boolean> listener);

}
