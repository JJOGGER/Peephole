package cn.jcyh.peephole.manager;

import android.graphics.Bitmap;

import cn.jcyh.peephole.entity.ConfigData;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.http.IDataListener;

/**
 * Created by jogger on 2018/8/6.
 */
public interface IDoorbellManager {
    DoorbellConfig getDoorbellConfig();

    void setDoorbellConfig(DoorbellConfig config);

    void setDoorbellMediaCount(int count);

    int getDoorbellMediaCount();

    void setDoorbellLeaveMsgCount(int count);

    int getDoorbellLeaveMsgCount();

    void setDoorbellConfig2Server(String deviceID, DoorbellConfig config, IDataListener<Boolean> listener);

    void getDoorbellConfigFromServer(String deviceID, IDataListener<ConfigData> listener);

    void updateNickname(String deviceID, String name, IDataListener<Boolean> listener);

    void sendDoorbellImg(String deviceId, Bitmap bitmap, String type, IDataListener<Boolean> listener);

    void setLastVideoTime(long lastVideoTime);

    long getLastVideoTime();
}
