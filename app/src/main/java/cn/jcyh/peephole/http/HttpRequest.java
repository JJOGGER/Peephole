package cn.jcyh.peephole.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

import cn.jcyh.peephole.entity.AdvertData;
import cn.jcyh.peephole.entity.ConfigData;
import cn.jcyh.peephole.entity.Doorbell;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.http.download.ProgressHttpListener;
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
    public void initNIM(String deviceID, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("DeviceId", deviceID);
        HttpTask httpTask = new HttpTask(HttpUrlIble.INIT_NIM, params, Doorbell.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTask, listener));
    }

    @Override
    public void setDoorbellConfig(String deviceID, String configJson, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceID);
        params.put("config", configJson);
        HttpTaskVoid httpTask = new HttpTaskVoid(HttpUrlIble.DOORBELL_SET_CONFIG_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTask, listener));
    }

    @Override
    public void getDoorbellConfig(String deviceID, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceID);
        HttpTask httpTask = new HttpTask(HttpUrlIble.DOORBELL_GET_CONFIG_URL, params, ConfigData.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTask, listener));
    }

    @Override
    public void unbindUser(String userID, String deviceID, String authorizationCode, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("UserId", userID);
        params.put("DeviceId", deviceID);
        params.put("AuthorizationCode", authorizationCode);
        HttpTaskVoid httpTask = new HttpTaskVoid(HttpUrlIble.DOORBELL_UNBIND_USER_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTask, listener));
    }

    @Override
    public void sendDoorbellImg(String deviceID, String command, int type, String filePath, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", deviceID);
        params.put("type", type);
        params.put("command", command);
        HttpTaskVoid httpTaskVoid = new HttpTaskVoid(HttpUrlIble.UPLOAD_DOORBELL_ALARM_URL, filePath, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTaskVoid, listener));
    }

    @Override
    public void setDoorbellName(String deviceID, String name, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("DeviceId", deviceID);
        params.put("Name", name);
        HttpTaskVoid httpTaskVoid = new HttpTaskVoid(HttpUrlIble.DOORBELL_SET_NAME_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(httpTaskVoid, listener));
    }

    @Override
    public void getVersion(int versionCode,IDataListener listener) {
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
    public void uploadBattery(String imei, int battery, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("DeviceId", imei);
        params.put("ElectricQuantity", battery);
        HttpTaskVoid updateVersion = new HttpTaskVoid(HttpUrlIble.DOORBELL_UPLOAD_BATTERY_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updateVersion, listener));
    }

    @Override
    public void getBanners(int terminalSize, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("terminalSize", terminalSize);
        HttpTask banners = new HttpTask(HttpUrlIble.DOORBELL_GET_BANNERS_URL, null, AdvertData.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(banners, listener));
    }

    @Override
    public void getBindUsers(String imei, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("DeviceId", imei);
        HttpTaskList bindUsers = new HttpTaskList(HttpUrlIble.DOORBELL_GET_BIND_USERS_URL, params, User.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(bindUsers, listener));
    }

    @Override
    public void setDoorbellManager(String imei, String userID, String authorizationCode, IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", imei);
        params.put("userId", userID);
        params.put("authorizationCode", authorizationCode);
        HttpTaskVoid updateVersion = new HttpTaskVoid(HttpUrlIble.DOORBELL_SET_MANAGER_URL, params, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updateVersion, listener));
    }

    @Override
    public void updatePatch(IDataListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("CurrentVersion", SystemUtil.getVersionCode());
        HttpTask updatePatch = new HttpTask(HttpUrlIble.DOORBELL_UPDATE_PATCH_URL, params, Version.class, listener);
        mThreadPoolManager.excute(new FutureTask<Object>(updatePatch, listener));
    }
}
