package cn.jcyh.peephole.entity;

/**
 * Created by jogger on 2018/11/12.
 */
public class RequestUploadElectricQuantity {
    private String clientId;
    private String accessToken;
    private int lockId;
    private int electricQuantity;
    private long date;

    public RequestUploadElectricQuantity(String clientId, String accessToken, int lockId, int electricQuantity, long date) {
        this.clientId = clientId;
        this.accessToken = accessToken;
        this.lockId = lockId;
        this.electricQuantity = electricQuantity;
        this.date = date;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getLockId() {
        return lockId;
    }

    public void setLockId(int lockId) {
        this.lockId = lockId;
    }

    public int getElectricQuantity() {
        return electricQuantity;
    }

    public void setElectricQuantity(int electricQuantity) {
        this.electricQuantity = electricQuantity;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "RequestUploadElectricQuantity{" +
                "clientId='" + clientId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", lockId=" + lockId +
                ", electricQuantity=" + electricQuantity +
                ", date=" + date +
                '}';
    }
}
