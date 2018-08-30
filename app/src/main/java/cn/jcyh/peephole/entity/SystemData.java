package cn.jcyh.peephole.entity;

/**
 * Created by jogger on 2018/8/17.
 */
public class SystemData {
    private int resID;
    private String name;

    public int getResID() {
        return resID;
    }

    public void setResID(int resID) {
        this.resID = resID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SystemData{" +
                "resID=" + resID +
                ", name='" + name + '\'' +
                '}';
    }
}
