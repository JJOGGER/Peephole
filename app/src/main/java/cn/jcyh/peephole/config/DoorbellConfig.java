package cn.jcyh.peephole.config;

import cn.jcyh.peephole.bean.DoorbellParam;

/**
 * Created by Jogger on 2018/4/20.
 * 猫眼默认设置
 */

public class DoorbellConfig {
    //    private int netPush;
//    private int videotap;
//    private int videoCall;
//    private int sendMsg;
//    private int dial;
//    private int leaveMessage;
//    private int ringAlarm;
//    private int monitor;
    //从本地取，如果为空，则去服务器取，如果有，存到本地，如果没有，创建，并保存到服务器
    private DoorbellParam mDoorbellParam;
    private int doorbellNetPush;
    private int doorbellVideoCall;
    private int doorbellSendMsg;
    private int doorbellDial;
    private int doorbellLeaveMessage;
    private int doorbellRingAlarm;
    private int monitorSwitch;
    private int sensorNetPush;
    private int sensorVideoCall;
    private int sensorSendMsg;
    private int sensorDial;
    private int sensorLeaveMessage;
    private int doorbellMonitor;
}
