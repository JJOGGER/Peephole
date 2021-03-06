package cn.jcyh.peephole.entity;

import android.text.TextUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import cn.jcyh.peephole.constant.Constant;

/**
 * Created by Jogger on 2018/4/20.
 * 猫眼默认设置
 */

public class DoorbellConfig {
    //从本地取，如果为空，则去服务器取，如果有，存到本地，如果没有，创建，并保存到服务器
    private static final String DEFAULT_RING = "ring/doorbell01.mp3";
    private static final String DEFAULT_ALARM = "alarm/alarm01.mp3";
    private DoorbellModelParam doorbellModelParam = new DoorbellModelParam();
    private DoorbellSensorParam doorbellSensorParam = new DoorbellSensorParam();
    private int faceRecognize = 0;//人脸识别
    private int voiceprintRecognize = 0;//声纹识别
    private String nickName = "";
    private int autoSensorTime = 5;
    private int videoLeaveMsgTime = 5;//猫眼留言时间
    private int videotapTime = 5;//录像时间
    private int doorbellLookTime = 10;//猫眼查看时间
    private String doorbellRingName;//门铃声
    private String doorbellAlarmName;//报警声
    private String customDoorbellRingName;
    private String customDoorbellAlarmName;
    private int ringVolume = 50;
    private int alarmVolume = 50;
    private int videoVolume = 1;//通话音量
    private boolean isExistOfflineData = false;//标记是否存在离线数据
    private Doorbell doorbell;//网易云登录
    private int multiVideo = 0;
    private ConfigData.VideoConfig videoConfig;//视频通话配置

    public DoorbellModelParam getDoorbellModelParam() {
        if (doorbellModelParam == null) {
            doorbellModelParam = new DoorbellModelParam();
        }
        if (doorbellModelParam.getLeaveMessage() == 1) {
            doorbellModelParam.setVideotap(0);
            doorbellModelParam.setVideoCall(0);
        }
        return doorbellModelParam;
    }

    public void setDoorbellModelParam(DoorbellModelParam doorbellModelParam) {
        this.doorbellModelParam = doorbellModelParam;
    }

    public DoorbellSensorParam getDoorbellSensorParam() {
        if (doorbellSensorParam == null)
            doorbellSensorParam = new DoorbellSensorParam();
        return doorbellSensorParam;
    }

    public void setDoorbellSensorParam(DoorbellSensorParam doorbellSensorParam) {
        this.doorbellSensorParam = doorbellSensorParam;
    }

    public int getFaceRecognize() {
        return faceRecognize;
    }

    public void setFaceRecognize(int faceRecognize) {
        this.faceRecognize = faceRecognize;
    }

    public int getVoiceprintRecognize() {
        return voiceprintRecognize;
    }

