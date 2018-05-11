package cn.jcyh.peephole.config;

import android.text.TextUtils;

import cn.jcyh.peephole.bean.DoorbellParam;

/**
 * Created by Jogger on 2018/4/20.
 * 猫眼默认设置
 */

public class DoorbellConfig {
    //从本地取，如果为空，则去服务器取，如果有，存到本地，如果没有，创建，并保存到服务器
    private int doorbellNetPush = 1;
    private int doorbellVideoCall = 1;
    private int doorbellVideotap = 0;//录像
    private int doorbellSendMsg = 0;
    private int doorbellDial = 0;
    private int doorbellLeaveMessage = 0;
    private int monitorSwitch = 0;
    private int sensorNetPush = 1;
    private int sensorVideoCall = 1;
    private int sensorVideotap = 0;
    private int sensorSendMsg = 0;
    private int sensorDial = 0;
    private int sensorRingAlarm = 0;//铃声报警
    //    private int videoTime = 5;//猫眼录制时间
//    private int doorbellSelect = 15;//猫眼查看时间
//    private int leaveMessageTime = 20;//猫眼留言时间
    private int autoSensorTime = 5;
    private String masterNumber;//主人号码
    private String sosNumber;//sos号码
    private int videoLeaveMsgTime = 20;//猫眼留言时间
    private int videotapTime = 5;//录像时间
    private int doorbellLookTime = 15;//猫眼查看时间
    private String mDoorbellRingName;//门铃声
    private String mDoorbellAlarmName;//报警声
    private int mRingVolume = 50;
    private int mAlarmVolume = 50;

    public int getDoorbellNetPush() {
        return doorbellNetPush;
    }

    public void setDoorbellNetPush(int doorbellNetPush) {
        this.doorbellNetPush = doorbellNetPush;
    }

    public int getDoorbellVideoCall() {
        return doorbellVideoCall;
    }

    public void setDoorbellVideoCall(int doorbellVideoCall) {
        this.doorbellVideoCall = doorbellVideoCall;
    }

    public int getDoorbellVideotap() {
        return doorbellVideotap;
    }

    public void setDoorbellVideotap(int doorbellVideotap) {
        this.doorbellVideotap = doorbellVideotap;
    }

    public int getDoorbellSendMsg() {
        return doorbellSendMsg;
    }

    public void setDoorbellSendMsg(int doorbellSendMsg) {
        this.doorbellSendMsg = doorbellSendMsg;
    }

    public int getDoorbellDial() {
        return doorbellDial;
    }

    public void setDoorbellDial(int doorbellDial) {
        this.doorbellDial = doorbellDial;
    }

    public int getSensorVideotap() {
        return sensorVideotap;
    }

    public void setSensorVideotap(int sensorVideotap) {
        this.sensorVideotap = sensorVideotap;
    }

    public int getDoorbellLeaveMessage() {
        return doorbellLeaveMessage;
    }

    public void setDoorbellLeaveMessage(int doorbellLeaveMessage) {
        this.doorbellLeaveMessage = doorbellLeaveMessage;
    }

    public int getMonitorSwitch() {
        return monitorSwitch;
    }

    public void setMonitorSwitch(int monitorSwitch) {
        this.monitorSwitch = monitorSwitch;
    }

    public int getAutoSensorTime() {
        return autoSensorTime;
    }

    public void setAutoSensorTime(int autoSensorTime) {
        this.autoSensorTime = autoSensorTime;
    }

    public int getSensorNetPush() {
        return sensorNetPush;
    }

    public void setSensorNetPush(int sensorNetPush) {
        this.sensorNetPush = sensorNetPush;
    }

    public int getSensorVideoCall() {
        return sensorVideoCall;
    }

    public void setSensorVideoCall(int sensorVideoCall) {
        this.sensorVideoCall = sensorVideoCall;
    }

    public int getSensorSendMsg() {
        return sensorSendMsg;
    }

