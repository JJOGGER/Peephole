package cn.jcyh.peephole.bean;

import java.io.Serializable;

/**
 * Created by jogger on 2017/8/1.
 * anychat传输任务
 */

public class AnyChatTask implements Serializable{
    private String type;
    private String name;//请求的文件名
    private int tastId;//anychat任务传输id
    private String status;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTastId() {
        return tastId;
    }

    public void setTastId(int tastId) {
        this.tastId = tastId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AnyChatTask{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", tastId=" + tastId +
                ", status='" + status + '\'' +
                '}';
    }
}
