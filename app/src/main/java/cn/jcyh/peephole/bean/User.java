package cn.jcyh.peephole.bean;

/**
 * Created by jogger on 2018/2/8.
 */

public class User {
    private String account;
    private String aid;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
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
