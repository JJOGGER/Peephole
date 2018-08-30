package cn.jcyh.peephole.command;


import cn.jcyh.peephole.entity.DoorbellParam;

/**
 * Created by jogger on 2018/8/1.
 */
public interface IMMessageCommand {
    void sendDoorbellModeParamsResponse(String account, DoorbellParam doorbellParam);

    void sendDoorbellSensorParamsResponse(String account, DoorbellParam doorbellParam);
}
