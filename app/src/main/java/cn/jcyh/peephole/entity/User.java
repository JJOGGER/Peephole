package cn.jcyh.peephole.entity;

/**
 * Created by jogger on 2018/2/8.
 */

public class User {
    private String account;
    private int aid;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    @Override
    public String toString() {
        return "User{" +
                "account='" + account + '\'' +
                ", aid=" + aid +
                '}';
    }
}
