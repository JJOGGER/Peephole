package cn.jcyh.eaglelock.api;

import android.os.Bundle;

import java.util.List;

import cn.jcyh.eaglelock.constant.Operation;
import cn.jcyh.eaglelock.entity.BleSession;
import cn.jcyh.eaglelock.entity.LockKey;
import cn.jcyh.eaglelock.entity.UnLockData;
import cn.jcyh.locklib.api.LockAPI;
import cn.jcyh.locklib.scanner.ExtendedBluetoothDevice;

/**
 * Created by jogger on 2018/6/9.
 */
public class MyLockAPI extends LockAPI {
    private static MyLockAPI sLockAPI;
    public static BleSession sBleSession = BleSession.getInstance(Operation.LOCKCAR_DOWN, null);
    private static int sOpenID;

    private MyLockAPI() {
        super();
    }

    public static MyLockAPI getLockAPI() {
        if (sLockAPI == null)
            synchronized (LockAPI.class) {
                if (sLockAPI == null) {
                    sLockAPI = new MyLockAPI();
                }
            }
//        sOpenID = ControlCenter.getControlCenter().getOpenID();
        return sLockAPI;
    }

    public synchronized void connect(String address, String operation) {
        sBleSession.setOperation(operation);
        sBleSession.setLockmac(address);
        connect(address);
    }

    public synchronized void connect(String address, Bundle bundle, String operation) {
        sBleSession.setArgments(bundle);
        sBleSession.setOperation(operation);
        sBleSession.setLockmac(address);
        connect(address);
    }

    public synchronized void connect(ExtendedBluetoothDevice device, String operation) {
        sBleSession.setOperation(operation);
        sBleSession.setLockmac(device.getAddress());
        connect(device);
    }

