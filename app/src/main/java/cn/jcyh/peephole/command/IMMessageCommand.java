package cn.jcyh.peephole.command;


import cn.jcyh.peephole.entity.DoorbellModelParam;

/**
 * Created by jogger on 2018/8/1.
 */
public interface IMMessageCommand {
    void sendDoorbellModeParamsResponse(String account, DoorbellModelParam doorbellModelParam);

    void sendDoorbellSensorParamsResponse(String account, DoorbellModelParam doorbellModelParam);
}
