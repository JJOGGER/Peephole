package cn.jcyh.peephole.http;

import java.util.List;

import cn.jcyh.peephole.entity.AdvertData;
import cn.jcyh.peephole.entity.ConfigData;
import cn.jcyh.peephole.entity.Doorbell;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.http.download.ProgressHttpListener;
import cn.jcyh.peephole.utils.NetworkUtil;

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

    public void initNIM(String deviceID, IDataListener<Doorbell> listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.initNIM(deviceID, listener);
    }

    /**
     * 设置猫眼名称
     */
    public void setDoorbellName(String deviceID, String name, IDataListener<Boolean> listener) {
        mHttpRequest.setDoorbellName(deviceID, name, listener);
    }

    /**
     * 设置猫眼参数
     */
    public void setDoorbellConfig(String deviceID, DoorbellConfig config, IDataListener<Boolean> listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.setDoorbellConfig(deviceID, DoorbellConfig.getGson().toJson(config), listener);
    }

    public void getDoorbellConfig(String deviceID, final IDataListener<ConfigData> listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.getDoorbellConfig(deviceID, listener);
    }

    /**
     * 解绑用户关系
     */
    public void unbindUser(String userID, String deviceID, String authorizationCode, final IDataListener<Boolean> listener) {
        mHttpRequest.unbindUser(userID, deviceID, authorizationCode, listener);
    }

    /**
     * 广告图
     */
//    public void getADPictures(final IDataListener<List<Advert>> listener) {
//        request2(HttpUrlIble.DOORBELL_AD_GET_URL, null, Advert.class, listener);
//    }
    public void sendDoorbellImg(String deviceId, String command, int type, String filePath, final IDataListener<Boolean> listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.sendDoorbellImg(deviceId, command, type, filePath, listener);
    }

    /**
     * 获取版本信息
     */
    public void getVersion(IDataListener<Version> listener) {
        mHttpRequest.getVersion(listener);
    }

    /**
     * 更新软件版本
     */
    public void updateVersion(String url, String savePath, ProgressHttpListener progressHttpListener, IDataListener<Boolean> listener) {
        mHttpRequest.updateVersion(url, savePath,progressHttpListener, listener);
    }

    /**
     * 上传电量
     */
    public void uploadBattery(String imei, int battery, IDataListener<Boolean> listener) {
        mHttpRequest.uploadBattery(imei, battery, listener);
    }

    /**
     * 获取banner
     */
    public void getBanners(int terminalSize,IDataListener<AdvertData> listener) {
        if (!NetworkUtil.isConnected()) return;
        mHttpRequest.getBanners(terminalSize,listener);
    }

    /**
     * 获取绑定猫眼的用户列表
     */
    public void getBindUsers(String imei, IDataListener<List<User>> listener) {
        mHttpRequest.getBindUsers(imei, listener);
    }

    /**
     * 指定设备的管理员
     */
    public void setDoorbellManager(String imei, String userID, String authorizationCode, IDataListener<List<User>> listener) {
        mHttpRequest.setDoorbellManager(imei, userID, authorizationCode, listener);
    }
}

