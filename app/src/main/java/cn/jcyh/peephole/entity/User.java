package cn.jcyh.peephole.entity;

import android.support.annotation.NonNull;

/**
 * Created by jogger on 2018/2/8.
 */

public class User implements Comparable<User> {
    private String userId;
    private String userName;
    private String nickName;
    private boolean isAdmin;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickname() {
        return nickName;
    }

    public void setNickname(String nickname) {
        this.nickName = nickname;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", nickname='" + nickName + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }

    @Override
    public int compareTo(@NonNull User user) {
        return user.isAdmin ?  0: -1;
    }
}
