package cn.jcyh.peephole.entity;

/**
 * Created by jogger on 2018/11/14.
 */
public class LogRecord {
    // 报警类型：1-猫眼电量低 2-猫眼离线 3-门锁电量低 4-门锁网络异常 5-门锁开锁多次失败
    public static final int TYPE_LOCK = 1;
    public static final int TYPE_DOORBELL = 2;
    public static final int ALARM_DOORBELL_LOW_POWER = 1;
    public static final int ALARM_DOORBELL_OFFLINE = 2;
    public static final int ALARM_LOCK_LOW_POWER = 3;
    public static final int ALARM_LOCK_OFFLINE = 4;
    public static final int ALARM_UNLOCK_FAILURE = 5;
    private long timestamp;
    private String deviceId;
    private int alarmType = 2;
    private String remark;
    private int deviceType = TYPE_DOORBELL;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String toString() {
        return "LogRecord{" +
                "timestamp=" + timestamp +
                ", deviceId='" + deviceId + '\'' +
                ", alarmType=" + alarmType +
                ", remark='" + remark + '\'' +
                ", deviceType=" + deviceType +
                '}';
    }
}
