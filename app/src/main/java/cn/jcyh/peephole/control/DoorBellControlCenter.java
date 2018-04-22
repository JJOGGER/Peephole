package cn.jcyh.peephole.control;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatOutParam;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.jcyh.peephole.bean.AnyChatTask;
import cn.jcyh.peephole.bean.CommandJson;
import cn.jcyh.peephole.bean.User;
import cn.jcyh.peephole.utils.FileUtil;
import timber.log.Timber;

/**
 * Created by jogger on 2017/3/17.
 * 控制中心
 */

public class DoorBellControlCenter {

    private static DoorBellControlCenter mDoorBellControlCenter;
    public static final String DOORBELL_PARAMS_TYPE_MODE = "mode";
    public static final String DOORBELL_PARAMS_TYPE_MONITOR = "monitor";
    public static final String DOORBELL_PARAMS_TYPE_SENSOR = "sensor";
    private static Context mContext;
    public static boolean sIsAnychatLogin = false;//标记anychat是否登录
    public AnyChatCoreSDK mAnyChat;//单例的anychat，同一事件的话可以使用这个
    private MediaPlayer mMediaPlaer;
    public static int mEventType = -1;
    private Gson mGson;
    public static boolean sIsVideo;//标记是否正在视频通话中
    public static boolean sIsBinding;//标记是否正在绑定中


    //    public static Map<String, Object> pushFlagMap;

    private DoorBellControlCenter() {
        mAnyChat = AnyChatCoreSDK.getInstance(null);
        mGson = new Gson();
    }

    public static DoorBellControlCenter getInstance(Context context) {
        mContext = context.getApplicationContext();
        if (mDoorBellControlCenter == null) {
            synchronized (DoorBellControlCenter.class) {
                if (mDoorBellControlCenter == null) {
                    mDoorBellControlCenter = new DoorBellControlCenter();
                }
            }
        }
        return mDoorBellControlCenter;
    }

//    /***
//     * 停止播放
//     */
//    public void stopSessionMis() {
//        /* End modify by shaunliu for phone can receive video request */
//        if (mMediaPlaer == null)
//            return;
//        try {
//            mMediaPlaer.pause();
//            mMediaPlaer.stop();
//            mMediaPlaer.release();
//            mMediaPlaer = null;
//        } catch (Exception e) {
//            Timber.i("media-stop: er");
//        }
//
//    }
//
//    /**
//     * 初始化在线好友数据
//     *
//     * @param isInit
//     */
//    public void initFriendDatas(int isInit) {
//        if (mCurrentDoorBellUser == null) {
//            return;
//        }
//        List<DoorBellBean> user_devices = mCurrentDoorBellUser.getUserDevices();
//        if (user_devices == null || user_devices.size() == 0) {
//            return;
//        }
//        mFriendItems.clear();
//        mFriendItems.addAll(user_devices);
//    }
//
//    public void initFriendDatas() {
//        if (mFriendItems == null || mFriendItems.size() == 0) {
//            return;
//        }
//        for (DoorBellBean doorbell :
//                mFriendItems) {
//            doorbell.setIsOnLine(0);
//        }
//    }
//
//    /***
//     * 获取好友数据
//     */
//    public void getFriendDatas() {
//        if (mCurrentDoorBellUser != null) {
//            mFriendItems.clear();
//            List<DoorBellBean> list = mCurrentDoorBellUser.getUserDevices();
//            if (list == null || list.size() == 0) {
//                return;
//            }
//            for (int i = 0; i < list.size(); i++) {
//                //获取好友在线状态
//                int device_anychat_id = list.get(i).getDevice_anychat_id();
//                int onLineStatus = mAnyChat.GetFriendStatus(device_anychat_id);
//                DoorBellBean doorBellBean = list.get(i);
//                doorBellBean.setIsOnLine(onLineStatus);//设置在线状态
//                mFriendItems.add(doorBellBean);
//            }
//
//            //按用户在线和不在线排序
//            int len = mFriendItems.size();
//            for (int k = len - 1; k >= 0; k--) {
//                if (mFriendItems.get(k).getIsOnLine() == 0) {
//                    //把它调整到最后
//                    mFriendItems.add(list.get(k));
//                    mFriendItems.remove(k);
//                }
//            }
//        }
//    }
//
//    /***
//     * 通过anychatid拿到猫眼
//     */
//    public DoorBellBean getDoorBellByAnychatId(int anychatId) {
//        if (mCurrentDoorBellUser != null) {
//            mFriendItems.clear();
//            List<DoorBellBean> list = mCurrentDoorBellUser.getUserDevices();
//            if (list != null && list.size() != 0) {
//                mFriendItems.addAll(list);
//                for (int i = 0; i < list.size(); i++) {
//                    int device_anychat_id = list.get(i).getDevice_anychat_id();
//                    if (anychatId == device_anychat_id) {
//                        return list.get(i);
//                    }
//                }
//            }
//
//        }
//        return null;
//    }
//
//    public interface OnLoginDoorBellListener {
//        void onSuccess(String uid);
//
//        void onFailure(String errorCode);
//    }
//
//    private boolean isReLogin = false;
//
//    /***
//     * 通过用户id获取用户对象
//     *
//     * @param userId 用户id
//     */
//    public DoorBellBean getUserItemByUserId(int userId) {
//        Timber.e("-------mFriendItems" + mFriendItems);
//        if (mFriendItems != null) {
//            int size = mFriendItems.size();
//            for (int i = 0; i < size; i++) {
//                DoorBellBean userItem = mFriendItems.get(i);
//
//                if (userItem != null && userItem.getDevice_anychat_id() == userId) {
//                    return userItem;
//                }
//            }
//        }
//        return null;
//    }
//
//    //清空操作
//    public void realse() {
//        mAnyChat = null;
//        mFriendItems = null;
//        mDoorBellControlCenter = null;
//    }
//
//    /**
//     * 清空数据
//     */
//    public void realseData() {
//        mFriendItems.clear();
//    }
//

