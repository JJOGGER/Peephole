package cn.jcyh.peephole.control;



/**
 * Created by Jogger on 2017/4/25.
 * 控制类
 */

public class ControlCenter {

    private static ControlCenter instance;

    public static ControlCenter getInstance() {
        if (instance == null) {
            instance = new ControlCenter();
        }
        return instance;
    }


}
