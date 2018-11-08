package cn.jcyh.eaglelock.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.TimeZone;

import cn.jcyh.eaglelock.constant.Constant;
import cn.jcyh.eaglelock.constant.Operation;
import cn.jcyh.eaglelock.entity.LockKey;
import cn.jcyh.eaglelock.entity.LockPwdRecord;
import cn.jcyh.locklib.callback.LockCallback;
import cn.jcyh.locklib.entity.Error;
import cn.jcyh.locklib.scanner.ExtendedBluetoothDevice;


/**
 * Created by jogger on 2018/4/27.
 * 锁回调
 */

public class MyLockCallback implements LockCallback {
    private static final String TAG = MyLockCallback.class.getSimpleName();
    private LocalBroadcastManager mLocalBroadcastManager;

    public MyLockCallback(Context context) {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
    }

    @Override
    public void onFoundDevice(ExtendedBluetoothDevice extendedBluetoothDevice) {
        Log.i(TAG, "------------onFoundDevice");
        //发现设备并广播
        Intent intent = new Intent(Constant.ACTION_BLE_DEVICE);
        intent.putExtra(Constant.DEVICE, extendedBluetoothDevice);
        mLocalBroadcastManager.sendBroadcast(intent);
        //根据accessToken和lockmac获取钥匙
//        if (ControlCenter.sCurrentKey == null) return;
        switch (MyLockAPI.sBleSession.getOperation()) {
            case Operation.LOCKCAR_DOWN:
                if (extendedBluetoothDevice.isTouch())
                    MyLockAPI.getLockAPI().connect(extendedBluetoothDevice);
                break;
            case Operation.CUSTOM_PWD:
            case Operation.SET_ADMIN_KEYBOARD_PASSWORD:
            case Operation.SET_DELETE_PASSWORD:
            case Operation.SET_LOCK_TIME:
            case Operation.RESET_KEYBOARD_PASSWORD:
            case Operation.RESET_EKEY:
            case Operation.RESET_LOCK:
            case Operation.GET_LOCK_TIME:
            case Operation.GET_OPERATE_LOG:
                if (extendedBluetoothDevice.getAddress().equals(MyLockAPI.sBleSession.getLockmac()))
                    MyLockAPI.getLockAPI().connect(extendedBluetoothDevice);
                break;
            default:
                if (extendedBluetoothDevice.getAddress().equals(MyLockAPI.sBleSession.getLockmac()))
                    MyLockAPI.getLockAPI().connect(extendedBluetoothDevice);
                break;
        }
    }

