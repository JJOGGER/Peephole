package cn.jcyh.peephole.http;

import java.util.List;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.entity.AdvertData;
import cn.jcyh.peephole.entity.ConfigData;
import cn.jcyh.peephole.entity.Doorbell;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.LogRecord;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.http.download.ProgressHttpListener;
import cn.jcyh.peephole.utils.NetworkUtil;
import cn.jcyh.peephole.utils.SystemUtil;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.utils.Util;

/**
 * Created by jogger on 2018/1/10.
 */

public class HttpAction extends BaseHttpAction {
    private static HttpAction sHttpAction;

    @Override
    IHttpRequest getHttpRequest() {
        return new HttpRequest();
    }

    private HttpAction() {
    }

    public static HttpAction getHttpAction() {
        if (sHttpAction == null) {
            synchronized (HttpAction.class) {
                if (sHttpAction == null) {
                    sHttpAction = new HttpAction();
                }
            }
        }
        return sHttpAction;
    }

    public void initNIM(IDataListener<Doorbell> listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.initNIM(listener);
    }

    /**
     * 设置猫眼名称
     */
    public void setDoorbellName(String name, IDataListener<Boolean> listener) {
        mHttpRequest.setDoorbellName(name, listener);
    }

    /**
     * 设置猫眼参数
     */
    public void setDoorbellConfig(DoorbellConfig config, IDataListener<Boolean> listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.setDoorbellConfig(DoorbellConfig.getGson().toJson(config), listener);
    }

    public void getDoorbellConfig(final IDataListener<ConfigData> listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.getDoorbellConfig(listener);
    }

    /**
     * 解绑用户关系
     */
    public void unbindUser(String userID, String authorizationCode, final IDataListener<Boolean> listener) {
        mHttpRequest.unbindUser(userID, authorizationCode, listener);
    }

    /**
     * 广告图
     */
//    public void getADPictures(final IDataListener<List<Advert>> listener) {
//        request2(HttpUrlIble.DOORBELL_AD_GET_URL, null, Advert.class, listener);
//    }
    public void sendDoorbellImg(String command, int type, String filePath, final IDataListener<Boolean> listener) {
        if (!NetworkUtil.isConnected()) {
            if (listener != null) {
                listener.onFailure(-1, Util.getApp().getString(R.string.network_is_not_available));
                return;
            }
        }
        mHttpRequest.sendDoorbellImg(command, type, filePath, listener);
    }

    /**
     * 获取版本信息
     */
    public void getSysVersion(int versionCode, String sysVersion, String screenResolution, IDataListener<Version> listener) {
        if (!NetworkUtil.isConnected()) {
            T.show(R.string.network_is_not_available);
            return;
        }
        mHttpRequest.getSysVersion(versionCode, sysVersion, screenResolution, listener);
    }

    /**
     * 获取版本信息
     */
    public void updatePatch(String sysVersion, String screenResolution, IDataListener<Version> listener) {
        if (!NetworkUtil.isConnected()) {
            T.show(R.string.network_is_not_available);
            return;
        }
        mHttpRequest.updatePatch(sysVersion, screenResolution, listener);
    }

    public void updateSystem(int versionCode, String sysVersion, String screenResolution, IDataListener<Version> listener) {
        if (!NetworkUtil.isConnected()) {
            T.show(R.string.network_is_not_available);
            return;
        }
        mHttpRequest.updateSystem(versionCode, sysVersion, screenResolution, listener);
    }

    /**
     * 获取版本信息
     */
    public void updateSoft(String sysVersion, String screenResolution, IDataListener<Version> listener) {
        if (!NetworkUtil.isConnected()) {
            T.show(R.string.network_is_not_available);
            return;
        }
        mHttpRequest.updateSoft(sysVersion, screenResolution, listener);
    }

    /**
     * 更新软件版本
     */
    public void updateVersion(String url, String savePath, ProgressHttpListener progressHttpListener, IDataListener<Boolean> listener) {
        mHttpRequest.updateVersion(url, savePath, progressHttpListener, listener);
    }

    /**
     * 上传电量
     */
    public void uploadBattery(int battery, IDataListener<Boolean> listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.uploadBattery(battery, listener);
    }

    /**
     * 获取banner
     */
    public void getBanners(IDataListener<AdvertData> listener) {
        if (!NetworkUtil.isConnected()) return;
        int terminalSize = SystemUtil.getTerminalSize();
        mHttpRequest.getBanners(terminalSize, listener);
    }

    /**
     * 获取绑定猫眼的用户列表
     */
    public void getBindUsers(IDataListener<List<User>> listener) {
        if (!NetworkUtil.isConnected()) {
            T.show(R.string.network_is_not_available);
            return;
        }
        mHttpRequest.getBindUsers(listener);
    }

    /**
     * 指定设备的管理员
     */
    public void setDoorbellManager(String userID, String authorizationCode, IDataListener<List<User>> listener) {
        mHttpRequest.setDoorbellManager(userID, authorizationCode, listener);
    }

    /**
     * 上传防拆报警
     */
    public void antiBreakAlarm(boolean isAlarm, IDataListener listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.antiBreakAlarm(isAlarm, listener);
    }

    /**
     * 上传门磁状态
     */
    public void doorbellMagneticNotice(boolean isOpen, IDataListener listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.doorbellMagneticNotice(isOpen, listener);
    }

    /**
     * 上传用户通话时长
     */
    public void userTaklTimeRecord(String userID, long sessionDuration, IDataListener listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.userTaklTimeRecord(userID, sessionDuration, listener);
    }

    /**
     * 上传定位
     */
    public void uploadLocation(double longitude, double latitude, String country, String province,
                               String city, String district, String street, String addrStr, IDataListener listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.uploadLocation(longitude, latitude, country, province, city, district, street, addrStr, listener);
    }

    public void uploadLog(LogRecord logRecord,IDataListener listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.uploadLog(logRecord,listener);
    }

    public void sendHeartBeat(int value, IDataListener listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.sendHeartBeat(value,listener);
    }
}

