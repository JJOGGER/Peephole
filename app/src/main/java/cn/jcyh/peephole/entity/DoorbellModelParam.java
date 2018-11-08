package cn.jcyh.peephole.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jogger on 2018/2/23.
 */

public class DoorbellModelParam implements Parcelable {
    private int netPush = 1;
    private int videotap = 0;
    private int videoCall = 1;
    private int leaveMessage = 0;

    public DoorbellModelParam() {
    }

    protected DoorbellModelParam(Parcel in) {
        netPush = in.readInt();
        videotap = in.readInt();
        videoCall = in.readInt();
        leaveMessage = in.readInt();
    }

    public static final Creator<DoorbellModelParam> CREATOR = new Creator<DoorbellModelParam>() {
        @Override
        public DoorbellModelParam createFromParcel(Parcel in) {
            return new DoorbellModelParam(in);
        }

        @Override
        public DoorbellModelParam[] newArray(int size) {
            return new DoorbellModelParam[size];
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

    public int getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(int leaveMessage) {
        this.leaveMessage = leaveMessage;
    }

    @Override
    public String toString() {
        return "DoorbellModelParam{" +
                "netPush=" + netPush +
                ", videotap=" + videotap +
                ", videoCall=" + videoCall +
                ", leaveMessage=" + leaveMessage +
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
        dest.writeInt(leaveMessage);
    }
}
