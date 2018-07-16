package cn.jcyh.peephole.entity;

/**
 * Created by jogger on 2018/6/9.
 */
public class Advert {
    private String picUrl;
    private int timer;

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    @Override
    public String toString() {
        return "Advert{" +
                "picUrl='" + picUrl + '\'' +
                ", timer=" + timer +
                '}';
    }
}
