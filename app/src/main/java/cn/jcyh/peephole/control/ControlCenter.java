package cn.jcyh.peephole.control;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;

import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.entity.Doorbell;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.manager.IBCManager;
import cn.jcyh.peephole.manager.IDoorbellManager;
import cn.jcyh.peephole.manager.IUserManager;
import cn.jcyh.peephole.manager.impl.BCManager;
import cn.jcyh.peephole.manager.impl.DoorbellManager;
import cn.jcyh.peephole.manager.impl.UserManager;
import cn.jcyh.peephole.utils.GsonUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.NetworkUtil;
import cn.jcyh.peephole.utils.SPUtil;

/**
 * Created by jogger on 2017/3/17.
 * 控制中心
 */

public class ControlCenter {
    public static final int DOORBELL_TYPE_RING = 0;
    public static final int DOORBELL_TYPE_ALARM = 1;
    public static boolean sIsVideo;//标记是否正在使用相机
    public static boolean sIsBinding;//标记是否正在绑定中
    public static boolean sIsLeaveMsgRecording;//标记是否正在留言中
    public static boolean sIsDownloadUpdate;//标记是否正在更新软件版本
    private static IUserManager sUserManager;//猫眼用户管理
    private static IDoorbellManager sDoorbellManager;//猫眼信息管理
    private static IBCManager sBCManager;//猫眼硬件管理

    public static IUserManager getUserManager() {
        if (sUserManager == null) {
            sUserManager = new UserManager();
        }
        return sUserManager;
    }

    public static IDoorbellManager getDoorbellManager() {
        if (sDoorbellManager == null) {
            sDoorbellManager = new DoorbellManager();
        }
        return sDoorbellManager;
    }

    public static IBCManager getBCManager() {
        if (sBCManager == null) {
            sBCManager = new BCManager();
        }
        return sBCManager;
    }

    /**
     * 获取imei
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getIMEI() {
//        String simSerialNumber;
//        try {
//            //实例化TelephonyManager对象
//            TelephonyManager telephonyManager = (TelephonyManager) Util.getApp().getSystemService(Context.TELEPHONY_SERVICE);
//            assert telephonyManager != null;
//            String simSerialNumber = Build.SERIAL;
////            simSerialNumber = telephonyManager.getSimSerialNumber();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
        if ("0123456789ABCDEF".equals(Build.SERIAL))
            return "";
        return Build.SERIAL;
    }

    /**
     * 连接网易
     */
    @SuppressWarnings("unchecked")
    public static void connectNIM() {
        if (!NetworkUtil.isConnected()) return;
        final DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        L.e("-------------connectNIM:" + doorbellConfig);
        if (TextUtils.isEmpty(ControlCenter.getIMEI())) return;
        HttpAction.getHttpAction().initNIM(ControlCenter.getIMEI(), new IDataListener<Doorbell>() {
            @Override
            public void onSuccess(Doorbell doorbell) {
                doorbellConfig.setDoorbell(doorbell);
                ControlCenter.getDoorbellManager().setDoorbellConfig(doorbellConfig);
                LoginInfo info = new LoginInfo(doorbellConfig.getDoorbell().getDeviceUserId(), doorbellConfig.getDoorbell().getToken()); // config...
                final AbortableFuture<LoginInfo> login = NIMClient.getService(AuthService.class).login(info);
                RequestCallback<LoginInfo> callback =
                        new RequestCallback<LoginInfo>() {
                            @Override
                            public void onSuccess(LoginInfo loginInfo) {
                                L.e("------------onSuccess");
                            }

                            @Override
                            public void onFailed(int i) {
                                L.e("-----------onFailed" + i);
                                if (i != 416) {
                                    login.abort();
                                    connectNIM();//重新连接
                                }
                            }

                            @Override
                            public void onException(Throwable throwable) {
                                L.e("-----------登录失败：" + throwable.getMessage());
                            }
                        };
                login.setCallback(callback);
            }

            @Override
            public void onFailure(int errorCode, String desc) {
                L.e("--------errorCode:" + errorCode);
            }
        });

    }

    public static void setNewVersion(Version version) {
        SPUtil.getInstance().put(Constant.VERSION, version == null ? "" : GsonUtil.toJson(version));
    }

    public static Version getNewVersion() {
        String json = SPUtil.getInstance().getString(Constant.VERSION, "");
        return TextUtils.isEmpty(json) ? null : GsonUtil.fromJson(json, Version.class);
    }

    /**
     * 设置开启拓展功能
     *
     * @param function {@link cn.jcyh.peephole.constant.ExtendFunction}
     * @param isUse    是否被使用
     */
    public static void setFunctionUse(String function, boolean isUse) {
        SPUtil.getInstance().put(function, isUse);
    }

    /**
     * 判断拓展功能是否开启
     */
    public static boolean isFunctionUse(String function) {
        return SPUtil.getInstance().getBoolean(function);
    }

    /**
     * 注册人脸识别
     */
    public static void registFaceVali(boolean isRegist) {
        SPUtil.getInstance().put(Constant.IS_FACE_VALI_REGISTED, isRegist);
    }

    public static boolean isRegistedFaceVali() {
        return SPUtil.getInstance().getBoolean(Constant.IS_FACE_VALI_REGISTED);
    }

    /**
     * 注册声纹识别
     */
    public static void registAudioVali(boolean isRegist) {
        SPUtil.getInstance().put(Constant.IS_AUDIO_VALI_REGISTED, isRegist);
    }

    public static boolean isRegistedAudioVali() {
        return SPUtil.getInstance().getBoolean(Constant.IS_AUDIO_VALI_REGISTED);
    }

}
