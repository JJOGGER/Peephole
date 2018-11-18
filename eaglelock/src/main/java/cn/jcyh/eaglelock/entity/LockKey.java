package cn.jcyh.eaglelock.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import cn.jcyh.locklib.entity.LockVersion;


/**
 * Created by Administrator on 2016/9/6 0006.
 */
public class LockKey implements Parcelable {

    private String accessToken;//访问令牌
    private int lockId;//锁id
    private int keyId;//钥匙id
    /**
     * 钥匙状态
     * "110401"	正常使用
     * "110402"	待接收
     * "110405"	已冻结
     * "110408"	已删除
     * "110410"	已重置
     */
    private String keyStatus;
    private String userType;
    private String lockName;//锁名称
    private String lockAlias;//锁别名
    private boolean isAdmin;//是否是管理员
    private String lockKey;//锁数据 直接传就行
    private String lockMac;//锁mac地址
    private int lockFlagPos;//锁标志位
    private String adminPwd;//管理员钥匙会有，锁的管理员密码，锁管理相关操作需要携带，校验管理员权限
    private String noKeyPwd;//管理员钥匙会有，管理员键盘密码，管理员用该密码开门
    private String deletePwd;//二代锁的管理员钥匙会有，清空码，用于清空锁上的密码
    private int electricQuantity;//电量
    private String aesKeyStr;//Aes加解密key
    private LockVersion lockVersion;//锁版本信息 json格式
    private long startDate;//有效期开始时间
    private long endDate;//有效期结束时间
    /**
     * 不考虑时区问题传入-1即可
     * 锁所在时区和UTC时区时间的差数，单位milliseconds，默认28800000（中国时区）
     */
    private int timezoneRawOffset;
    private String remarks;//备注，留言
    private String keyRight;//钥匙是否被授权：0-否，1-是
    private int keyboardPwdVersion;//键盘密码版本: 0、1、2、3、4
    private int specialValue;//锁特征值，用于表示锁支持的功能
    private String pwdInfo;//密码数据信息
    private long timestamp;//时间搓
    private String modelNumber;//锁型号
    private String hardwareRevision;//锁硬件版本号
    String firmwareRevision;//锁固件版本号
    private String username;
    private String senderUsername;
    private int openid;
    private long date;
    private String cateyeId;
    private Gson mGson;

    public LockKey() {
        mGson = new Gson();
    }

    public LockKey(UnLockData unLockData) {
        mGson = new Gson();
        openid = unLockData.getSmartLockUserId();
        adminPwd = unLockData.getAdminPwd();
        aesKeyStr = unLockData.getAesKeyStr();
        lockFlagPos = unLockData.getLockFlagPos();
        lockKey = unLockData.getLockKey();
        lockId = unLockData.getLockId();
        lockMac = unLockData.getLockMac();
        setLockVersion(unLockData.getLockVersion());
        accessToken = unLockData.getSmartLockAccessToken();
        timezoneRawOffset = unLockData.getTimezoneRawOffset();
        userType = unLockData.getUserType();
        accessToken = unLockData.getSmartLockAccessToken();
    }

    private LockKey(Parcel in) {
        accessToken = in.readString();
        lockId = in.readInt();
        keyId = in.readInt();
        keyStatus = in.readString();
        userType = in.readString();
        lockName = in.readString();
        lockAlias = in.readString();
        isAdmin = in.readByte() != 0;
        lockKey = in.readString();
        lockMac = in.readString();
        lockFlagPos = in.readInt();
        adminPwd = in.readString();
        noKeyPwd = in.readString();
        deletePwd = in.readString();
        electricQuantity = in.readInt();
        aesKeyStr = in.readString();
        lockVersion = in.readParcelable(LockVersion.class.getClassLoader());
        startDate = in.readLong();
        endDate = in.readLong();
        timezoneRawOffset = in.readInt();
        remarks = in.readString();
        keyRight = in.readString();
        keyboardPwdVersion = in.readInt();
        specialValue = in.readInt();
        pwdInfo = in.readString();
        timestamp = in.readLong();
        modelNumber = in.readString();
        hardwareRevision = in.readString();
        firmwareRevision = in.readString();
        cateyeId = in.readString();
    }

