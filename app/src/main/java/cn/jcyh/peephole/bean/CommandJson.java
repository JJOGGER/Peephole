package cn.jcyh.peephole.bean;

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

    public class CommandType {
        public static final String BIND_DOORBELL_REQUEST = "bind_doorbell_request";
        public static final String BIND_DOORBELL_RESPONSE = "bind_doorbell_response";

        public static final String BIND_DOORBELL_COMPLETED = "bind_doorbell_completed";//接收猫眼绑定成功
    }
}
