package cn.jcyh.peephole.entity;

/**
 * Created by jogger on 2018/7/18.
 * 猫眼登录云端返回对象
 */
public class Doorbell {
    private String deviceUserId;
    private String token;
    private String name;

    public String getDeviceUserId() {
        return deviceUserId;
    }

    public void setDeviceUserId(String deviceUserId) {
        this.deviceUserId = deviceUserId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Doorbell{" +
                "deviceUserId='" + deviceUserId + '\'' +
                ", token='" + token + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
