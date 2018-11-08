package cn.jcyh.peephole.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jogger on 2018/2/23.
 */

public class DoorbellSensorParam implements Parcelable {
    private int netPush=1;
    private int videotap=0;
    private int videoCall=1;
    private int ringAlarm=0;
    private int monitor=0;

    public DoorbellSensorParam() {
    }

    protected DoorbellSensorParam(Parcel in) {
        netPush = in.readInt();
        videotap = in.readInt();
        videoCall = in.readInt();
        ringAlarm = in.readInt();
        monitor = in.readInt();
    }

    public static final Creator<DoorbellSensorParam> CREATOR = new Creator<DoorbellSensorParam>() {
        @Override
        public DoorbellSensorParam createFromParcel(Parcel in) {
            return new DoorbellSensorParam(in);
        }

        @Override
        public DoorbellSensorParam[] newArray(int size) {
            return new DoorbellSensorParam[size];
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
        return "DoorbellModelParam{" +
                "netPush=" + netPush +
                ", videotap=" + videotap +
                ", videoCall=" + videoCall +
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
        dest.writeInt(ringAlarm);
        dest.writeInt(monitor);
    }
}
