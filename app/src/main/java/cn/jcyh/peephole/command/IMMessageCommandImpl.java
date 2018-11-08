package cn.jcyh.peephole.command;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.CommandJson;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.GsonUtil;
import cn.jcyh.peephole.utils.L;

/**
 * Created by jogger on 2018/8/1.
 */
public class IMMessageCommandImpl {
    public static final String DOORBELL_PARAMS_TYPE_MODE = "mode";
    public static final String DOORBELL_PARAMS_TYPE_SENSOR = "sensor";
    public static final String DOORBELL_PARAMS_TYPE_FACE_VALI = "face";

    public static void sendDoorbellParamsGetResponse(String account) {
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_PARAMS_GET_RESPONSE);
        if (!ControlCenter.getUserManager().isBinded(account))
            commandJson.setFlag2(0);
        else {
            commandJson.setFlag2(1);
            commandJson.setFlag(DoorbellConfig.getGson().toJson(ControlCenter.getDoorbellManager().getDoorbellConfig()));
        }
        sendTextCommand(account, commandJson);
    }

    public static void sendDoorbellModeParamsCommand(String account, int isSuccess) {
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_PARAMS_RESPONSE);
        commandJson.setCommand(DOORBELL_PARAMS_TYPE_MODE);
        commandJson.setFlag2(isSuccess);
        sendTextCommand(account, commandJson);
    }

    public static void sendDoorbellSensorParamsCommand(String account, int isSuccess) {
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_PARAMS_RESPONSE);
        commandJson.setCommand(DOORBELL_PARAMS_TYPE_SENSOR);
        commandJson.setFlag2(isSuccess);
        sendTextCommand(account, commandJson);
    }

    public static void sendDoorbellFaceValiParamsResponse(String account, int isSuccess) {
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_PARAMS_RESPONSE);
        commandJson.setCommand(DOORBELL_PARAMS_TYPE_FACE_VALI);
        commandJson.setFlag2(isSuccess);
        sendTextCommand(account, commandJson);
    }

    public static void sendBindResponse(String fromAccount, String imei, String flag) {
        CommandJson commandJson = new CommandJson();
        commandJson.setCommand(imei);
        commandJson.setFlag(flag);
        commandJson.setCommandType(CommandJson.CommandType.BIND_DOORBELL_RESPONSE);
        sendTextCommand(fromAccount, commandJson);
    }

    public static void sendUnlockResponse(String fromAccount) {
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(CommandJson.CommandType.UNLOCK_DOORBELL_RESPONSE);
        sendTextCommand(fromAccount, commandJson);
    }

    /**
     * 图片请求响应
     */
    public static void sendLastedPicsNamesResponse(String account, String command, int requestNum) {
        File baseFile = new File(FileUtil.getDoorbellImgPath());
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
                }
                commandJson.setFlag(GsonUtil.toJson(names));
                commandJson.setFlag2(1);
            }
        }
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_LASTED_IMG_NAMES_RESPONSE);
        sendTextCommand(account, commandJson);
    }

    /**
     * 传输图片
     */
    public static void sendLastedPics(String account, String command, List<String> names) {
        if (CommandJson.CommandType.DOORBELL_IMG_COMMAND.equals(command)) {
            for (int i = 0; i < names.size(); i++) {
                File file = new File(FileUtil.getDoorbellImgPath() + File.separator + names.get
                        (i));
                if (file.exists()) {
                    sendImageCommand(account, command, file);
                }
            }
        } else {
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            for (int i = 0; i < names.size(); i++) {
                if (names.get(i).endsWith(".mp4")) {
                    //视频缩略图请求
                    File thumbnailPath = new File(FileUtil.getDoorbellMediaThumbnailPath());
                    if (!thumbnailPath.exists())
                        thumbnailPath.mkdirs();
                    String filePath = thumbnailPath + File.separator + names.get(i).replace("" +
                            ".mp4", ".jpg");
                    File file = new File(filePath);
                    if (file.exists()) {
                        sendImageCommand(account, command, file);
                    } else {
                        media.setDataSource(FileUtil.getDoorbellImgPath() + File.separator +
                                names.get(i));
                        Bitmap bitmap = media.getFrameAtTime();
                        FileUtil.saveBitmap2File(bitmap, filePath);
                        file = new File(filePath);
                        if (file.exists()) {
                            sendImageCommand(account, command, file);
                        }
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
    public static void sendLastVideo(String account, String fileName) {
        File file = new File(FileUtil.getDoorbellImgPath() + File.separator +
                fileName);
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(CommandJson.CommandType.DOORBELL_LASTED_VIDEO_RESPONSE);
        L.e("---------->file:" + file.getAbsolutePath() + "-->" + file.exists());
        if (file.exists()) {
//            mAnyChat.TransFile(userId, file.getAbsolutePath(), 0, CommandJson.CommandType
//                    .DOORBELL_MEDIA_VIDEO_PARAM, 0, anyChatOutParam);
//            commandJson.setFlag2(1);//表示成功
//            AnyChatTask anyChatTask = new AnyChatTask();
//            anyChatTask.setName(fileName);
//            anyChatTask.setTastId(anyChatOutParam.GetIntValue());
//            commandJson.setFlag(GsonUtil.toJson(anyChatTask));
        } else {
            commandJson.setFlag2(0);
        }
//        sendTextCommand(userId, commandJson);
    }

    private static void sendTextCommand(String account, CommandJson command) {
        SessionTypeEnum sessionType = SessionTypeEnum.P2P;
        IMMessage messageTxt = MessageBuilder.createTextMessage(account, sessionType, GsonUtil.toJson(command));
        configMessage(messageTxt);
        NIMClient.getService(MsgService.class).sendMessage(messageTxt, true);
    }

    private static void sendImageCommand(String account, String command, File file) {
        SessionTypeEnum sessionType = SessionTypeEnum.P2P;
        CommandJson commandJson = new CommandJson();
        commandJson.setCommandType(command);
        IMMessage messageImg = MessageBuilder.createImageMessage(account, sessionType, file, file.getName());
        messageImg.setContent(GsonUtil.toJson(commandJson));
        configMessage(messageImg);
        NIMClient.getService(MsgService.class).sendMessage(messageImg, true);
    }


    private static void configMessage(IMMessage message) {
        CustomMessageConfig config = message.getConfig();
        if (config == null)
            config = new CustomMessageConfig();
        config.enableRoaming = false;//是否漫游
        config.enableUnreadCount = false;//是否记录未读
        config.enablePersist = false;//是否存离线
        config.enableHistory = false;// 该消息不保存到服务器
        // 该消息不同步
        config.enableSelfSync = false;
        message.setConfig(config);
    }

}
