package cn.jcyh.eaglelock.entity;

/**
 * Created by jogger on 2018/11/13.
 */
public class UnLockData {
    private int smartLockUserId;
    private String smartLockAccessToken;
    private String adminPwd;
    private int lockId;
    private String lockKey;
    private String aesKeyStr;
    private String lockVersion;
    private int lockFlagPos;
    private int timezoneRawOffset;
    private String lockMac;
    private String userType;

    public int getSmartLockUserId() {
        return smartLockUserId;
    }

    public void setSmartLockUserId(int smartLockUserId) {
        this.smartLockUserId = smartLockUserId;
    }

    public String getAdminPwd() {
        return adminPwd;
    }

    public void setAdminPwd(String admionPwd) {
        this.adminPwd = admionPwd;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public int getLockId() {
        return lockId;
    }

    public void setLockId(int lockId) {
        this.lockId = lockId;
    }

    public String getAesKeyStr() {
        return aesKeyStr;
    }

    public void setAesKeyStr(String aesKeyStr) {
        this.aesKeyStr = aesKeyStr;
    }

    public String getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(String lockVersion) {
        this.lockVersion = lockVersion;
    }

    public int getLockFlagPos() {
        return lockFlagPos;
    }

    public void setLockFlagPos(int lockFlagPos) {
        this.lockFlagPos = lockFlagPos;
    }

    public String getLockMac() {
        return lockMac;
    }

    public void setLockMac(String lockMac) {
        this.lockMac = lockMac;
    }

    public int getTimezoneRawOffset() {
        return timezoneRawOffset;
    }

    public void setTimezoneRawOffset(int timezoneRawOffset) {
        this.timezoneRawOffset = timezoneRawOffset;
    }

    public String getSmartLockAccessToken() {
        return smartLockAccessToken;
    }

    public void setSmartLockAccessToken(String smartLockAccessToken) {
        this.smartLockAccessToken = smartLockAccessToken;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "UnLockData{" +
                "smartLockUserId='" + smartLockUserId + '\'' +
                "adminPwd='" + adminPwd + '\'' +
                "adminPwd='" + adminPwd + '\'' +
                ", lockKey='" + lockKey + '\'' +
                ", aesKeyStr='" + aesKeyStr + '\'' +
                ", lockVersion='" + lockVersion + '\'' +
                ", lockFlagPos=" + lockFlagPos +
                ", timezoneRawOffset=" + timezoneRawOffset +
                '}';
    }
}