    public void setVoiceprintRecognize(int voiceprintRecognize) {
        this.voiceprintRecognize = voiceprintRecognize;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getAutoSensorTime() {
        return autoSensorTime;
    }

    public void setAutoSensorTime(int autoSensorTime) {
        this.autoSensorTime = autoSensorTime;
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

    public String getDoorbellRingName() {
        return TextUtils.isEmpty(doorbellRingName) ? DEFAULT_RING : doorbellRingName;
    }

    public String getShowDoorbellRingName() {
        return (TextUtils.isEmpty(doorbellRingName) ? DEFAULT_RING : doorbellRingName).replace(Constant.ASSET_RING +
                File.separator, "");
    }

    public void setDoorbellRingName(String doorbellRingName) {
        this.doorbellRingName = doorbellRingName;
    }

    public String getDoorbellAlarmName() {
        return TextUtils.isEmpty(doorbellAlarmName) ? DEFAULT_ALARM : doorbellAlarmName;
    }

    public String getShowDoorbellAlarmName() {
        return (TextUtils.isEmpty(doorbellAlarmName) ? DEFAULT_ALARM : doorbellAlarmName).replace(Constant.ASSET_ALARM +
                File.separator, "");
    }

    public String getCustomDoorbellRingName() {
        File file = null;
        if (customDoorbellRingName != null)
            file = new File(customDoorbellRingName);
        if (file == null) return null;
        if (file.exists())
            return customDoorbellRingName;
        return null;
    }

    public void setCustomDoorbellRingName(String customDoorbellRingName) {
        this.customDoorbellRingName = customDoorbellRingName;
    }

    public String getCustomDoorbellAlarmName() {
        File file = null;
        if (customDoorbellAlarmName != null)
            file = new File(customDoorbellAlarmName);
        if (file == null) return null;
        if (file.exists())
            return customDoorbellAlarmName;
        return null;
    }

    public void setCustomDoorbellAlarmName(String customDoorbellAlarmName) {
        this.customDoorbellAlarmName = customDoorbellAlarmName;
    }

    public void setDoorbellAlarmName(String doorbellAlarmName) {
        this.doorbellAlarmName = doorbellAlarmName;
    }

    public int getVideoVolume() {
        return videoVolume == 0 ? 1 : videoVolume;
    }

    public void setVideoVolume(int videoVolume) {
        this.videoVolume = videoVolume;
    }

    public int getRingVolume() {
        return ringVolume;
    }

    public void setRingVolume(int ringVolume) {
        this.ringVolume = ringVolume;
    }

    public int getAlarmVolume() {
        return alarmVolume;
    }

    public void setAlarmVolume(int alarmVolume) {
        this.alarmVolume = alarmVolume;
    }

    public boolean isExistOfflineData() {
        return isExistOfflineData;
    }

    public void setExistOfflineData(boolean existOfflineData) {
        isExistOfflineData = existOfflineData;
    }

    public Doorbell getDoorbell() {
        return doorbell == null ? new Doorbell() : doorbell;
    }

    public void setDoorbell(Doorbell doorbell) {
        this.doorbell = doorbell;
    }

    public ConfigData.VideoConfig getVideoConfig() {
        return videoConfig == null ? new ConfigData.VideoConfig() : videoConfig;
    }

    public void setVideoConfig(ConfigData.VideoConfig videoConfig) {
        this.videoConfig = videoConfig;
    }

    public boolean isMultiVideo() {
        return multiVideo == 1;
    }

    public void setMultiVideo(int multiVideo) {
        this.multiVideo = multiVideo;
    }

    public static Gson getGson() {
        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                String name = f.getName();
                return
                        ("nickName".equals(name) ||
                                "autoSensorTime".equals(name) ||
                                "videoLeaveMsgTime".equals(name) ||
                                "videotapTime".equals(name) ||
                                "doorbellLookTime".equals(name) ||
                                "doorbellRingName".equals(name) ||
                                "doorbellAlarmName".equals(name) ||
                                "ringVolume".equals(name) ||
                                "alarmVolume".equals(name) ||
                                "isExistOfflineData".equals(name) ||
                                "doorbell".equals(name) ||
                                "videoConfig".equals(name)
                        );
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();
    }

    @Override
    public String toString() {
        return "DoorbellConfig{" +
                "nickName='" + nickName + '\'' +
                ", doorbellModelParam=" + doorbellModelParam +
                ", doorbellSensorParam=" + doorbellSensorParam +
                ", autoSensorTime=" + autoSensorTime +
                ", videoLeaveMsgTime=" + videoLeaveMsgTime +
                ", videotapTime=" + videotapTime +
                ", doorbellLookTime=" + doorbellLookTime +
                ", doorbellRingName='" + doorbellRingName + '\'' +
                ", doorbellAlarmName='" + doorbellAlarmName + '\'' +
                ", ringVolume=" + ringVolume +
                ", alarmVolume=" + alarmVolume +
                ", isExistOfflineData=" + isExistOfflineData +
                ", faceRecognize=" + faceRecognize +
                ", voiceprintRecognize=" + voiceprintRecognize +
                ", doorbell=" + doorbell +
                ", videoConfig=" + videoConfig +
                '}';
    }
}