    public void setAdminKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey, String password) {
        setAdminKeyboardPassword(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), password);
    }

    public void setDeletePassword(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey, String password) {
        setAdminKeyboardPassword(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), password);
    }

    public void unlockByAdministrator(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        unlockByAdministrator(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), System.currentTimeMillis(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }
    public void unlockByAdministrator(ExtendedBluetoothDevice extendedBluetoothDevice, UnLockData lockKey) {
        unlockByAdministrator(extendedBluetoothDevice, lockKey.getSmartLockUserId(), lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), System.currentTimeMillis(), lockKey.getAesKeyStr(), lockKey.getTimezoneRawOffset());
    }
    public void lockByAdministrator(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        lockByAdministrator(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void unlockByUser(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        unlockByUser(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getStartDate(), lockKey.getEndDate(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void lockByUser(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        lockByUser(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getStartDate(), lockKey.getEndDate(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }


    public void setLockTime(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        setLockTime(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getLockKey(), System.currentTimeMillis(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void resetKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        resetKeyboardPassword(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void resetEKey(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        resetEKey(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockFlagPos() + 1, lockKey.getAesKeystr());
    }


    public void setLockName(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        setLockName(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), lockKey.getLockName());
    }

    public void getLockTime(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        getLockTime(extendedBluetoothDevice, lockKey.getLockVersion(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void resetLock(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        resetLock(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void getOperateLog(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        getOperateLog(extendedBluetoothDevice, lockKey.getLockVersion(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void getAllOperateLog(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        getAllOperateLog(extendedBluetoothDevice, lockKey.getLockVersion(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void getValidKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        getValidKeyboardPassword(extendedBluetoothDevice, lockKey.getLockVersion(), lockKey.getAesKeystr());
    }


    public void addPeriodKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, String password, LockKey lockKey) {
        addPeriodKeyboardPassword(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), password, lockKey.getStartDate(), lockKey.getEndDate(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void deleteOneKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, int keyboardPwdType, String password, LockKey lockKey) {
        deleteOneKeyboardPassword(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), keyboardPwdType, password, lockKey.getAesKeystr());
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void deleteKeyboardPasswords(ExtendedBluetoothDevice extendedBluetoothDevice, List<String> passwords, LockKey lockKey) {
        deleteKeyboardPasswords(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), passwords, lockKey.getAesKeystr());
    }

    public void modifyKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, int keyboardPwdType, String originalPwd, String newPwd, LockKey lockKey) {
        modifyKeyboardPassword(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), keyboardPwdType, originalPwd, newPwd, lockKey.getStartDate(), lockKey.getEndDate(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void deleteAllKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        deleteAllKeyboardPassword(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void searchDeviceFeature(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        searchDeviceFeature(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void searchICCard(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        searchICCard(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void addICCard(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        addICCard(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void modifyICPeriod(ExtendedBluetoothDevice extendedBluetoothDevice, long cardNo, LockKey lockKey) {
        modifyICPeriod(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), cardNo, lockKey.getStartDate(), lockKey.getEndDate(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void deleteICCard(ExtendedBluetoothDevice extendedBluetoothDevice, long cardNo, LockKey lockKey) {
        deleteICCard(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), cardNo, lockKey.getAesKeystr());
    }

    public void clearICCard(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        clearICCard(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void setWristbandKeyToLock(ExtendedBluetoothDevice extendedBluetoothDevice, String wristbandKey, LockKey lockKey) {
        setWristbandKeyToLock(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), wristbandKey, lockKey.getAesKeystr());
    }

    public void searchFingerPrint(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        searchFingerPrint(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void addFingerPrint(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        addFingerPrint(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void modifyFingerPrintPeriod(ExtendedBluetoothDevice extendedBluetoothDevice, long FRNo, LockKey lockKey) {
        modifyFingerPrintPeriod(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), FRNo, lockKey.getStartDate(), lockKey.getEndDate(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void deleteFingerPrint(ExtendedBluetoothDevice extendedBluetoothDevice, long FRNo, LockKey lockKey) {
        deleteFingerPrint(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), FRNo, lockKey.getAesKeystr());
    }

    public void clearFingerPrint(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        clearFingerPrint(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void searchAutoLockTime(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        searchAutoLockTime(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void modifyAutoLockTime(ExtendedBluetoothDevice extendedBluetoothDevice, int time, LockKey lockKey) {
        modifyAutoLockTime(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), time, lockKey.getAesKeystr());
    }

    public void readDeviceInfo(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        readDeviceInfo(extendedBluetoothDevice, lockKey.getLockVersion(), lockKey.getAesKeystr());
    }

    public void enterDFUMode(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        enterDFUMode(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }

    public void getLockSwitchState(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        getLockSwitchState(extendedBluetoothDevice, lockKey.getLockVersion(), lockKey.getAesKeystr());
    }

    public void lock(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        lock(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getStartDate(), lockKey.getEndDate(), lockKey.getLockKey(), lockKey.getLockFlagPos(), System.currentTimeMillis(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void showPasswordOnScreen(ExtendedBluetoothDevice extendedBluetoothDevice, int opType, LockKey lockKey) {
        showPasswordOnScreen(extendedBluetoothDevice, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), opType, lockKey.getAesKeystr());
    }

    public void recoveryData(ExtendedBluetoothDevice extendedBluetoothDevice, int opType, String json, LockKey lockKey) {
        recoveryData(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), opType, json, lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void searchPasscodeParam(ExtendedBluetoothDevice extendedBluetoothDevice, LockKey lockKey) {
        searchPasscodeParam(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void searchPasscode(ExtendedBluetoothDevice extendedBluetoothDevice, int uid, LockKey lockKey) {
        searchPasscode(extendedBluetoothDevice, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr(), lockKey.getTimezoneRawOffset());
    }

    public void operateRemoteUnlockSwitch(ExtendedBluetoothDevice extendedBluetoothDevice, int operateType, int state, LockKey lockKey) {
        operateRemoteUnlockSwitch(extendedBluetoothDevice, operateType, state, sOpenID, lockKey.getLockVersion(), lockKey.getAdminPwd(), lockKey.getLockKey(), lockKey.getLockFlagPos(), lockKey.getAesKeystr());
    }
}