    /***
     * 发送呼叫事件
     *
     * @param dwEventType 视频呼叫事件类型
     * @param dwUserId    目标userid
     * @param dwErrorCode 原因
     * @param dwFlags     功能标志
     * @param dwParam     自定义参数，传给对方
     * @param szUserStr   自定义参数，传给对方
     */
    public void videoCallContrl(int dwEventType, int dwUserId,
                                int dwErrorCode, int dwFlags, int dwParam, String szUserStr) {
        mEventType = dwEventType;
        mAnyChat.VideoCallControl(dwEventType, dwUserId, dwErrorCode, dwFlags,
                dwParam, szUserStr);
    }
//
//    @SuppressWarnings("unused")
//    @Override
//    public void VideoCall_SessionRequest(int dwUserId, int dwFlags,
//                                         int dwParam, String szUserStr) {
//        // 如果程序在后台，通知有呼叫请求
//    }
//
//    @Override
//    public void VideoCall_SessionReply(int dwUserId, int dwErrorCode,
//                                       int dwFlags, int dwParam, String szUserStr) {
//        String strMessage = null;
//        switch (dwErrorCode) {
//            case ERRORCODE_SESSION_BUSY:
//                strMessage = mContext.getString(R.string.str_returncode_bussiness);
//                break;
//            case ERRORCODE_SESSION_REFUSE:
//                strMessage = mContext
//                        .getString(R.string.str_returncode_requestrefuse);
//                break;
//            case ERRORCODE_SESSION_OFFLINE:
//                strMessage = mContext.getString(R.string.str_returncode_offline);
//                break;
//            case ERRORCODE_SESSION_QUIT:
//                strMessage = mContext
//                        .getString(R.string.str_returncode_requestcancel);
//                break;
//            case ERRORCODE_SESSION_TIMEOUT:
//                strMessage = mContext.getString(R.string.str_returncode_timeout);
//                break;
//            case ERRORCODE_SESSION_DISCONNECT:
//                strMessage = mContext.getString(R.string.str_returncode_disconnect);
//                break;
//            case ERRORCODE_SUCCESS:
//                break;
//            default:
//                break;
//        }
//        if (strMessage != null) {
//            ToastUtil.showToast(mContext, strMessage);
//            stopSessionMis();
//        }
//
//    }
//
//    @Override
//    public void VideoCall_SessionStart(Context context, int dwUserId, int dwFlags, int dwParam,
//                                       String szUserStr) {
//        stopSessionMis();
////        sessionItem = new SessionItem(dwFlags, mSelfUserId, dwUserId);
////        sessionItem.setRoomId(dwParam);
//        Intent intent = new Intent();
//        intent.setClass(context, VideoActivity.class);
//        DoorBellBean doorBellBean = getUserItemByUserId(dwUserId);
//        intent.putExtra("doorBell", doorBellBean);
//        intent.putExtra("roomId", dwParam);
//        context.startActivity(intent);
//    }
//
//    @Override
//    public void VideoCall_SessionEnd(int dwUserId, int dwFlags, int dwParam,
//                                     String szUserStr) {
//        deviceVer = null;
//        sessionItem = null;
//    }
//

