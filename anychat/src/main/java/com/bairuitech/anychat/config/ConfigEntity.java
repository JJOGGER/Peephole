package com.bairuitech.anychat.config;

//anychat配置参数
public class ConfigEntity {

    public static final int VIDEO_QUALITY_NORMAL = 2;        // 普通视频质量
    public static final int VIDEO_QUALITY_GOOD = 3;            // 中等视频质量
    public static final int VIDEO_QUALITY_BEST = 4;            // 较好视频质量

    public String ip = "";//用户ip
    public int port;//用户端口

    public int configMode = 1;//0，服务器视频参数配置 1。 自定义视频参数配置
    public int resolution_width = 0;//本地视频采集分辨率宽度ConfigHelper设置
    public int resolution_height = 0;//本地视频采集分辨率高度


    public int videoBitrate = 150 * 1000;                        // 本地视频码率
    public int videoFps = 15;                                // 本地视频帧率
    public int videoQuality = VIDEO_QUALITY_GOOD;
    public int videoPreset = 1;
    public int videoOverlay = 1;                            // 本地视频是否采用Overlay模式
    public int videorotatemode = 0;                            // 本地视频旋转模式
    public int videoCapDriver = 3;                            // 本地视频采集驱动（0 默认， 1 Linux驱动，3 Java驱动
    public int fixcolordeviation = 1;                        // 修正本地视频采集偏色：0 关闭(默认）， 1 开启
    public int videoShowGPURender = 0;                        // 视频数据通过GPU直接渲染：0  关闭(默认)， 1 开启
    public int videoAutoRotation = 1;                        // 本地视频自动旋转控制（参数为int型， 0表示关闭， 1 开启[默认]，视频旋转时需要参考本地视频设备方向参数）

    public int enableP2P = 1;
    public int useARMv6Lib = 0;                                // 是否强制使用ARMv6指令集，默认是内核自动判断
    public int enableAEC = 1;                                // 是否使用回音消除功能
    public int useHWCodec = 0;                                // 是否使用平台内置硬件编解码器
    public int videoShowDriver = 5;                            // 视频显示驱动（0 默认， 4 Android 2.x兼容模式，5 Java驱动）
    public int audioPlayDriver = 3;                            // 音频播放驱动（0 默认，3 Java驱动）
    public int audioRecordDriver = 3;                        // 音频采集驱动（0默认，3 Java驱动）
}
