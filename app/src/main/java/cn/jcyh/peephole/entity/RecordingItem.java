package cn.jcyh.peephole.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 录音文件的实体类
 *
 * Created by developerHaoz on 2017/8/12.
 */

public class RecordingItem implements Parcelable {
    private String mName;
    private String mFilePath;
    private int mId;
    private int mLength;
    private long mTime;
    private int mType;//门铃.报警
    private boolean mIsRecord;//标记文件选取还是录制

    public RecordingItem() {
    }

    protected RecordingItem(Parcel in) {
        mName = in.readString();
        mFilePath = in.readString();
        mId = in.readInt();
        mLength = in.readInt();
        mTime = in.readLong();
        mType = in.readInt();
        mIsRecord = in.readByte() != 0;
    }

    public static final Creator<RecordingItem> CREATOR = new Creator<RecordingItem>() {
        @Override
        public RecordingItem createFromParcel(Parcel in) {
            return new RecordingItem(in);
        }

        @Override
        public RecordingItem[] newArray(int size) {
            return new RecordingItem[size];
        }
    };

    public boolean isRecord() {
        return mIsRecord;
    }

    public void setRecord(boolean record) {
        mIsRecord = record;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        mLength = length;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getType() {
        return mType;
    }

    /**
     * @link Constant
     */
    public void setType(int type) {
        mType = type;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mFilePath);
        parcel.writeInt(mId);
        parcel.writeInt(mLength);
        parcel.writeLong(mTime);
        parcel.writeInt(mType);
        parcel.writeByte((byte) (mIsRecord ? 1 : 0));
    }
}
