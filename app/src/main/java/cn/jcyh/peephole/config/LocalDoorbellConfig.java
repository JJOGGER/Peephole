package cn.jcyh.peephole.config;

/**
 * Created by jogger on 2018/4/25.
 * 本地猫眼设置
 */

public class LocalDoorbellConfig {
    private String masterNumber;
    private String sosNumber;
    private int videoLeaveMsgTime;
    private int videotapTime;//录像时间
    private int doorbellLookTime;//猫眼查看时间

    public String getMasterNumber() {
        return masterNumber;
    }

    public void setMasterNumber(String masterNumber) {
        this.masterNumber = masterNumber;
    }

    public String getSosNumber() {
        return sosNumber;
    }

    public void setSosNumber(String sosNumber) {
        this.sosNumber = sosNumber;
    }

    public int getVideoLeaveMsgTime() {
        return videoLeaveMsgTime;
    }

    public void setVideoLeaveMsgTime(int videoLeaveMsgTime) {
        this.videoLeaveMsgTime = videoLeaveMsgTime;
    }

    public int getVideotapTime() {
        return videotapTime;
    }

    public void setVideotapTime(int videotapTime) {
        this.videotapTime = videotapTime;
    }

    public int getDoorbellLookTime() {
        return doorbellLookTime;
    }

    public void setDoorbellLookTime(int doorbellLookTime) {
        this.doorbellLookTime = doorbellLookTime;
    }
}
