package cn.jcyh.peephole.config;

/**
 * Created by Jogger on 2018/4/20.
 * 猫眼默认设置
 */

public class DoorbellConfig {
    //    private int netPush;
//    private int videotap;
//    private int videoCall;
//    private int sendMsg;
//    private int dial;
//    private int leaveMessage;
//    private int ringAlarm;
//    private int monitor;
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
    private int videoTime = 5;//猫眼录制时间
    private int doorbellSelect = 15;//猫眼查看时间
    private int leaveMessageTime = 20;//猫眼留言时间

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


    public int getVideoTime() {
        return videoTime;
    }

    public void setVideoTime(int videoTime) {
        this.videoTime = videoTime;
    }

    public int getDoorbellSelect() {
        return doorbellSelect;
    }

    public void setDoorbellSelect(int doorbellSelect) {
        this.doorbellSelect = doorbellSelect;
    }

    public int getLeaveMessageTime() {
        return leaveMessageTime;
    }

    public void setLeaveMessageTime(int leaveMessageTime) {
        this.leaveMessageTime = leaveMessageTime;
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
                ", videoTime=" + videoTime +
                ", doorbellSelect=" + doorbellSelect +
                ", leaveMessageTime=" + leaveMessageTime +
                '}';
    }
}
