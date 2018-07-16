package cn.jcyh.eaglelock.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jogger on 2018/5/12.
 * 密码记录
 */

public class LockPwdRecord implements Parcelable {
    private int lockId;//锁ID
    private int keyboardPwdVersion;//键盘密码版本
    private long endDate;//有效期结束时间
    private long sendDate;//发送时间
    private int keyboardPwdId;//键盘密码ID
    private String keyboardPwd;//键盘密码
    private int keyboardPwdType;//键盘密码类型
    private long startDate;//有效期开始时间
    private String receiverUsername;

    protected LockPwdRecord(Parcel in) {
        keyboardPwdId = in.readInt();
        lockId = in.readInt();
        keyboardPwd = in.readString();
        keyboardPwdVersion = in.readInt();
        keyboardPwdType = in.readInt();
        startDate = in.readLong();
        endDate = in.readLong();
        sendDate = in.readLong();
        receiverUsername = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(keyboardPwdId);
        dest.writeInt(lockId);
        dest.writeString(keyboardPwd);
        dest.writeInt(keyboardPwdVersion);
        dest.writeInt(keyboardPwdType);
        dest.writeLong(startDate);
        dest.writeLong(endDate);
        dest.writeLong(sendDate);
        dest.writeString(receiverUsername);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LockPwdRecord> CREATOR = new Creator<LockPwdRecord>() {
        @Override
        public LockPwdRecord createFromParcel(Parcel in) {
            return new LockPwdRecord(in);
        }

        @Override
        public LockPwdRecord[] newArray(int size) {
            return new LockPwdRecord[size];
        }
    };

    public int getKeyboardPwdId() {
        return keyboardPwdId;
    }

    public void setKeyboardPwdId(int keyboardPwdId) {
        this.keyboardPwdId = keyboardPwdId;
    }

    public int getLockId() {
        return lockId;
    }

    public void setLockId(int lockId) {
        this.lockId = lockId;
    }

    public String getKeyboardPwd() {
        return keyboardPwd;
    }

    public void setKeyboardPwd(String keyboardPwd) {
        this.keyboardPwd = keyboardPwd;
    }

    public int getKeyboardPwdVersion() {
        return keyboardPwdVersion;
    }

    public void setKeyboardPwdVersion(int keyboardPwdVersion) {
        this.keyboardPwdVersion = keyboardPwdVersion;
    }

    public int getKeyboardPwdType() {
        return keyboardPwdType;
    }

    public void setKeyboardPwdType(int keyboardPwdType) {
        this.keyboardPwdType = keyboardPwdType;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getSendDate() {
        return sendDate;
    }

    public void setSendDate(long sendDate) {
        this.sendDate = sendDate;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    @Override
    public String toString() {
        return "LockPwdRecord{" +
                "keyboardPwdId=" + keyboardPwdId +
                ", lockId=" + lockId +
                ", keyboardPwd='" + keyboardPwd + '\'' +
                ", keyboardPwdVersion=" + keyboardPwdVersion +
                ", keyboardPwdType=" + keyboardPwdType +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", sendDate=" + sendDate +
                ", receiverUsername='" + receiverUsername + '\'' +
                '}';
    }
}