    /**
     * 进入房间
     */
    public void enterRoom(int roomId, String roomPwd) {
        mAnyChat.EnterRoom(roomId, roomPwd);
    }

    /**
     * 离开房间
     */
    public void leaveRoom(int roomId) {
        mAnyChat.LeaveRoom(roomId);
    }

    /**
     * 视频帮助类
     */
    public DoorbellVideoHelper getDoorbellVideoHelper() {
        return new DoorbellVideoHelper();
    }

    /**
     * 添加指令
     *
     * @param targetUserId 目标userid
     */
    public void addFriend(int targetUserId) {
        String add = "addfriend:" + targetUserId;
        byte[] buf = add.getBytes();
        mAnyChat.TransBuffer(0, buf, buf.length);
    }

    /**
     * 绑定猫眼响应指令
     */
    public void sendBindResponse(int userId, CommandJson commandJson) {
        sendCommand(userId, commandJson);
    }

    public void sendDoorbellParams() {

    }

    /**
     * 解锁响应指令
     */
    public void sendUnlockResponse(int userId, CommandJson commandJson) {
        sendCommand(userId, commandJson);
    }

//    public void sendChangeCameraResponse(int userId)
    /**
     * 图片请求响应
     */
    public void sendLastedPicsNamesResponse(int userId, String command, int requestNum) {
        File baseFile = new File(FileUtil.getInstance().getDoorbellMediaPath());
        CommandJson commandJson = new CommandJson();
        commandJson.setCommand(command);
        if (!baseFile.exists() || baseFile.list() == null || baseFile.list().length == 0) {
            //找不到文件
            commandJson.setFlag2(0);
        } else {
            List<String> names = new ArrayList<>();
            final List<File> files = new ArrayList<>();
            for (int i = 0; i < baseFile.list().length; i++) {
                File file = new File(baseFile.getAbsolutePath() + File.separator + baseFile.list
                        ()[i]);
                if (CommandJson.CommandType.DOORBELL_IMG_COMMAND.equals(command)) {
                    if (file.isDirectory() || !file.getName().endsWith(".jpg")) {
                        continue;
                    }
                } else {
                    if (file.isDirectory() || !file.getName().endsWith(".mp4")) {
                        continue;
                    }
                }
                files.add(file);
            }
            Timber.e("------->files.size()：" + files.size());
            //按时间排序
            if (files.size() == 0) {
                //找不到文件
                commandJson.setFlag2(0);
            } else {
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.lastModified() < o2.lastModified())
                            return 1;
                        return -1;
                    }
                });
                requestNum = files.size() >= requestNum ? requestNum : files.size();
                for (int i = 0; i < files.size(); i++) {
                    requestNum--;
                    if (requestNum < 0) break;
                    names.add(files.get(i).getName());
                    Timber.e("--------将发送文件：" + files.get(i).getName());
                }
                commandJson.setFlag(mGson.toJson(names));
                commandJson.setFlag2(1);
            }
        }
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_LASTED_IMG_NAMES_RESPONSE);
        sendCommand(userId, commandJson);
    }

    /**
     * 传输图片
     */
    public void sendLastedPics(int userId, String command, List<String> names) {

        FileUtil fileUtil = FileUtil.getInstance();
        if (CommandJson.CommandType.DOORBELL_IMG_COMMAND.equals(command)) {
            for (int i = 0; i < names.size(); i++) {
                File file = new File(fileUtil.getDoorbellMediaPath() + File.separator + names.get
                        (i));
                Timber.e("----------->filePath:" + file.getAbsolutePath());
                if (file.exists()) {
                    Timber.e("--------已发送文件");
                    mAnyChat.TransFile(userId, file.getAbsolutePath(), 0, CommandJson.CommandType
                            .DOORBELL_MEDIA_PIC_PARAM, 0, new AnyChatOutParam());
                }
            }
        } else {
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            for (int i = 0; i < names.size(); i++) {
                if (names.get(i).endsWith(".mp4")) {
                    //视频缩略图请求
                    File thumbnailPath = new File(fileUtil.getDoorbellMediaThumbnailPath());
                    if (!thumbnailPath.exists())
                        thumbnailPath.mkdirs();
                    String filePath = thumbnailPath + File.separator + names.get(i).replace("" +
                            ".mp4", ".jpg");
                    File file = new File(filePath);
                    Timber.e("----------->filePath:" + file.getAbsolutePath() + "--->userid:" +
                            userId);
                    if (file.exists())
                        mAnyChat.TransFile(userId, filePath, 0, CommandJson.CommandType
                                .DOORBELL_MEDIA_THUMBNAIL_PARAM, 0, new AnyChatOutParam());
                    else {
                        media.setDataSource(fileUtil.getDoorbellMediaPath() + File.separator +
                                names.get(i));
                        Bitmap bitmap = media.getFrameAtTime();
                        fileUtil.saveBitmap2File(bitmap, filePath);
                        file = new File(filePath);
                        if (file.exists())
                            mAnyChat.TransFile(userId, filePath, 0, CommandJson.CommandType
                                    .DOORBELL_MEDIA_THUMBNAIL_PARAM, 0, new AnyChatOutParam());
                    }
                }
            }
        }
    }

    /**
     * 发送视频文件
     *
     * @param fileName .mp4
     */
    public void sendLastVideo(int userId, String fileName) {
        File file = new File(FileUtil.getInstance().getDoorbellMediaPath() + File.separator +
                fileName);
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_LASTED_VIDEO_RESPONSE);
        Timber.e("---------->file:" + file.getAbsolutePath() + "-->" + file.exists());
        if (file.exists()) {
            AnyChatOutParam anyChatOutParam = new AnyChatOutParam();
            mAnyChat.TransFile(userId, file.getAbsolutePath(), 0, CommandJson.CommandType
                    .DOORBELL_MEDIA_VIDEO_PARAM, 0, anyChatOutParam);
            commandJson.setFlag2(1);//表示成功
            AnyChatTask anyChatTask = new AnyChatTask();
            anyChatTask.setName(fileName);
            anyChatTask.setTastId(anyChatOutParam.GetIntValue());
            commandJson.setFlag(mGson.toJson(anyChatTask));
        } else {
            commandJson.setFlag2(0);
        }
        sendCommand(userId, commandJson);
    }


    /**
     * 发送命令
     */
    private void sendCommand(int userId, CommandJson commandJson) {
        String json = mGson.toJson(commandJson);
        Timber.e("------------------>send:" + json);

        mAnyChat.TransBuffer(userId, json.getBytes(), json.getBytes().length);
    }
