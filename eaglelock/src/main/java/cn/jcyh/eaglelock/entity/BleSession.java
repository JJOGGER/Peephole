package cn.jcyh.eaglelock.entity;


import android.os.Bundle;


/**
 * 蓝牙锁操作
 */
public class BleSession {

    /**
     * 操作
     */
    private String operation;

    /**
     * mac地址
     */
    private String lockmac;

    private Bundle argments;

    public String getOperation() {
        return operation==null?"":operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getLockmac() {
        return lockmac;
    }

    public void setLockmac(String lockmac) {
        this.lockmac = lockmac;
    }

    private BleSession() {
    }

    public void setArgments(Bundle bundle) {
        argments = bundle;
    }

    public Bundle getArgments() {
        return argments;
    }

    public static BleSession getInstance(String operation, String lockmac) {
        BleSession bleSession = new BleSession();
        bleSession.setOperation(operation);
        bleSession.setLockmac(lockmac);
        return bleSession;
    }
}