    @Override
    public void onDeviceConnected(ExtendedBluetoothDevice extendedBluetoothDevice) {
        Log.e(TAG, "--------onDeviceConnected:" + MyLockAPI.sBleSession.getOperation());
        MyLockAPI lockAPI = MyLockAPI.getLockAPI();
        LockKey localKey = null;
        String operation = MyLockAPI.sBleSession.getOperation();
        if (Operation.ADD_ADMIN.equals(operation)) {
            lockAPI.addAdministrator(extendedBluetoothDevice);
        }

        if (localKey == null) return;
        switch (operation) {
            case Operation.LOCKCAR_DOWN:
                Log.e(TAG, "--------------UNLOCK" + localKey);
                //本地存在锁
                if (localKey.isAdmin()) {
                    lockAPI.unlockByAdministrator(extendedBluetoothDevice, localKey);
                } else {
                    lockAPI.unlockByUser(extendedBluetoothDevice, localKey);
                }
                break;
            case Operation.SET_ADMIN_KEYBOARD_PASSWORD://管理码
                lockAPI.setAdminKeyboardPassword(extendedBluetoothDevice, localKey, MyLockAPI.sBleSession.getArgments().getString("password"));
                break;
            case Operation.CUSTOM_PWD:
                Bundle argments = MyLockAPI.sBleSession.getArgments();
                localKey.setStartDate(argments.getLong(Constant.START_DATE));
                localKey.setEndDate(argments.getLong(Constant.END_DATE));
                Log.e(TAG, "------CUSTOM_PWD");
                lockAPI.addPeriodKeyboardPassword(extendedBluetoothDevice, argments.getString(Constant.PWD), localKey);
                break;
            case Operation.DELETE_ONE_KEYBOARDPASSWORD://刪除密碼
                Log.e(TAG, "---------DELETE_ONE_KEYBOARDPASSWORD");
                argments = MyLockAPI.sBleSession.getArgments();
                LockPwdRecord lockPwdRecord = argments.getParcelable(Constant.PWD_INFO);
                if (lockPwdRecord == null) return;
                lockAPI.deleteOneKeyboardPassword(extendedBluetoothDevice, lockPwdRecord.getKeyboardPwdType(), lockPwdRecord.getKeyboardPwd(), localKey);
                break;
//                case SET_DELETE_PASSWORD://删除码
//                    mTTTTLockAPI.setDeletePassword(extendedBluetoothDevice, uid, curKey.getLockVersion(), curKey.getAdminPs(), curKey.getUnlockKey(), curKey.getLockFlagPos(), curKey.getAesKeystr(), bleSession.getPassword());
//                    break;
            case Operation.SET_LOCK_TIME://设置锁时间
                lockAPI.setLockTime(extendedBluetoothDevice, localKey);
                break;
            case Operation.RESET_KEYBOARD_PASSWORD://重置键盘密码
                lockAPI.resetKeyboardPassword(extendedBluetoothDevice, localKey);
                break;
            case Operation.RESET_EKEY://重置电子钥匙 锁标志位+1
                lockAPI.resetEKey(extendedBluetoothDevice, localKey);
                break;
            case Operation.RESET_LOCK://重置锁
                lockAPI.resetLock(extendedBluetoothDevice, localKey.getOpenid(), localKey.getLockVersion(), localKey.getAdminPwd(), localKey.getLockKey(), localKey.getLockFlagPos(), localKey.getAesKeystr());
                break;
            case Operation.ADD_IC_CARD:
                lockAPI.addICCard(extendedBluetoothDevice, localKey);
                break;
            case Operation.DELETE_IC_CARD:
                lockAPI.deleteICCard(extendedBluetoothDevice, MyLockAPI.sBleSession.getArgments().getLong(Constant.IC_CARD_NUMBER), localKey);
                break;
            case Operation.MODIFY_IC_PERIOD:
                lockAPI.modifyICPeriod(extendedBluetoothDevice, MyLockAPI.sBleSession.getArgments().getLong(Constant.IC_CARD_NUMBER), localKey);
                break;
            case Operation.SEARCH_IC_NUMBER:
                lockAPI.searchICCard(extendedBluetoothDevice, localKey);
                break;
            case Operation.CLEAR_IC_CARD:
                lockAPI.clearICCard(extendedBluetoothDevice, localKey);
                break;
            case Operation.ADD_FINGERPRINT:
                lockAPI.addFingerPrint(extendedBluetoothDevice, localKey);
                break;
            case Operation.MODIFY_FINGERPRINT_PERIOD:
                lockAPI.modifyFingerPrintPeriod(extendedBluetoothDevice, MyLockAPI.sBleSession.getArgments().getLong(Constant.FRNO), localKey);
                break;
            case Operation.CLEAR_FINGERPRINTS:
                lockAPI.clearFingerPrint(extendedBluetoothDevice, localKey);
                break;
            case Operation.DELETE_FINGERPRINT:
                lockAPI.deleteFingerPrint(extendedBluetoothDevice, MyLockAPI.sBleSession.getArgments().getLong(Constant.FRNO), localKey);
                break;
            case Operation.GET_OPERATE_LOG://获取操作日志
                lockAPI.getOperateLog(extendedBluetoothDevice, localKey);
                break;
            case Operation.GET_LOCK_TIME://获取锁时间
                lockAPI.getLockTime(extendedBluetoothDevice, localKey);
                break;
//                case DELETE_ONE_KEYBOARDPASSWORD://这里的密码类型传0
//                    mTTTTLockAPI.deleteOneKeyboardPassword(extendedBluetoothDevice, uid, localKey.getLockVersion(), localKey.getAdminPs(), localKey.getUnlockKey(), localKey.getLockFlagPos(), 0, bleSession.getPassword(), localKey.getAesKeystr());
//                    break;
//                case GET_LOCK_VERSION_INFO:
//                    mTTTTLockAPI.readDeviceInfo(extendedBluetoothDevice, localKey.getLockVersion(), localKey.getAesKeystr());
//                    break;
            default:
                break;
        }

    }

