package cn.jcyh.peephole.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jogger on 2018/2/23.
 */

public class DoorbellParam implements Parcelable {
    private int netPush;
    private int videotap;
    private int videoCall;
    private int sendMsg;
    private int dial;
    private int leaveMessage;
    private int ringAlarm;
    private int monitor;

    public DoorbellParam() {
    }

    protected DoorbellParam(Parcel in) {
        netPush = in.readInt();
        videotap = in.readInt();
        videoCall = in.readInt();
        sendMsg = in.readInt();
        dial = in.readInt();
        leaveMessage = in.readInt();
        ringAlarm = in.readInt();
        monitor = in.readInt();
    }

    public static final Creator<DoorbellParam> CREATOR = new Creator<DoorbellParam>() {
        @Override
        public DoorbellParam createFromParcel(Parcel in) {
            return new DoorbellParam(in);
        }

        @Override
        public DoorbellParam[] newArray(int size) {
            return new DoorbellParam[size];
        }
    };

    public int getNetPush() {
        return netPush;
    }

    public void setNetPush(int netPush) {
        this.netPush = netPush;
    }

    public int getVideotap() {
        return videotap;
    }

    public void setVideotap(int videotap) {
        this.videotap = videotap;
    }

    public int getVideoCall() {
        return videoCall;
    }

    public void setVideoCall(int videoCall) {
        this.videoCall = videoCall;
    }

    public int getSendMsg() {
        return sendMsg;
    }

    public void setSendMsg(int sendMsg) {
        this.sendMsg = sendMsg;
    }

    public int getDial() {
        return dial;
    }

    public void setDial(int dial) {
        this.dial = dial;
    }

    public int getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(int leaveMessage) {
        this.leaveMessage = leaveMessage;
    }

    public int getRingAlarm() {
        return ringAlarm;
    }

    public void setRingAlarm(int ringAlarm) {
        this.ringAlarm = ringAlarm;
    }

    public int getMonitor() {
        return monitor;
    }

    public void setMonitor(int monitor) {
        this.monitor = monitor;
    }

    @Override
    public String toString() {
        return "DoorbellParam{" +
                "netPush=" + netPush +
                ", videotap=" + videotap +
                ", videoCall=" + videoCall +
                ", sendMsg=" + sendMsg +
                ", dial=" + dial +
                ", leaveMessage=" + leaveMessage +
                ", ringAlarm=" + ringAlarm +
                ", monitor=" + monitor +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(netPush);
        dest.writeInt(videotap);
        dest.writeInt(videoCall);
        dest.writeInt(sendMsg);
        dest.writeInt(dial);
        dest.writeInt(leaveMessage);
        dest.writeInt(ringAlarm);
        dest.writeInt(monitor);
    }
}
