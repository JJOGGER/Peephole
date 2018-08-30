package cn.jcyh.peephole.manager.impl;

import android.os.IBinder;
import android.os.IHardwareService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;

import com.cust.service.ICustService;

import java.lang.reflect.Method;

import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.manager.IBCManager;
import cn.jcyh.peephole.utils.Util;

/**
 * Created by jogger on 2018/8/6.猫眼硬件控制
 */
public class BCManager implements IBCManager {
    private static final String IR_LED_EN_STATE = "/sys/devices/platform/CUSTDriver/driver/IrLedEnState";
    private static final String PIR_EN_STATE = "/sys/devices/platform/CUSTDriver/driver/PirEnState";
    private static final String PIR_STATE = "/sys/devices/platform/CUSTDriver/driver/PirState";
    private static final String LOCK_DETECT_STATE = "/sys/devices/platform/CUSTDriver/driver/LockDetectState";
    private static final String LIGHT_SENSOR_VALUE = "/sys/devices/platform/CUSTDriver/driver/LightSensor";
    private static final String SPK1_STATE = "/sys/bus/platform/drivers/mt-soc-codec/ExtspkampState";
    private static final String SPK2_STATE = "/sys/bus/platform/drivers/mt-soc-codec/Extspkamp2State";
    private static final String RING_KEY_LED_STATE = "/sys/class/leds/button-backlight/brightness";
    private static final String LOCK_STATE = "/sys/devices/platform/CUSTDriver/driver/LockEnState";
    private static final String PREFIX = "eogleking_";
    private static final int SPEAKER_MAIN = 1;
    private static final int SPEAKER_EXTEND = 0;

    private static final int LIGHT_ID_BUTTONS = 2;

    private IHardwareService localhardwareservice;
    private ICustService ics;

    public BCManager() {
        try {
            Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"hardware"});
            localhardwareservice = IHardwareService.Stub.asInterface(binder);
            IBinder binder2 = ServiceManager.getService("CustService"); //
            ics = ICustService.Stub.asInterface(binder2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum INT_TYPE {
        OURDOOR_PRESS, INDOOR_PRESS, TAMPER, PIR//, WIFI
    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        //unRegisterBcRecInt();
    }

    /**
     * PIR传感器 状态查询
     *
     * @return true 表示有人  false  表示没有人
     */
    @Override
    public boolean getPIRStatus() {
        boolean variable = false;
        try {
            int r = ics.readSysFileStatusInt(PIR_STATE);
            variable = r == 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return variable;
    }

    /**
     * 查询pir传感器的状态
     */
    @Override
    public boolean getPIRSensorOn() {
        boolean variable = false;
        try {
            int r = ics.readSysFileStatusInt(PIR_EN_STATE);
            variable = r == 1;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return variable;
    }

    /**
     * 控制pir传感器打开或关闭
     */
    @Override
    public void setPIRSensorOn(boolean on) {
        if (ics == null) return;
        if (on) {
            try {
                ics.writeSysFileStatusInt(PIR_EN_STATE, 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ics.writeSysFileStatusInt(PIR_EN_STATE, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 防拆感应 状态查询
     *
     * @return true 表示拆掉了, false 表示正常
     */
    @Override
    public boolean getTamperSensorStatus() {
        boolean variable = false;
        try {
            int r = ics.readSysFileStatusInt(LOCK_DETECT_STATE);
            variable = r == 1;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return variable;
    }

    /**
     * 控制红外灯打开和关闭 true - 打开, false - 关闭
     */
    @Override
    public void setInfraredLightPowerOn(boolean isPowerOn) {
        if (isPowerOn) {
            try {
                ics.writeSysFileStatusInt(IR_LED_EN_STATE, 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ics.writeSysFileStatusInt(IR_LED_EN_STATE, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 红外灯 状态查询
     *
     * @return true 表示打开  false 表示关闭
     */
    @Override
    public boolean getInfraredLightStatus() {
        boolean variable = false;
        int r;
        try {
            r = ics.readSysFileStatusInt(IR_LED_EN_STATE);
            variable = r == 1;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return variable;
    }

    /**
     * 控制喇叭的打开和关闭
     *
     * @param speakerId 音频输出设备(speaker0/speaker1) - (0/1)1:MAIN 2:EXTEND
     * @param isPowerOn true - 打开, false - 关闭
     */
    @Override
    public void setSpeakerPowerOn(int speakerId, boolean isPowerOn) {
        if (speakerId == 0) {
            try {
                ics.writeSysFileStatusInt(SPK1_STATE, isPowerOn ? 1 : 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (speakerId == 1) {
            try {
                ics.writeSysFileStatusInt(SPK2_STATE, isPowerOn ? 1 : 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 喇叭状态查询
     *
     * @param speakerId 音频输出设备(speaker0/speaker1) - (0/1)
     * @return true 为打开  false 为关闭
     */
    @Override
    public boolean getSpeakerStatus(int speakerId) {
        boolean variable;
        int r = 0;
        if (speakerId == 0) {
            try {
                r = ics.readSysFileStatusInt(SPK1_STATE);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (speakerId == 1) {
            try {
                r = ics.readSysFileStatusInt(SPK2_STATE);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        variable = r == 1;
        return variable;
    }

    /**
     * 打开关闭led
     */
    @Override
    public void setRingKeyLedOn(boolean on) {
        if (on) {
            try {
                localhardwareservice.setColor(LIGHT_ID_BUTTONS, 0xffff0000);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                localhardwareservice.turnOff(LIGHT_ID_BUTTONS);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 取得门铃按键灯状态
     *
     * @return true 表示是亮的  false 表示是关的
     */
    @Override
    public boolean getRingKeyLedStatus() {
        boolean variable = false;
        try {
            int r = ics.readSysFileStatusInt(RING_KEY_LED_STATE);
            variable = r > 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return variable;
    }


    /**
     * 取得光敏电阻的值
     */
    @Override
    public int getLightSensorValue() {
        int r = 0;
        try {
            r = ics.readSysFileStatusInt(LIGHT_SENSOR_VALUE);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * 配置某些中断是否产生时,是否点亮屏幕
     */
    @Override
    public boolean setIntTurnOnScreen(BCManager.INT_TYPE type, boolean trunOnScreen) {
        return Settings.System.putInt(Util.getApp().getContentResolver(),
                PREFIX + type.toString(), trunOnScreen ? 1 : 0);
    }

    @Override
    public boolean isIntType(BCManager.INT_TYPE type) {
        return Settings.System.getInt(Util.getApp().getContentResolver(),
                PREFIX + type.toString(), 0) == 1;
    }

    /**
     * 打开关闭锁
     */
    @Override
    public void setLock(boolean on) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ics.writeSysFileStatusInt(LOCK_STATE, 7);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ics.writeSysFileStatusInt(LOCK_STATE, 4);
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ics.writeSysFileStatusInt(LOCK_STATE, 7);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public boolean getLockStatus() {
        boolean variable = false;
        try {
            int r = ics.readSysFileStatusInt(LOCK_STATE);
            variable = r > 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return variable;
    }

    @Override
    public void setMainSpeakerOn(boolean mainSpeakerOn) {
        ControlCenter.getBCManager().setSpeakerPowerOn(SPEAKER_MAIN, mainSpeakerOn);
        ControlCenter.getBCManager().setSpeakerPowerOn(SPEAKER_EXTEND, !mainSpeakerOn);
    }
}
