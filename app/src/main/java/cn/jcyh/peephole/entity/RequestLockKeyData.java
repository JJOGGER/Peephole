package cn.jcyh.peephole.entity;

/**
 * Created by jogger on 2018/11/13.
 */
public class RequestLockKeyData {
    private String cateyeId;

    public RequestLockKeyData( String cateyeId) {
        this.cateyeId = cateyeId;
    }


    public String getCateyeId() {
        return cateyeId;
    }

    public void setCateyeId(String cateyeId) {
        this.cateyeId = cateyeId;
    }

    @Override
    public String toString() {
        return "RequestLockKeyData{" +
                ", cateyeId='" + cateyeId + '\'' +
                '}';
    }
}