//
//    /**
//     * 旧版请求图片
//     */
//    public void oldImageRequest(int userId) {
//        String action = "action:imageRequest";
//        mAnyChat.TransBuffer(userId, action.getBytes(), action.getBytes().length);//请求图片
//    }
//
//    /**
//     * 新版请求图片（区分了停留或门铃）
//     */
//    public void newImageRequest(int userId) {
//        String action = "{\"notification\":{\"type\":\"imageRequest\", \" flag\":1000}} ";
//        mAnyChat.TransBuffer(userId, action.getBytes(), action.getBytes().length);//请求图片
//    }
//
//    /**
//     * 呼叫请求
//     */
//    public void startChat(int tUserId) {
//        if (mEventType != -1 && mEventType != AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH) {
//            //发起视频通话后未结束
////            if (sessionItem != null) {
//            finishVideoCall(tUserId);
////            }
//        }
////        sessionItem = new SessionItem(0, mSelfUserId, tUserId);
//        //开启视频，发送呼叫事件
//        requestVideoCall(tUserId);
//    }
//
//    /**
//     * 呼叫请求
//     */
//    public void requestVideoCall(int tUserId) {
//        videoCallContrl(AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST, tUserId, 0, 0, 0, "");
//    }

    /**
     * 接受请求
     */
    public void acceptVideoCall(int aId) {
        videoCallContrl(AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY, aId, AnyChatDefine
                .BRAC_ERRORCODE_SUCCESS, 0, 0, "");
    }

    /**
     * 拒绝请求
     */
    public void rejectVideoCall(int aId) {
        videoCallContrl(AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY, aId, AnyChatDefine
                .BRAC_ERRORCODE_SESSION_REFUSE, 0, 0, "");
    }

    /**
     * 发送视频呼叫消息
     *
     * @param users    要通知的用户
     * @param type     通知类型0：门铃 1报警
     * @param filePath
     */
    public void sendVideoCall(List<User> users, int type, String filePath) {
        if (users == null || users.size() == 0) return;
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_NOTIFICATION);
        commandJson.setCommand(type == 1 ? CommandJson.CommandType.NOTIFICATION_DOORBELL_RING :
                CommandJson.CommandType.NOTIFICATION_DOORBELL_ALARM);
        commandJson.setFlag(filePath);
        String json = mGson.toJson(commandJson);
        for (int i = 0; i < users.size(); i++) {
            mAnyChat.TransBuffer(Integer.valueOf(users.get(i).getAid()), json.getBytes(), json
                    .getBytes().length);
            Timber.e("-----------通知报警的用户：" + users.get(i).getAccount());
        }

    }

    /**
     * 发送视频呼叫抓拍图
     *
     * @param aId anychatid
     */
    public void sendVideoCallImg(int aId, String filePath) {
        // TODO: 2018/2/4 判断视频呼叫已打开
        mAnyChat.TransFile(aId, filePath, 0, CommandJson.CommandType.DOORBELL_VIDEO_CALL_PARAM,
                0, new AnyChatOutParam());
    }

    //
