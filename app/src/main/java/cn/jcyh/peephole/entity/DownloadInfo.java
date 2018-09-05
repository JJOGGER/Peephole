package cn.jcyh.peephole.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jogger on 2018/9/4.
 */
public class DownloadInfo implements Parcelable {
    public static final int STATE_NO_DOWNLOAD = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_DOWNLOADED = 2;
    public static final int STATE_DOWNLOAD_PAUSE = 3;
    public static final String TYPE_DOWNLOAD_APK_ID = "TYPE_DOWNLOAD_APK_ID";
    public static final String TYPE_DOWNLOAD_SYSTEM_ID = "TYPE_DOWNLOAD_SYSTEM_ID";
    private String title;
    private String desc;
    private String url;//下载地址
    private String saveFilePath;//存储路径
    private int currentState;
    private String type;

    public DownloadInfo() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSaveFilePath() {
        return saveFilePath;
    }

    public void setSaveFilePath(String saveFilePath) {
        this.saveFilePath = saveFilePath;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private DownloadInfo(Parcel in) {
        title = in.readString();
        desc = in.readString();
        url = in.readString();
        saveFilePath = in.readString();
        currentState = in.readInt();
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeString(url);
        dest.writeString(saveFilePath);
        dest.writeInt(currentState);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        @Override
        public DownloadInfo createFromParcel(Parcel in) {
            return new DownloadInfo(in);
        }

        @Override
        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", url='" + url + '\'' +
                ", saveFilePath='" + saveFilePath + '\'' +
                ", currentState=" + currentState +
                ", type='" + type + '\'' +
                '}';
    }
}