    @Override
    public void onDeviceDisconnected(ExtendedBluetoothDevice extendedBluetoothDevice) {
        Log.e(TAG, "-----------onDeviceDisconnected");
        //断开连接
        MyLockAPI.sBleSession.setOperation(Operation.LOCKCAR_DOWN);
        Intent intent = new Intent(Constant.ACTION_BLE_DISCONNECTED);
        intent.putExtra(Constant.DEVICE, extendedBluetoothDevice);
        mLocalBroadcastManager.sendBroadcast(intent);
//            if(!operateSuccess) {
//                toast("蓝牙已断开");
//            }
//            LogUtil.d("蓝牙断开", DBG);
    }

    @Override
    public void onGetLockVersion(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, int var3, int var4,
                                 int var5, int var6, Error var7) {
        Log.e(TAG, "-----------onGetLockVersion");
    }

    @Override
    public void onAddAdministrator(ExtendedBluetoothDevice extendedBluetoothDevice, String lockVersionString, String adminPs, String unlockKey, String adminKeyboardPwd, String deletePwd, String pwdInfo, long timestamp, String aesKeystr, int feature, String modelNumber, String hardwareRevision, String firmwareRevision, Error error) {
        Log.e(TAG, "-----------onAddAdministrator");
        addAdmin(extendedBluetoothDevice, lockVersionString, adminPs, unlockKey, adminKeyboardPwd, deletePwd, pwdInfo, timestamp, aesKeystr, feature, modelNumber, hardwareRevision, firmwareRevision, error);
    }

    @Override
    public void onResetEKey(ExtendedBluetoothDevice extendedBluetoothDevice, int lockFlagPos, Error error) {
        Log.e(TAG, "-----------onResetEKey");
        Intent intent = new Intent(Constant.ACTION_RESET_KEY);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onSetLockName(ExtendedBluetoothDevice extendedBluetoothDevice, String var2, Error var3) {
        Log.e(TAG, "-----------onSetLockName");
    }

    @Override
    public void onSetAdminKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, String adminCode, Error
            error) {
        Log.e(TAG, "-----------onSetAdminKeyboardPassword" + adminCode);
        Intent intent = new Intent(Constant.ACTION_SET_ADMIN_PWD);
        intent.putExtra(Constant.ERROR_MSG, error);
        intent.putExtra(Constant.PWD, adminCode);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onSetDeletePassword(ExtendedBluetoothDevice extendedBluetoothDevice, String var2, Error var3) {
        Log.e(TAG, "-----------onSetDeletePassword");
    }

    /**
     * @param uniqueid 开锁的唯一标识id 只有三代锁有用 其它默认(取系统时间)
     * @param lockTime 锁时间 只有三代锁有用 其它默认(取系统时间)
     */
    @Override
    public void onUnlock(ExtendedBluetoothDevice extendedBluetoothDevice, int uid, int uniqueid, long lockTime, Error error) {
        Log.e(TAG, "---------onUnlock:" + error);
        Intent intent = new Intent(Constant.ACTION_UNLOCK);
        intent.putExtra(Constant.ERROR_MSG, error);
        intent.putExtra(Constant.LOCK_ADDRESS, extendedBluetoothDevice.getAddress());
        mLocalBroadcastManager.sendBroadcast(intent);
        MyLockAPI.getLockAPI().connect(extendedBluetoothDevice, Operation.GET_OPERATE_LOG);
//        if (ControlCenter.sCurrentKey != null) {
//            LockHttpAction.getHttpAction().uploadElectricQuantity(ControlCenter.sCurrentKey.getLockId(), extendedBluetoothDevice.getBatteryCapacity(), null);
//        }
    }

    @Override
    public void onSetLockTime(ExtendedBluetoothDevice extendedBluetoothDevice, Error error) {
        Log.e(TAG, "-----------onSetLockTime");
        Intent intent = new Intent(Constant.ACTION_LOCK_SYNC_TIME);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onGetLockTime(ExtendedBluetoothDevice extendedBluetoothDevice, long date, Error error) {
        Log.e(TAG, "-----------onGetLockTime");
        Intent intent = new Intent(Constant.ACTION_LOCK_GET_TIME);
        intent.putExtra(Constant.ERROR_MSG, error);
        intent.putExtra(Constant.DATE, date);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onResetKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, String pwdInfo, long timestamp, Error error) {
        Log.e(TAG, "-----------onResetKeyboardPassword" + pwdInfo + "--?" + timestamp);
        Intent intent = new Intent(Constant.ACTION_RESET_PWD);
        intent.putExtra(Constant.ERROR_MSG, error);
        intent.putExtra(Constant.PWD_RESET_DATA, pwdInfo);
        intent.putExtra(Constant.PWD_RESET_TIMESTAMP, timestamp);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onSetMaxNumberOfKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, Error
            var3) {
        Log.e(TAG, "-----------onSetMaxNumberOfKeyboardPassword");
    }

    @Override
    public void onResetKeyboardPasswordProgress(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, Error
            var3) {
        Log.e(TAG, "-----------onResetKeyboardPasswordProgress");
    }

    @Override
    public void onResetLock(ExtendedBluetoothDevice extendedBluetoothDevice, Error error) {
        Log.e(TAG, "-----------onResetLock");
        Intent intent = new Intent(Constant.ACTION_RESET_LOCK);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);

    }

    @Override
    public void onAddKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, String customPwd,
                                      long startTime, long endTime, Error error) {
        Log.e(TAG, "-----------onAddKeyboardPassword" + customPwd);
        Intent intent = new Intent(Constant.ACTION_CUSTOM_PWD);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onModifyKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, String
            var3, String var4, Error var5) {
        Log.e(TAG, "-----------onModifyKeyboardPassword");
    }