//    /**
//     * 结束通话
//     *
//     * @param tUserId
//     */
//    public void finishVideoCall(int tUserId) {
//        videoCallContrl(AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH, tUserId, 0, 0, mSelfUserId,
// "");
//    }
//
//    public void initVideoSDK(Context context) {
//        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION, 1);
//        mAnyChat.mSensorHelper.InitSensor(context);//启动anychat传感器监听
////        mAnyChat.SetRecordSnapShotEvent(context);
//    }
//
//    /**
//     * 切换摄像头
//     */
//    public void changeCamera(int tUserId) {
//        CommandJson.Command command = new CommandJson.Command();
//        command.setType(ConstantUtil.REQUEST_SWITCH_CAMERA);
//        transCommand(tUserId, command);
//    }
//
//    /**
//     * 截图
//     */
//    public void snapShot(int tUserId) {
//        mAnyChat.SnapShot(tUserId, AnyChatDefine.ANYCHAT_RECORD_FLAGS_SNAPSHOT, 0);
//    }
//
//    /**
//     * 录屏
//     */
//    public void startRecord(int tUserId) {
//        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_RECORD_FILETYPE, 0);
//        if (FileUtils.getInstance().getSDCardPath() != null) {
//            AnyChatCoreSDK.SetSDKOptionString(AnyChatDefine.BRAC_SO_RECORD_TMPDIR, FileUtils
// .getInstance().getSDCardPath() + Environment.DIRECTORY_DCIM);
//        }
//        int dwFlags = AnyChatDefine.ANYCHAT_RECORD_FLAGS_VIDEO + AnyChatDefine
// .ANYCHAT_RECORD_FLAGS_AUDIO;
//        mAnyChat.StreamRecordCtrlEx(tUserId, 1, dwFlags, 0, "");
//    }
//
//    /**
//     * 结束录屏
//     */
//    public void stopRecord(int tUserId) {
//        mAnyChat.StreamRecordCtrlEx(tUserId, 0, 0, 0, "");
//    }
//


