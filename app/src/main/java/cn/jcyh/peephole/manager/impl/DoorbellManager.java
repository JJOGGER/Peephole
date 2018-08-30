package cn.jcyh.peephole.manager.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.constant.SPConstant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.CommandJson;
import cn.jcyh.peephole.entity.ConfigData;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.manager.IDoorbellManager;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.GsonUtil;
import cn.jcyh.peephole.utils.ImgUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.NetworkUtil;
import cn.jcyh.peephole.utils.SPUtil;
import cn.jcyh.peephole.utils.ScreenUtil;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.utils.Util;

/**
 * Created by jogger on 2018/8/6.猫眼管理
 */
public class DoorbellManager implements IDoorbellManager {
    private static DoorbellConfig sDoorbellConfig;

    /**
     * 获取猫眼配置
     */
    @Override
    public DoorbellConfig getDoorbellConfig() {
        if (sDoorbellConfig != null) return sDoorbellConfig;
        DoorbellConfig config;
        String configJson = FileUtil.readFile(FileUtil.getDoorbellDataPath());
        if (TextUtils.isEmpty(configJson)) {
            config = new DoorbellConfig();
            setDoorbellConfig(config);
            FileUtil.readFile(FileUtil.getDoorbellDataPath());
            //保存到服务器
            if (!NetworkUtil.isConnected()) {
                return config;
            }
            HttpAction.getHttpAction().setDoorbellConfig(ControlCenter.getIMEI(), config, null);
        } else {
            config = GsonUtil.fromJson(configJson, DoorbellConfig.class);
        }
        sDoorbellConfig = config;
        return config;
    }

    /**
     * 猫眼配置
     */
    @Override
    public void setDoorbellConfig(DoorbellConfig config) {
        sDoorbellConfig = config;
        File file = new File(FileUtil.getDoorbellDataPath());
        FileUtil.writeFile(file, GsonUtil.toJson(config));
    }

    /**
     * 设置影像记录未读
     */
    @Override
    public void setDoorbellMediaCount(int count) {
        SPUtil.getInstance().put(SPConstant.MEDIA_MSG_COUNT, count > 0 ? count : 0);
    }

    /**
     * 获取影响记录未读
     */
    @Override
    public int getDoorbellMediaCount() {
        return SPUtil.getInstance().getInt(SPConstant.MEDIA_MSG_COUNT, 0);
    }

    /**
     * 设置留言记录未读
     */
    @Override
    public void setDoorbellLeaveMsgCount(int count) {
        SPUtil.getInstance().put(SPConstant.LEAVE_MSG_COUNT, count > 0 ? count : 0);
    }

    /**
     * 获取留言记录未读
     */
    @Override
    public int getDoorbellLeaveMsgCount() {
        return SPUtil.getInstance().getInt(SPConstant.LEAVE_MSG_COUNT, 0);
    }

    @Override
    public void setDoorbellConfig2Server(String deviceID, final DoorbellConfig config, IDataListener<Boolean> listener) {
        HttpAction.getHttpAction().setDoorbellConfig(deviceID, config, listener);
    }

    @Override
    public void getDoorbellConfigFromServer(String deviceID, IDataListener<ConfigData> listener) {
        HttpAction.getHttpAction().getDoorbellConfig(deviceID, listener);
    }


    /**
     * 修改设备名称
     */
    @Override
    public void updateNickname(String deviceID, String name, IDataListener<Boolean> listener) {
        if (!NetworkUtil.isConnected()) {
            T.show(R.string.network_is_not_available);
            return;
        }
        HttpAction.getHttpAction().setDoorbellName(deviceID, name, listener);
    }

    /**
     * 报警门铃抓拍缩略图提交服务器{@link DoorbellSystemAction}
     */
    @Override
    public void sendDoorbellImg(String deviceId, Bitmap bitmap, String type, IDataListener<Boolean> listener) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Util.getApp().getResources().getConfiguration().locale);
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        String imagePath = FileUtil.getDoorbellImgPath() + File.separator + "IMG_" + time +
                ".jpg";
        String thumbPath = FileUtil.getDoorbellImgThumbnailPath() + File.separator + "IMG_" + time +
                ".jpg";
        simpleDateFormat.applyPattern("yyyy/MM/dd HH:mm:ss");
        time = simpleDateFormat.format(date);
        int heightPixels = ScreenUtil.getSrceenHeight();
        int widthPixels = ScreenUtil.getSrceenWidth();
        //抓拍存储到本地
        ImgUtil.createWaterMaskWidthText(imagePath, thumbPath, bitmap,
                BitmapFactory.decodeResource(Util.getApp().getResources(), R.mipmap.eagleking),
                time, heightPixels, widthPixels);
        int count = ControlCenter.getDoorbellManager().getDoorbellMediaCount() + 1;
        ControlCenter.getDoorbellManager().setDoorbellMediaCount(count);
        //获取拍照的图片
        int t = 0;
        if (DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING.equals(type)) {
            t = ControlCenter.DOORBELL_TYPE_RING;
        } else if (DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM.equals(type)) {
            t = ControlCenter.DOORBELL_TYPE_ALARM;
        }
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_NOTIFICATION);
        if (DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING.equals(type)) {
            commandJson.setCommand(CommandJson.CommandType.NOTIFICATION_DOORBELL_RING);
        } else if (DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM.equals(type)) {
            commandJson.setCommand(CommandJson.CommandType.NOTIFICATION_DOORBELL_ALARM);
        }
        String command = GsonUtil.toJson(commandJson);
        L.e("--------------------------按门铃图片上传" + thumbPath);
        HttpAction.getHttpAction().sendDoorbellImg(ControlCenter.getIMEI(), command, t,
                thumbPath, listener);
    }

    @Override
    public void setLastVideoTime(long lastVideoTime) {
        SPUtil.getInstance().put(Constant.LAST_VIDEO_TIME, lastVideoTime);
    }

    @Override
    public long getLastVideoTime() {
        return SPUtil.getInstance().getLong(Constant.LAST_VIDEO_TIME);
    }
}