    @Override
    public void onDeleteOneKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, int keyboardPwdType, String deletedPwd, Error error) {
        Log.e(TAG, "-----------onDeleteOneKeyboardPassword");
        Intent intent = new Intent(Constant.ACTION_DELETE_PWD);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onDeleteAllKeyboardPassword(ExtendedBluetoothDevice extendedBluetoothDevice, Error var2) {
        Log.e(TAG, "-----------onDeleteAllKeyboardPassword");
    }

    @Override
    public void onGetOperateLog(ExtendedBluetoothDevice extendedBluetoothDevice, String records, Error error) {
        Log.e(TAG, "-----------onGetOperateLog");
        if (Error.SUCCESS != error) return;
        //根据accessToken和lockmac获取钥匙
//        if (ControlCenter.sCurrentKey != null && ControlCenter.sCurrentKey.getLockMac().equals(extendedBluetoothDevice.getAddress())) {
//            LockHttpAction.getHttpAction().uploadLockRecords(ControlCenter.sCurrentKey.getLockId(), records, null);
//        }
        Log.e(TAG, "----------records:" + records + "-->" + error);
    }

    @Override
    public void onSearchDeviceFeature(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, int var3, Error
            var4) {
        Log.e(TAG, "-----------onSearchDeviceFeature");
    }

    @Override
    public void onAddICCard(ExtendedBluetoothDevice extendedBluetoothDevice, int status, int battery, long cardNo, Error error) {
        //onAddICCard2--9--1036620029
        Log.e(TAG, "-----------onAddICCard" + status + "--" + battery + "--" + cardNo);
        Intent intent = new Intent(Constant.ACTION_LOCK_IC_CARD);
        intent.putExtra(Constant.TYPE, Constant.TYPE_ADD_IC_CARD);
        intent.putExtra(Constant.ERROR_MSG, error);
        intent.putExtra(Constant.STATUS, status);
        intent.putExtra(Constant.IC_CARD_NUMBER, cardNo);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onModifyICCardPeriod(ExtendedBluetoothDevice extendedBluetoothDevice, int battery, long cardNo, long startDate, long endDate, Error error) {
        Log.e(TAG, "-----------onModifyICCardPeriod" + startDate + "-->" + endDate);
        Intent intent = new Intent(Constant.ACTION_LOCK_IC_CARD);
        intent.putExtra(Constant.TYPE, Constant.TYPE_MODIFY_IC_CARD);
        intent.putExtra(Constant.IC_CARD_NUMBER, cardNo);
        intent.putExtra(Constant.START_DATE, startDate);
        intent.putExtra(Constant.END_DATE, endDate);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onDeleteICCard(ExtendedBluetoothDevice extendedBluetoothDevice, int battery, long cardNo, Error error) {
        Log.e(TAG, "-----------onDeleteICCard");
        Intent intent = new Intent(Constant.ACTION_LOCK_IC_CARD);
        intent.putExtra(Constant.TYPE, Constant.TYPE_DELETE_IC_CARD);
        intent.putExtra(Constant.ERROR_MSG, error);
        intent.putExtra(Constant.IC_CARD_NUMBER, cardNo);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onClearICCard(ExtendedBluetoothDevice extendedBluetoothDevice, int battery, Error error) {
        Log.e(TAG, "-----------onClearICCard");
        Intent intent = new Intent(Constant.ACTION_LOCK_IC_CARD);
        intent.putExtra(Constant.TYPE, Constant.TYPE_CLEAR_IC_CARD);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onSetWristbandKeyToLock(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, Error var3) {
        Log.e(TAG, "-----------onSetWristbandKeyToLock");
    }

    @Override
    public void onSetWristbandKeyToDev(Error error) {
        Log.e(TAG, "-----------onSetWristbandKeyToDev");
    }

    @Override
    public void onSetWristbandKeyRssi(Error error) {
        Log.e(TAG, "-----------onSetWristbandKeyRssi");
    }

    @Override
    public void onAddFingerPrint(ExtendedBluetoothDevice extendedBluetoothDevice, int status, int battery, long fingerPrintNo, Error error) {
//         Log.e(TAG,"-----------onAddFingerPrint:" + "status:" + status + "-->fingerPrintNo:" + fingerPrintNo + "error:" + error);
    }

    @Override
    public void onAddFingerPrint(ExtendedBluetoothDevice extendedBluetoothDevice, int status, int bery, long fingerPrintNo, int maxVail, Error error) {
        Log.e(TAG, "-----------onAddFingerPrint:" + "status:" + status + "-->fingerPrintNo:" + fingerPrintNo + "-->maxVail:" + maxVail + "error:" + error);
        Intent intent = new Intent(Constant.ACTION_LOCK_FINGERPRINT);
        intent.putExtra(Constant.TYPE, Constant.TYPE_ADD_FINGERPRINT);
        intent.putExtra(Constant.FRNO, fingerPrintNo);
        intent.putExtra(Constant.STATUS, status);
        intent.putExtra(Constant.ERROR_MSG, error);
        intent.putExtra(Constant.MAX_VALIDATE, maxVail);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onFingerPrintCollection(ExtendedBluetoothDevice extendedBluetoothDevice, int battery, Error error) {
    }

    @Override
    public void onFingerPrintCollection(ExtendedBluetoothDevice extendedBluetoothDevice, int battery, int vail,
                                        int maxVail, Error error) {
        Log.e(TAG, "-----------onFingerPrintCollection" + vail + "--:" + maxVail);//1--:4
        Intent intent = new Intent(Constant.ACTION_LOCK_FINGERPRINT);
        intent.putExtra(Constant.TYPE, Constant.TYPE_COLLECTION_FINGERPRINT);
        intent.putExtra(Constant.ERROR_MSG, error);
        intent.putExtra(Constant.VALIDATE, vail);
        intent.putExtra(Constant.MAX_VALIDATE, maxVail);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onModifyFingerPrintPeriod(ExtendedBluetoothDevice extendedBluetoothDevice, int battery, long FRNo, long startDate, long endDate, Error error) {
        Log.e(TAG, "-----------onModifyFingerPrintPeriod");
        Intent intent = new Intent(Constant.ACTION_LOCK_FINGERPRINT);
        intent.putExtra(Constant.TYPE, Constant.TYPE_MODIFY_FINGERPRINT);
        intent.putExtra(Constant.FRNO, FRNo);
        intent.putExtra(Constant.START_DATE, startDate);
        intent.putExtra(Constant.END_DATE, endDate);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onDeleteFingerPrint(ExtendedBluetoothDevice extendedBluetoothDevice, int battery, long FRNo, Error error) {
        Log.e(TAG, "-----------onDeleteFingerPrint");
        Intent intent = new Intent(Constant.ACTION_LOCK_FINGERPRINT);
        intent.putExtra(Constant.TYPE, Constant.TYPE_DELETE_FINGERPRINT);
        intent.putExtra(Constant.FRNO, FRNo);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onClearFingerPrint(ExtendedBluetoothDevice extendedBluetoothDevice, int battery, Error error) {
        Log.e(TAG, "-----------onClearFingerPrint");
        Intent intent = new Intent(Constant.ACTION_LOCK_FINGERPRINT);
        intent.putExtra(Constant.TYPE, Constant.TYPE_CLEAR_FINGERPRINT);
        intent.putExtra(Constant.ERROR_MSG, error);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onSearchAutoLockTime(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, int var3, int var4,
                                     int var5, Error var6) {
        Log.e(TAG, "-----------onSearchAutoLockTime");
    }

    @Override
    public void onModifyAutoLockTime(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, int var3, Error
            var4) {
        Log.e(TAG, "-----------onModifyAutoLockTime");
    }

    @Override
    public void onReadDeviceInfo(ExtendedBluetoothDevice extendedBluetoothDevice, String var2, String var3, String
            var4, String var5, String var6) {
        Log.e(TAG, "-----------onReadDeviceInfo");
    }

    @Override
    public void onEnterDFUMode(ExtendedBluetoothDevice extendedBluetoothDevice, Error var2) {
        Log.e(TAG, "-----------onEnterDFUMode");
    }

    @Override
    public void onGetLockSwitchState(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, int var3, Error
            var4) {
        Log.e(TAG, "-----------onGetLockSwitchState");
    }

    @Override
    public void onLock(ExtendedBluetoothDevice extendedBluetoothDevice, int battery, int uid, int uniqueid, long lockTime, Error error) {
        Log.e(TAG, "-----------onLock");
    }

    @Override
    public void onScreenPasscodeOperate(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, int var3, Error
            var4) {
        Log.e(TAG, "-----------onScreenPasscodeOperate");
    }

    @Override
    public void onRecoveryData(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, Error var3) {
        Log.e(TAG, "-----------onRecoveryData");
    }

    @Override
    public void onSearchICCard(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, String var3, Error var4) {
        Log.e(TAG, "-----------onSearchICCard");
    }

    @Override
    public void onSearchFingerPrint(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, String var3, Error
            var4) {
        Log.e(TAG, "-----------onSearchFingerPrint");
    }

    @Override
    public void onSearchPasscode(ExtendedBluetoothDevice extendedBluetoothDevice, String var2, Error var3) {
        Log.e(TAG, "-----------onSearchPasscode");
    }

    @Override
    public void onSearchPasscodeParam(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, String var3,
                                      long var4, Error var6) {
        Log.e(TAG, "-----------onSearchPasscodeParam");
    }

    @Override
    public void onOperateRemoteUnlockSwitch(ExtendedBluetoothDevice extendedBluetoothDevice, int var2, int var3,
                                            int var4, int var5, Error var6) {
        Log.e(TAG, "-----------onOperateRemoteUnlockSwitch");
    }


    /**
     * 添加管理员
     */
    private void addAdmin(ExtendedBluetoothDevice extendedBluetoothDevice, String lockVersionString, String adminPs, String unlockKey, String adminKeyboardPwd, String deletePwd, String pwdInfo, long timestamp, String aesKeystr, int feature, String modelNumber, String hardwareRevision, String firmwareRevision, final Error error) {
        Log.e(TAG, "--------error:" + error + "--->" + (error == Error.SUCCESS));
        if (error == Error.SUCCESS) {
            LockKey key = new LockKey();
//            key.setAccessToken(ControlCenter.getControlCenter().getUserInfo().getAccess_token());
            key.setAdmin(true);
            key.setLockVersion(lockVersionString);
            key.setLockName(extendedBluetoothDevice.getName());
            key.setLockMac(extendedBluetoothDevice.getAddress());
            key.setAdminPwd(adminPs);
            key.setLockKey(unlockKey);
            key.setNoKeyPwd(adminKeyboardPwd);
            key.setDeletePwd(deletePwd);
            key.setPwdInfo(pwdInfo);
            key.setTimestamp(timestamp);
            key.setAesKeystr(aesKeystr);
            key.setSpecialValue(feature);

            //获取当前时区偏移量
            key.setTimezoneRawOffset(TimeZone.getDefault().getOffset(System.currentTimeMillis()));
            key.setModelNumber(modelNumber);
            key.setHardwareRevision(hardwareRevision);
            key.setFirmwareRevision(firmwareRevision);
//            T.show("锁添加成功，正在上传服务端进行初始化操作");
//            UserHttpAction.getHttpAction().initLock(key, new OnHttpRequestListener<Boolean>() {
//                @Override
//                public void onFailure(int errorCode) {
//
//                }
//
//                @Override
//                public void onSuccess(Boolean aBoolean) {
//                    Intent intent = new Intent(Constant.ACTION_ADD_ADMIN);
//                    intent.putExtra(Constant.ERROR_MSG, error);
//                    mLocalBroadcastManager.sendBroadcast(intent);
//                }
//            });
        } else {
            Intent intent = new Intent(Constant.ACTION_ADD_ADMIN);
            intent.putExtra(Constant.ERROR_MSG, error);
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }
}