    public static final Creator<LockKey> CREATOR = new Creator<LockKey>() {
        @Override
        public LockKey createFromParcel(Parcel in) {
            return new LockKey(in);
        }

        @Override
        public LockKey[] newArray(int size) {
            return new LockKey[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
        dest.writeInt(lockId);
        dest.writeInt(keyId);
        dest.writeString(keyStatus);
        dest.writeString(userType);
        dest.writeString(lockName);
        dest.writeString(lockAlias);
        dest.writeByte((byte) ("110301".equals(userType) ? 1 : 0));
        dest.writeString(lockKey);
        dest.writeString(lockMac);
        dest.writeInt(lockFlagPos);
        dest.writeString(adminPwd);
        dest.writeString(noKeyPwd);
        dest.writeString(deletePwd);
        dest.writeInt(electricQuantity);
        dest.writeString(aesKeyStr);
        dest.writeParcelable(lockVersion, flags);
        dest.writeLong(startDate);
        dest.writeLong(endDate);
        dest.writeInt(timezoneRawOffset);
        dest.writeString(remarks);
        dest.writeString(keyRight);
        dest.writeInt(keyboardPwdVersion);
        dest.writeInt(specialValue);
        dest.writeString(pwdInfo);
        dest.writeLong(timestamp);
        dest.writeString(modelNumber);
        dest.writeString(hardwareRevision);
        dest.writeString(firmwareRevision);
        dest.writeString(cateyeId);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getLockId() {
        return lockId;
    }

    public void setLockId(int lockId) {
        this.lockId = lockId;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getKeyStatus() {
        return keyStatus;
    }

    public void setKeyStatus(String keyStatus) {
        this.keyStatus = keyStatus;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public String getLockAlias() {
        return lockAlias;
    }

    public void setLockAlias(String lockAlias) {
        this.lockAlias = lockAlias;
    }

    public boolean isAdmin() {
        return "110301".equals(userType);
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public String getLockMac() {
        return lockMac;
    }

    public void setLockMac(String lockMac) {
        this.lockMac = lockMac;
    }

    public int getLockFlagPos() {
        return lockFlagPos;
    }

    public void setLockFlagPos(int lockFlagPos) {
        this.lockFlagPos = lockFlagPos;
    }

    public String getAdminPwd() {
        return adminPwd;
    }

    public void setAdminPwd(String adminPwd) {
        this.adminPwd = adminPwd;
    }

    public String getNoKeyPwd() {
        return noKeyPwd;
    }

    public void setNoKeyPwd(String noKeyPwd) {
        this.noKeyPwd = noKeyPwd;
    }

    public String getDeletePwd() {
        return deletePwd;
    }

    public void setDeletePwd(String deletePwd) {
        this.deletePwd = deletePwd;
    }

    public int getElectricQuantity() {
        return electricQuantity;
    }

    public void setElectricQuantity(int electricQuantity) {
        this.electricQuantity = electricQuantity;
    }

    public String getAesKeystr() {
        return aesKeyStr;
    }

    public void setAesKeystr(String aesKeystr) {
        this.aesKeyStr = aesKeystr;
    }

    public String getLockVersion() {
        return mGson.toJson(lockVersion);
    }

    public LockVersion getLockVersionEntity() {
        return lockVersion;
    }

    public void setLockVersion(LockVersion lockVersion) {
        this.lockVersion = lockVersion;
    }

    public void setLockVersion(String lockVersion) {
        this.lockVersion = mGson.fromJson(lockVersion, LockVersion.class);
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

    public int getTimezoneRawOffset() {
        return timezoneRawOffset;
    }

    public void setTimezoneRawOffset(int timezoneRawOffset) {
        this.timezoneRawOffset = timezoneRawOffset;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getKeyRight() {
        return keyRight;
    }

    public void setKeyRight(String keyRight) {
        this.keyRight = keyRight;
    }

    public int getKeyboardPwdVersion() {
        return keyboardPwdVersion;
    }

    public void setKeyboardPwdVersion(int keyboardPwdVersion) {
        this.keyboardPwdVersion = keyboardPwdVersion;
    }

    public int getSpecialValue() {
        return specialValue;
    }

    public void setSpecialValue(int specialValue) {
        this.specialValue = specialValue;
    }

    public String getPwdInfo() {
        return pwdInfo;
    }

    public void setPwdInfo(String pwdInfo) {
        this.pwdInfo = pwdInfo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getHardwareRevision() {
        return hardwareRevision;
    }

    public void setHardwareRevision(String hardwareRevision) {
        this.hardwareRevision = hardwareRevision;
    }

    public String getFirmwareRevision() {
        return firmwareRevision;
    }

    public void setFirmwareRevision(String firmwareRevision) {
        this.firmwareRevision = firmwareRevision;
    }

    public String getAesKeyStr() {
        return aesKeyStr;
    }

    public void setAesKeyStr(String aesKeyStr) {
        this.aesKeyStr = aesKeyStr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public int getOpenid() {
        return openid;
    }

    public void setOpenid(int openid) {
        this.openid = openid;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getCateyeId() {
        return cateyeId;
    }

    public void setCateyeId(String cateyeId) {
        this.cateyeId = cateyeId;
    }

    @Override
    public String toString() {
        return "LockKey{" +
                "cateyeId='" + cateyeId + '\'' +
                "accessToken='" + accessToken + '\'' +
                ", lockId=" + lockId +
                ", keyId=" + keyId +
                ", keyStatus='" + keyStatus + '\'' +
                ", userType='" + userType + '\'' +
                ", lockName='" + lockName + '\'' +
                ", lockAlias='" + lockAlias + '\'' +
                ", isAdmin=" + isAdmin +
                ", lockKey='" + lockKey + '\'' +
                ", lockMac='" + lockMac + '\'' +
                ", lockFlagPos=" + lockFlagPos +
                ", adminPwd='" + adminPwd + '\'' +
                ", noKeyPwd='" + noKeyPwd + '\'' +
                ", deletePwd='" + deletePwd + '\'' +
                ", electricQuantity=" + electricQuantity +
                ", aesKeyStr='" + aesKeyStr + '\'' +
                ", lockVersion=" + lockVersion +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", timezoneRawOffset=" + timezoneRawOffset +
                ", remarks='" + remarks + '\'' +
                ", keyRight='" + keyRight + '\'' +
                ", keyboardPwdVersion=" + keyboardPwdVersion +
                ", specialValue=" + specialValue +
                ", pwdInfo='" + pwdInfo + '\'' +
                ", timestamp=" + timestamp +
                ", modelNumber='" + modelNumber + '\'' +
                ", hardwareRevision='" + hardwareRevision + '\'' +
                ", firmwareRevision='" + firmwareRevision + '\'' +
                ", username='" + username + '\'' +
                ", senderUsername='" + senderUsername + '\'' +
                ", openid=" + openid + '\'' +
                ", date=" + date +
                '}';
    }
}
