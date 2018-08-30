package cn.jcyh.peephole.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jogger on 2018/1/26.
 */

public class CommandJson implements Parcelable {
    private String command;
    private String commandType;
    private String flag;//附带flag
    private int flag2;//附带flag2

    public CommandJson() {
    }

    protected CommandJson(Parcel in) {
        command = in.readString();
        commandType = in.readString();
        flag = in.readString();
        flag2 = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(command);
        dest.writeString(commandType);
        dest.writeString(flag);
        dest.writeInt(flag2);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CommandJson> CREATOR = new Creator<CommandJson>() {
        @Override
        public CommandJson createFromParcel(Parcel in) {
            return new CommandJson(in);
        }

        @Override
        public CommandJson[] newArray(int size) {
            return new CommandJson[size];
        }
    };

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public int getFlag2() {
        return flag2;
    }

    public void setFlag2(int flag2) {
        this.flag2 = flag2;
    }

    @Override
    public String toString() {
        return "CommandJson{" +
                "command='" + command + '\'' +
                ", commandType='" + commandType + '\'' +
                ", flag='" + flag + '\'' +
                ", flag2=" + flag2 +
                '}';
    }

    public class CommandType {
        public static final int ERROR = -1;
        public static final String BIND_DOORBELL_REQUEST = "bind_doorbell_request";
        public static final String BIND_DOORBELL_RESPONSE = "bind_doorbell_response";
        public static final String BIND_RESPONSE_FLAG_RECEIVED = "1";
        public static final String BIND_RESPONSE_FLAG_BINDED = "2";
        public static final String BIND_RESPONSE_FLAG_REJECT = "0";
        public static final String BIND_RESPONSE_FLAG_ERROR = "3";
        public static final String BIND_DOORBELL_COMPLETED = "bind_doorbell_completed";//接收猫眼绑定成功
        public static final String UNBIND_DOORBELL_COMPLETED = "unbind_doorbell_completed";

        public static final String BIND_DOORBELL_REFRESH = "bind_doorbell_refresh";//猫眼刷新

        public static final String UNLOCK_DOORBELL_REQUEST = "unlock_doorbell_request";//解锁请求
        public static final String UNLOCK_DOORBELL_RESPONSE = "unlock_doorbell_response";//解锁响应

        public static final String DOORBELL_PARAMS_REQUEST = "doorbell_params_request";//参数设置
        public static final String DOORBELL_PARAMS_RESPONSE = "doorbell_params_response";//参数设置响应
        public static final String DOORBELL_PARAMS_GET_REQUEST = "doorbell_params_get_request";//猫眼参数获取
        public static final String DOORBELL_PARAMS_GET_RESPONSE = "doorbell_params_get_response";

        public static final String CHANGE_CAMERA_REQUEST = "change_camera_request";//切换摄像头
        public static final String CHANGE_CAMERA_RESPONSE = "change_camera_response";

        public static final String DOORBELL_NOTIFICATION = "doorbell_notification";//猫眼通知
        public static final String NOTIFICATION_DOORBELL_RING = "doorbell_ring";//有人按门铃
        public static final String NOTIFICATION_DOORBELL_ALARM = "doorbell_alarm";//停留报警
        //视频呼叫图片请求
        public static final String DOORBELL_CALL_IMG_REQUEST = "doorbell_call_img_request";
        public static final String DOORBELL_LASTED_IMG_NAMES_REQUEST =
                "doorbell_lasted_img_names_request";//猫眼最后图片请求
        public static final String DOORBELL_LASTED_IMG_NAMES_RESPONSE =
                "doorbell_lasted_img_names_response";//猫眼最后图片响应
        public static final String DOORBELL_LASTED_IMG_REQUEST = "doorbell_lasted_img_request";
        //请求图片
        public static final String DOORBELL_LASTED_VIDEO_REQUEST =
                "doorbell_lasted_video_request";//视频文件请求
        public static final String DOORBELL_LASTED_VIDEO_RESPONSE =
                "doorbell_lasted_video_response";//视频文件响应
        public static final String DOORBELL_IMG_COMMAND = "image";
        public static final String DOORBELL_VIDEO_COMMAND = "video";

        public static final int DOORBELL_VIDEO_CALL_PARAM = 10;//视频呼叫传输参数
        public static final int DOORBELL_MEDIA_PIC_PARAM = 11;//多媒体图片传输参数
        public static final int DOORBELL_MEDIA_THUMBNAIL_PARAM = 12;//多媒体缩略图片传输参数
        public static final int DOORBELL_MEDIA_VIDEO_PARAM = 13;//多媒体视频文件传输参数
    }
}
