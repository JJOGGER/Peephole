package cn.jcyh.peephole.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

import cn.jcyh.eaglelock.entity.UnLockData;
import cn.jcyh.peephole.entity.AdvertData;
import cn.jcyh.peephole.entity.ConfigData;
import cn.jcyh.peephole.entity.Doorbell;
import cn.jcyh.peephole.entity.LogRecord;
import cn.jcyh.peephole.entity.RequestUploadElectricQuantity;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.http.download.ProgressHttpListener;
import cn.jcyh.peephole.utils.GsonUtil;
import cn.jcyh.peephole.utils.SystemUtil;

/**
 * Created by jogger on 2018/7/17.
 */
public class HttpRequest implements IHttpRequest {
    private final ThreadPoolManager mThreadPoolManager;

    HttpRequest() {
        mThreadPoolManager = ThreadPoolManager.getThreadPoolManager();
    }

    @Override
    public void initNIM(IDataListener listener) {
        HttpTask httpTask = new HttpTask(HttpUrlIble.INIT_NIM, null, Doorbell.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTask, listener));
    }

    @Override
    public void setDoorbellConfig(String configJson, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("config", configJson);
        HttpTaskVoid httpTask = new HttpTaskVoid(HttpUrlIble.DOORBELL_SET_CONFIG_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTask, listener));
    }

    @Override
    public void getDoorbellConfig(IDataListener listener) {
        HttpTask httpTask = new HttpTask(HttpUrlIble.DOORBELL_GET_CONFIG_URL, null, ConfigData.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTask, listener));
    }

    @Override
    public void unbindUser(String userID, String authorizationCode, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("UserId", userID);
        params.put("AuthorizationCode", authorizationCode);
        HttpTaskVoid httpTask = new HttpTaskVoid(HttpUrlIble.DOORBELL_UNBIND_USER_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTask, listener));
    }

    @Override
    public void sendDoorbellImg(String command, int type, String filePath, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("command", command);
        HttpTaskVoid httpTaskVoid = new HttpTaskVoid(HttpUrlIble.UPLOAD_DOORBELL_ALARM_URL, filePath, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTaskVoid, listener));
    }

    @Override
    public void setDoorbellName(String name, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("Name", name);
        HttpTaskVoid httpTaskVoid = new HttpTaskVoid(HttpUrlIble.DOORBELL_SET_NAME_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTaskVoid, listener));
    }

    @Override
    public void getSysVersion(int versionCode, String sysVersion, String screenResolution, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("Number", versionCode);
        HttpTask getVersion = new HttpTask(HttpUrlIble.DOORBELL_GET_VERSION_URL, params, Version.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(getVersion, listener));
    }

    @Override
    public void updateVersion(String url, String savePath, ProgressHttpListener progressHttpListener, IDataListener listener) {
        HttpTaskVoid updateVersion = new HttpTaskVoid(url, savePath, progressHttpListener, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updateVersion, listener));
    }

    @Override
    public void uploadBattery(int battery, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("ElectricQuantity", battery);
        HttpTaskVoid updateVersion = new HttpTaskVoid(HttpUrlIble.DOORBELL_UPLOAD_BATTERY_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updateVersion, listener));
    }

    @Override
    public void getBanners(int terminalSize, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("terminalSize", terminalSize);
        HttpTask banners = new HttpTask(HttpUrlIble.DOORBELL_GET_BANNERS_URL, params, AdvertData.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(banners, listener));
    }

    @Override
    public void getBindUsers(IDataListener listener) {
        HttpTaskList bindUsers = new HttpTaskList(HttpUrlIble.DOORBELL_GET_BIND_USERS_URL, null, User.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(bindUsers, listener));
    }

    @Override
    public void setDoorbellManager(String userID, String authorizationCode, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userID);
        params.put("authorizationCode", authorizationCode);
        HttpTaskVoid updateVersion = new HttpTaskVoid(HttpUrlIble.DOORBELL_SET_MANAGER_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updateVersion, listener));
    }

    @Override
    public void updatePatch(String sysVersion, String screenResolution, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("CurrentVersion", SystemUtil.getVersionCode());
        HttpTask updatePatch = new HttpTask(HttpUrlIble.DOORBELL_UPDATE_PATCH_URL, params, Version.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updatePatch, listener));
    }

    @Override
    public void updateSoft(String sysVersion, String screenResolution, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("CurrentVersion", SystemUtil.getVersionCode());
        params.put("VersionCode", sysVersion);
        params.put("ScreenResolution", screenResolution);
        HttpTask updatePatch = new HttpTask(HttpUrlIble.DOORBELL_UPDATE_SOFT_URL, params, Version.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updatePatch, listener));
    }

    @Override
    public void updateSystem(int versionCode, String sysVersion, String screenResolution, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("Number", versionCode);
        params.put("VersionCode", sysVersion);
        params.put("ScreenResolution", screenResolution);
        HttpTask getVersion = new HttpTask(HttpUrlIble.DOORBELL_UPDATE_SYSTEM_URL, params, Version.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(getVersion, listener));
    }

    @Override
    public void antiBreakAlarm(boolean isAlarm, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("isAlarm", isAlarm);
        HttpTaskVoid updateVersion = new HttpTaskVoid(HttpUrlIble.DOORBELL_ANTI_BREAK_ALARM, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updateVersion, listener));
    }

    @Override
    public void doorbellMagneticNotice(boolean isOpen, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("IsOpen", isOpen);
        HttpTaskVoid updateVersion = new HttpTaskVoid(HttpUrlIble.DOORBELL_MAGNETIC_NOTICE, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updateVersion, listener));
    }

    @Override
    public void userTaklTimeRecord(String userID, long sessionDuration, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("UserId", userID);
        params.put("TalkTime", sessionDuration);
        HttpTaskVoid userTaklTimeRecord = new HttpTaskVoid(HttpUrlIble.DOORBELL_TALK_TIME_RECORD, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(userTaklTimeRecord, listener));
    }

    @Override
    public void uploadLocation(double longitude, double latitude, String country, String province, String city, String district, String street, String addrStr, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("Longitude", longitude);
        params.put("Latitude", latitude);
        params.put("Country", country);
        params.put("Province", province);
        params.put("City", city);
        params.put("District", district);
        params.put("Street", street);
        params.put("Address", addrStr);
        HttpTaskVoid uploadLocation = new HttpTaskVoid(HttpUrlIble.DOORBELL_UPLOAD_LOCATION, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(uploadLocation, listener));
    }

    @Override
    public void lockUpdateElectricQuantity(RequestUploadElectricQuantity requestUploadElectricQuantity, IDataListener listener) {
        HttpTaskVoid lockUpdateElectricQuantity = new HttpTaskVoid(HttpUrlIble.LOCK_UPDATE_ELECTRIC_QUANTITY, GsonUtil.toJson(requestUploadElectricQuantity), listener);
        mThreadPoolManager.excute(new FutureTask<Object>(lockUpdateElectricQuantity, listener));
    }

    @Override
    public void getUnLockKeyData(String sn, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("cateyeId", sn);
        HttpTask unLockKeyData = new HttpTask(HttpUrlIble.LOCK_GET_KEY_DATA, params, UnLockData.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(unLockKeyData, listener));
    }

    @Override
    public void lockUploadLog(int lockId, String accessToken, String records, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("lockId", lockId);
        params.put("accessToken", accessToken);
        params.put("records", records);
        HttpTaskVoid lockUploadLog = new HttpTaskVoid(HttpUrlIble.LOCK_RECORD_UPLOAD, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(lockUploadLog, listener));
    }

    @Override
    public void uploadLog(LogRecord logRecord, IDataListener listener) {
        HttpTaskVoid lockUploadLog = new HttpTaskVoid(HttpUrlIble.DOORBELL_LOG_UPLOAD, GsonUtil.toJson(logRecord), listener);
        mThreadPoolManager.excute(new FutureTask<Object>(lockUploadLog, listener));
    }
}
