package cn.jcyh.peephole.http;

import cn.jcyh.peephole.http.download.ProgressHttpListener;

/**
 * Created by jogger on 2018/7/17.
 */
public interface IHttpRequest {
    void initNIM(IDataListener listener);

//    void initDoorbell(String deviceID, String appID, int timestamp, IDataListener listener);

//    void getBindUsers(String deviceID, IDataListener listener);

    void setDoorbellConfig(String configJson, IDataListener listener);

    void getDoorbellConfig(IDataListener listener);

    void unbindUser(String userID, String authorizationCode, IDataListener listener);

    void sendDoorbellImg(String command, int type, String filePath, IDataListener listener);

    void setDoorbellName(String name, IDataListener listener);

    void getSysVersion(int versionCode, String sysVersion, String screenResolution, IDataListener listener);

    void updateVersion(String url, String savePath, ProgressHttpListener progressHttpListener, IDataListener listener);

    void uploadBattery(int battery, IDataListener listener);

    void getBanners(int terminalSize, IDataListener listener);

    void getBindUsers(IDataListener listener);

    void setDoorbellManager(String userID, String authorizationCode, IDataListener listener);

    void updatePatch(String sysVersion, String screenResolution, IDataListener listener);

    void updateSoft(String sysVersion, String screenResolution, IDataListener listener);

    void updateSystem(int versionCode, String sysVersion, String screenResolution, IDataListener listener);

    void antiBreakAlarm(boolean isAlarm, IDataListener listener);

    void doorbellMagneticNotice(boolean isOpen, IDataListener listener);

    void userTaklTimeRecord(String userID, long sessionDuration, IDataListener listener);

    void uploadLocation(double longitude, double latitude, String country, String province, String city, String district, String street, String addrStr, IDataListener listener);
}
