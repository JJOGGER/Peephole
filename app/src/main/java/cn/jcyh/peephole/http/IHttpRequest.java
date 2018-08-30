package cn.jcyh.peephole.http;

import cn.jcyh.peephole.http.download.ProgressHttpListener;

/**
 * Created by jogger on 2018/7/17.
 */
public interface IHttpRequest {
    void initNIM(String deviceID, IDataListener listener);

//    void initDoorbell(String deviceID, String appID, int timestamp, IDataListener listener);

//    void getBindUsers(String deviceID, IDataListener listener);

    void setDoorbellConfig(String deviceID, String configJson, IDataListener listener);

    void getDoorbellConfig(String deviceID, IDataListener listener);

    void unbindUser(String userID, String deviceID, String authorizationCode, IDataListener listener);

    void sendDoorbellImg(String deviceID, String command, int type, String filePath, IDataListener listener);

    void setDoorbellName(String deviceID, String name, IDataListener listener);

    void getVersion(IDataListener listener);

    void updateVersion(String url, String savePath, ProgressHttpListener progressHttpListener, IDataListener listener);

    void uploadBattery(String imei, int battery, IDataListener listener);

    void getBanners(int terminalSize,IDataListener listener);

    void getBindUsers(String imei, IDataListener listener);

    void setDoorbellManager(String imei, String userID, String authorizationCode, IDataListener listener);
}
