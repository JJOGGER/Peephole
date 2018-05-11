package cn.jcyh.peephole.control;

import android.content.Context;
import android.os.IBinder;
import android.os.IHardwareService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;

import com.cust.service.ICustService;

import java.lang.reflect.Method;

import cn.jcyh.peephole.utils.Utils;


public class BcManager {

    private static final String PREFIX = "eogleking_";

    private static final int LIGHT_ID_BUTTONS = 2;
    private static BcManager sManager;
    private static Context sContext = null;

    private IHardwareService localhardwareservice;
    private ICustService ics;
//	private Utils mutils;
    //private PowerManager mPowerManager = null;

    //private BroadcastReceiver mBcReceiver=new BcRecever();

    private BcManager() {
        try {
            Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"hardware"});
            localhardwareservice = IHardwareService.Stub.asInterface(binder);
            IBinder binder2 =  ServiceManager.getService("CustService"); //
            ics = ICustService.Stub.asInterface(binder2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BcManager getManager(Context context) {
        sContext = context.getApplicationContext();
        if (sManager == null) {
            synchronized (BcManager.class) {
                if (sManager == null) {
                    sManager = new BcManager();
                }
            }
        }
        return sManager;
    }

    /**
     * 释放资源
     */
    public void release() {
        //unRegisterBcRecInt();
    }

    /**
     * PIR传感器 状态查询
     *
     * @return true 表示有人  false  表示没有人
     */
    public boolean getPIRStatus() {
        boolean variable = false;
        try {
            int r = ics.readSysFileStatusInt(Utils.PIR_STATE);
            if (r == 0) {
                variable = true;
            } else {
                variable = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return variable;
    }

    /**
     * 查询pir传感器的状态
     *
     * @return
     */
    public boolean getPIRSensorOn() {
        boolean variable = false;
        try {
            int r = ics.readSysFileStatusInt(Utils.PIR_EN_STATE);
            if (r == 1) {
                variable = true;
            } else {
                variable = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return variable;
    }

    /**
     * 控制pir传感器打开或关闭
     *
     * @param on
     */
    public void setPIRSensorOn(boolean on) {
        if (ics==null) return;
        if (on) {
            try {
                ics.writeSysFileStatusInt(Utils.PIR_EN_STATE, 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ics.writeSysFileStatusInt(Utils.PIR_EN_STATE, 0);
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
    public boolean getTamperSensorStatus() {
        boolean variable = false;
        try {
            int r = ics.readSysFileStatusInt(Utils.LOCK_DETECT_STATE);
            if (r == 1) {
                variable = true;
            } else {
                variable = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return variable;
    }

    /**
     * 控制红外灯打开和关闭 true - 打开, false - 关闭
     *
     * @param isPowerOn
     */
    public void setInfraredLightPowerOn(boolean isPowerOn) {
        if (isPowerOn) {
            try {
                ics.writeSysFileStatusInt(Utils.IR_LED_EN_STATE, 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ics.writeSysFileStatusInt(Utils.IR_LED_EN_STATE, 0);
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
    public boolean getInfraredLightStatus() {
        boolean variable = false;
        int r;
        try {
            r = ics.readSysFileStatusInt(Utils.IR_LED_EN_STATE);
            if (r == 1) {
                variable = true;
            } else {
                variable = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return variable;
    }

    /**
     * 控制喇叭的打开和关闭
     *
     * @param speakerId 音频输出设备(speaker0/speaker1) - (0/1)
     * @param isPowerOn true - 打开, false - 关闭
     */
    public void setSpeakerPowerOn(int speakerId, boolean isPowerOn) {
        if (speakerId == 0) {
            try {
                ics.writeSysFileStatusInt(Utils.SPK1_STATE, isPowerOn ? 1 : 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (speakerId == 1) {
            try {
                ics.writeSysFileStatusInt(Utils.SPK2_STATE, isPowerOn ? 1 : 0);
            } catch (RemoteException e) {
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
    public boolean getSpeakerStatus(int speakerId) {
        boolean variable = false;
        int r = 0;
        if (speakerId == 0) {
            try {
                r = ics.readSysFileStatusInt(Utils.SPK1_STATE);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (speakerId == 1) {
            try {
                r = ics.readSysFileStatusInt(Utils.SPK2_STATE);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if (r == 1) {
            variable = true;
        } else {
            variable = false;
        }
        return variable;
    }

    /**
     * 打开关闭led
     */
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
    public boolean getRingKeyLedStatus() {
        boolean variable = false;
        try {
            int r = ics.readSysFileStatusInt(Utils.RING_KEY_LED_STATE);
            if (r > 0) {
                variable = true;
            } else {
                variable = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return variable;
    }


    /**
     * 取得光敏电阻的值
     *
     * @return
     */
    public int getLightSensorValue() {
        int r = 0;
        try {
            r = ics.readSysFileStatusInt(Utils.LIGHT_SENSOR_VALUE);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return r;
    }

    public static enum INT_TYPE {
        OURDOOR_PRESS, INDOOR_PRESS, TAMPER, PIR//, WIFI
    }

    /**
     * 配置某些中断是否产生时,是否点亮屏幕
     *
     * @param type
     * @param trunOnScreen
     * @return
     */
    public boolean setIntTurnOnScreen(INT_TYPE type, boolean trunOnScreen) {
        return Settings.System.putInt(sContext.getContentResolver(),
                PREFIX + type.toString(), trunOnScreen ? 1 : 0);
    }

    public boolean isIntType(INT_TYPE type) {
        return Settings.System.getInt(sContext.getContentResolver(),
                PREFIX + type.toString(), 0) == 1;
    }

    /**
     * 打开关闭锁
     */
    public void setLock(boolean on) {
        if (on){
            try {
                ics.writeSysFileStatusInt("/sys/devices/platform/CUSTDriver/driver/LockEnState", 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            try {
                ics.writeSysFileStatusInt("/sys/devices/platform/CUSTDriver/driver/LockEnState", 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
