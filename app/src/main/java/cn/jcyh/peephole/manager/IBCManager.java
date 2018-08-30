package cn.jcyh.peephole.manager;

import cn.jcyh.peephole.manager.impl.BCManager;

/**
 * Created by jogger on 2018/8/6.猫眼硬件控制
 */
public interface IBCManager {

    void release();

    boolean getPIRStatus();

    boolean getPIRSensorOn();

    void setPIRSensorOn(boolean on);

    boolean getTamperSensorStatus();

    void setInfraredLightPowerOn(boolean isPowerOn);

    boolean getInfraredLightStatus();

    void setSpeakerPowerOn(int speakerId, boolean isPowerOn);

    boolean getSpeakerStatus(int speakerId);

    void setRingKeyLedOn(boolean on);

    boolean getRingKeyLedStatus();

    int getLightSensorValue();

    boolean setIntTurnOnScreen(BCManager.INT_TYPE type, boolean trunOnScreen);

    boolean isIntType(BCManager.INT_TYPE type);

    void setLock(boolean on);

    boolean getLockStatus();

    void setMainSpeakerOn(boolean mainSpeakerOn);

}