    public void setSensorSendMsg(int sensorSendMsg) {
        this.sensorSendMsg = sensorSendMsg;
    }

    public int getSensorDial() {
        return sensorDial;
    }

    public void setSensorDial(int sensorDial) {
        this.sensorDial = sensorDial;
    }

    public int getSensorRingAlarm() {
        return sensorRingAlarm;
    }

    public void setSensorRingAlarm(int sensorRingAlarm) {
        this.sensorRingAlarm = sensorRingAlarm;
    }

    public String getMasterNumber() {
        return masterNumber == null ? "" : masterNumber;
    }

    public void setMasterNumber(String masterNumber) {
        this.masterNumber = masterNumber;
    }

    public String getSosNumber() {
        return sosNumber == null ? "" : sosNumber;
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

    public String getDoorbellRingName() {
        return TextUtils.isEmpty(mDoorbellRingName) ? "ring_one.mp3" : mDoorbellRingName;
    }

    public void setDoorbellRingName(String doorbellRingName) {
        mDoorbellRingName = doorbellRingName;
    }

    public String getDoorbellAlarmName() {
        return TextUtils.isEmpty(mDoorbellAlarmName) ? "alarm_one.mp3" : mDoorbellAlarmName;
    }

    public void setDoorbellAlarmName(String doorbellAlarmName) {
        mDoorbellAlarmName = doorbellAlarmName;
    }

    public int getRingVolume() {
        return mRingVolume;
    }

    public void setRingVolume(int ringVolume) {
        mRingVolume = ringVolume;
    }

    public int getAlarmVolume() {
        return mAlarmVolume;
    }

    public void setAlarmVolume(int alarmVolume) {
        mAlarmVolume = alarmVolume;
    }

    public void setDoorbellParams(DoorbellParam doorbellParams) {
        this.doorbellNetPush = doorbellParams.getNetPush();
        this.doorbellVideotap = doorbellParams.getVideotap();
        this.doorbellDial = doorbellParams.getDial();
        this.doorbellLeaveMessage = doorbellParams.getLeaveMessage();
        this.doorbellVideoCall = doorbellParams.getVideoCall();
        this.doorbellSendMsg = doorbellParams.getSendMsg();
    }

    public void setMonitorParams(DoorbellParam doorbellParams) {
        this.sensorNetPush = doorbellParams.getNetPush();
        this.sensorVideotap = doorbellParams.getVideotap();
        this.sensorDial = doorbellParams.getDial();
        this.sensorRingAlarm = doorbellParams.getRingAlarm();
        this.sensorVideoCall = doorbellParams.getVideoCall();
        this.sensorSendMsg = doorbellParams.getSendMsg();
        this.monitorSwitch = doorbellParams.getMonitor();
    }

    @Override
    public String toString() {
        return "DoorbellConfig{" +
                "doorbellNetPush=" + doorbellNetPush +
                ", doorbellVideoCall=" + doorbellVideoCall +
                ", doorbellVideotap=" + doorbellVideotap +
                ", doorbellSendMsg=" + doorbellSendMsg +
                ", doorbellDial=" + doorbellDial +
                ", doorbellLeaveMessage=" + doorbellLeaveMessage +
                ", monitorSwitch=" + monitorSwitch +
                ", sensorNetPush=" + sensorNetPush +
                ", sensorVideoCall=" + sensorVideoCall +
                ", sensorVideotap=" + sensorVideotap +
                ", sensorSendMsg=" + sensorSendMsg +
                ", sensorDial=" + sensorDial +
                ", sensorRingAlarm=" + sensorRingAlarm +
                ", autoSensorTime=" + autoSensorTime +
                ", masterNumber='" + masterNumber + '\'' +
                ", sosNumber='" + sosNumber + '\'' +
                ", videoLeaveMsgTime=" + videoLeaveMsgTime +
                ", videotapTime=" + videotapTime +
                ", doorbellLookTime=" + doorbellLookTime +
                '}';
    }
}
