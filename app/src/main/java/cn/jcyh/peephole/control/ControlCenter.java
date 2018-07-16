package cn.jcyh.peephole.control;


import com.google.gson.Gson;

import cn.jcyh.peephole.utils.GsonUtil;

/**
 * Created by Jogger on 2017/4/25.
 * 控制类
 */

public class ControlCenter {
    protected Gson mGson;
    ControlCenter() {
        mGson = GsonUtil.getGson();
    }

}
