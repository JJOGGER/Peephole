package com.bairuitech.anychat.config;

import android.content.Context;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;

import java.util.Locale;

public class ConfigHelper {
    private static ConfigHelper mConfigHelper;
    private static final String SP_FILE_PATH = "perference";
    private static Context mContext;

    private ConfigHelper() {

    }

    public static ConfigHelper getConfigHelper(Context context) {
        mContext = context.getApplicationContext();
        if (mConfigHelper == null) {
            synchronized (ConfigHelper.class) {
                if (mConfigHelper == null) {
                    mConfigHelper = new ConfigHelper();
                }
            }
        }
        return mConfigHelper;
    }

    /**
     * 读取配置文件
     */
    public ConfigEntity LoadConfig() {
        ConfigEntity configEntity = new ConfigEntity();
        AnyChatSharePreUtil share = AnyChatSharePreUtil.getInstance(mContext);
        configEntity.ip =Constants.ADDRESS;
        configEntity.port = Constants.PORT;//8906--anychat测试服务器端口

        configEntity.configMode = share.getInt("configMode", 1);
        configEntity.resolution_width = 320;//分辨率
        configEntity.resolution_height = 240;
        configEntity.videoBitrate = 300 * 1000;
        configEntity.videoFps = 15;
        configEntity.videoQuality = 2;//视频质量
        configEntity.videoPreset = 4;
        configEntity.videoOverlay = share.getInt("videoOverlay", 1);
        configEntity.videorotatemode = share.getInt("VideoRotateMode", 0);
        configEntity.videoCapDriver = share.getInt("VideoCapDriver", AnyChatDefine.VIDEOCAP_DRIVER_JAVA);
        configEntity.fixcolordeviation =share.getInt("FixColorDeviation", 0);
        configEntity.videoShowGPURender = share.getInt("videoShowGPURender", 0);
        configEntity.videoAutoRotation = share.getInt("videoAutoRotation", 1);

        configEntity.enableP2P = share.getInt("enableP2P", 1);
        configEntity.useARMv6Lib = share.getInt("useARMv6Lib", 0);
        configEntity.enableAEC = share.getInt("enableAEC", 1);
        configEntity.useHWCodec = share.getInt("useHWCodec", 0);
        configEntity.videoShowDriver = share.getInt("videoShowDriver", AnyChatDefine.VIDEOSHOW_DRIVER_JAVA);
        configEntity.audioPlayDriver = share.getInt("audioPlayDriver", AnyChatDefine.AUDIOPLAY_DRIVER_JAVA);
        configEntity.audioRecordDriver = share.getInt("audioRecordDriver", AnyChatDefine.AUDIOREC_DRIVER_JAVA);
        return configEntity;
    }

    /**
     * 判断当前语言环境
     *
     * @return
     */
    private boolean isZh() {
        Locale locale = mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    /**
     * 保存配置信息
     */
    public void saveConfig(ConfigEntity configEntity) {
        AnyChatSharePreUtil share = AnyChatSharePreUtil.getInstance(mContext);
        share.putString("ip", configEntity.ip);
        share.putInt("port", configEntity.port);

        share.putInt("configMode", configEntity.configMode);
        share.putInt("resolution_width", configEntity.resolution_width);
        share.putInt("resolution_height", configEntity.resolution_height);

        share.putInt("videoBitrate", configEntity.videoBitrate);
        share.putInt("videoFps", configEntity.videoFps);
        /*Begin modify by shaunliu for door mode select*/
        share.putInt("videoQuality", configEntity.videoQuality);
        /*End modify by shaunliu for door mode select*/
        share.putInt("videoPreset", configEntity.videoPreset);
        share.putInt("videoOverlay", configEntity.videoOverlay);
        share.putInt("VideoRotateMode", configEntity.videorotatemode);
        share.putInt("VideoCapDriver", configEntity.videoCapDriver);
        share.putInt("FixColorDeviation", configEntity.fixcolordeviation);
        share.putInt("videoShowGPURender", configEntity.videoShowGPURender);
        share.putInt("videoAutoRotation", configEntity.videoAutoRotation);

        share.putInt("enableP2P", configEntity.enableP2P);
        share.putInt("useARMv6Lib", configEntity.useARMv6Lib);
        share.putInt("enableAEC", configEntity.enableAEC);
        share.putInt("useHWCodec", configEntity.useHWCodec);
        share.putInt("videoShowDriver", configEntity.videoShowDriver);
        share.putInt("audioPlayDriver", configEntity.audioPlayDriver);
        share.putInt("audioRecordDriver", configEntity.audioRecordDriver);
    }

    // 根据配置文件设置视频参数
    public void applyVideoConfig() {
        ConfigEntity configEntity = mConfigHelper.LoadConfig(); //读取配置对象
        if (configEntity.configMode == 1)        // 自定义视频参数配置
        {
            // 设置本地视频编码的码率（如果码率为0，则表示使用质量优先模式）
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_BITRATECTRL, configEntity.videoBitrate);
            if (configEntity.videoBitrate == 0) {
                // 设置本地视频编码的质量
                AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_QUALITYCTRL, configEntity.videoQuality);
            }
            // 设置本地视频编码的帧率
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_FPSCTRL, configEntity.videoFps);
            // 设置本地视频编码的关键帧间隔
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_GOPCTRL, configEntity.videoFps * 4);
            // 设置本地视频采集分辨率
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL, configEntity.resolution_width);
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL, configEntity.resolution_height);
            // 设置视频编码预设参数（值越大，编码质量越高，占用CPU资源也会越高）
            AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_PRESETCTRL, configEntity.videoPreset);
        }
        // 让视频参数生效
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_APPLYPARAM, configEntity.configMode);
        // P2P设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_NETWORK_P2PPOLITIC, configEntity.enableP2P);
        // 本地视频Overlay模式设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_OVERLAY, configEntity.videoOverlay);
        // 回音消除设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_AUDIO_ECHOCTRL, configEntity.enableAEC);
        // 平台硬件编码设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_CORESDK_USEHWCODEC, configEntity.useHWCodec);
        // 视频旋转模式设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_ROTATECTRL, configEntity.videorotatemode);
        // 视频采集驱动设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER, configEntity.videoCapDriver);
        // 本地视频采集偏色修正设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_FIXCOLORDEVIA, configEntity.fixcolordeviation);
        // 视频显示驱动设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL, configEntity.videoShowDriver);
        // 音频播放驱动设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_AUDIO_PLAYDRVCTRL, configEntity.audioPlayDriver);
        // 音频采集驱动设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_AUDIO_RECORDDRVCTRL, configEntity.audioRecordDriver);
        // 视频GPU渲染设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_GPUDIRECTRENDER, configEntity.videoShowGPURender);
        // 本地视频自动旋转设置
        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION, configEntity.videoAutoRotation);


    }
}
