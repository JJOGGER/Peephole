package cn.jcyh.eaglelock.entity;

/**
 * Created by jogger on 2018/5/7.
 */

public class LockKeyboardPwd {
    private String keyboardPwd;
    private int keyboardPwdId;

    public String getKeyboardPwd() {
        return keyboardPwd;
    }

    public void setKeyboardPwd(String keyboardPwd) {
        this.keyboardPwd = keyboardPwd;
    }

    public int getKeyboardPwdId() {
        return keyboardPwdId;
    }

    public void setKeyboardPwdId(int keyboardPwdId) {
        this.keyboardPwdId = keyboardPwdId;
    }

    @Override
    public String toString() {
        return "LockKeyboardPwd{" +
                "keyboardPwd='" + keyboardPwd + '\'' +
                ", keyboardPwdId=" + keyboardPwdId +
                '}';
    }
}