//
//    /**
//     * 请求图片名称
//     */
//    public void requestLastedPicsNames(int userId) {
////        String command = "{\"command\":{\"type\":\"requestLastedPicsNames\",\"nums\":\"3\"}}";
////        mAnyChat.TransBuffer(userId, command.getBytes(), command.getBytes().length);
////        Timber.e("-------->requestLastedPicsNames" + userId + "--->" + mAnyChat + "--->" +
/// command);
//        CommandJson.Command command = new CommandJson.Command();
//        command.setType(ConstantUtil.REQUEST_LASTED_PICS_NAME);
//        command.setNums("3");
//        transCommand(userId, command);
//    }
//
//    /**
//     * 请求视频名称
//     */
//    public void requestVideoNames(int userId) {
//        CommandJson.Command command = new CommandJson.Command();
//        command.setType(ConstantUtil.REQUEST_VIDEO_NAMES);
//        command.setNums("3");
//        transCommand(userId, command);
//    }
//
//    /**
//     * 请求视频缩略图
//     *
//     * @param fileName 文件名
//     */
//    public void requestVideoThumbnail(int userId, String fileName) {
//        CommandJson.Command command = new CommandJson.Command();
//        command.setType(ConstantUtil.REQUEST_VIDEO_THUMBNAIL);
//        command.setName(fileName);
//        transCommand(userId, command);
//    }
//
//    /**
//     * 请求传输指定文件
//     */
//    public void requestMediaFile(int userId, String fileName) {
//        CommandJson.Command command = new CommandJson.Command();
//        command.setType(ConstantUtil.REQUEST_MEDIA_FILE);
//        command.setName(fileName);
//        transCommand(userId, command);
//    }
//
//    /**
//     * 查询下载进度
//     */
//    public int queryTransTaskInfo(int userId, int taskId, AnyChatOutParam anyChatOutParam) {
//        return mAnyChat.QueryTransTaskInfo(userId, taskId, AnyChatDefine
// .BRAC_TRANSTASK_PROGRESS, anyChatOutParam);
//    }
//
//    /**
//     * 取消任务
//     */
//    public void cancelTransTask(int userId, int taskId) {
//        mAnyChat.CancelTransTask(userId, taskId);
//    }
//
//    private void transCommand(int userId, CommandJson.Command command) {
////        String comm = "{\"command\":{\"type\":\"requestMediaFile\", \"name\":\"" + fileName +
/// "\"}}";
//        mCommandJson.setCommand(command);
//        String comm = mGson.toJson(mCommandJson);
//        mAnyChat.TransBuffer(userId, comm.getBytes(), comm.getBytes().length);
//    }


}
