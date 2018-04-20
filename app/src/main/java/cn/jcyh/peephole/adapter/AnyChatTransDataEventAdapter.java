package cn.jcyh.peephole.adapter;

import android.content.Context;
import android.content.Intent;

import com.bairuitech.anychat.AnyChatTransDataEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import cn.jcyh.peephole.bean.CommandJson;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.utils.ConstantUtil;
import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_TRANS_BUFFER;

/**
 * Created by jogger on 2018/3/7.
 */

public class AnyChatTransDataEventAdapter implements AnyChatTransDataEvent {
    private Context mContext;
    private DoorBellControlCenter mControlCenter;
    private Gson mGson;

    public AnyChatTransDataEventAdapter(Context context) {
        mContext = context;
        mControlCenter = DoorBellControlCenter.getInstance(mContext);
        mGson = new Gson();
    }

    @Override
    public void OnAnyChatTransFile(int dwUserid, String FileName, String TempFilePath, int dwFileLength, int wParam, int lParam, int dwTaskId) {
        Timber.e("---------OnAnyChatTransFile" + lParam + "-->" + FileName + "--" + TempFilePath);
//        String targetPath = null;
//        String mediaImgSrc = FileUtils.getInstance().getMediaImgSrc();
//        switch (lParam) {
//            case 3:
//                //视频呼叫文件传输
//                targetPath = FileUtils.getInstance().getDoorBellRecordFileSrc(FileName);
//                break;
//            case 10:
//                //传输照片文件
//                if (mediaImgSrc != null)
//                    targetPath = FileUtils.getInstance().getMediaImgSrc()
//                            + File.separator + dwUserid + File.separator + FileName;
//                break;
//            case 11:
//                //传输视频文件
//                break;
//            case 12:
//                //传输视频缩略图文件
//                if (mediaImgSrc != null)
//                    targetPath = FileUtils.getInstance().getMediaVideoSrc()
//                            + File.separator + dwUserid + File.separator + FileName;
//                break;
//        }
//        Timber.e("----TempFilePath:" + TempFilePath + "---targetPath:" + targetPath);
//        if (targetPath != null) {
//            boolean b = FileUtils.getInstance().moveFile(TempFilePath, targetPath);
//            if (lParam == 3) {
//                sTargetPath = targetPath;
//            }
//            if (b) {
        Intent intent = new Intent();
        intent.putExtra("dwUserid", dwUserid);
        intent.putExtra("targetPath", TempFilePath);
        intent.putExtra("dwFileLength", dwFileLength);
        intent.putExtra("wParam", wParam);
        intent.putExtra("lParam", lParam);
        intent.putExtra("dwTaskId", dwTaskId);
        intent.setAction(ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intent.putExtra("type", ConstantUtil.TYPE_ANYCHAT_TRANS_FILE);
        mContext.sendBroadcast(intent);
//            }
//        }
    }

    @Override
    public void OnAnyChatTransBuffer(int dwUserid, byte[] lpBuf, int dwLen) {
        String result = new String(lpBuf, 0, lpBuf.length);
        Intent intent = new Intent();
        intent.putExtra("dwUserid", dwUserid);
        intent.putExtra("result", result);
        intent.setAction(ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_TRANS_BUFFER);
        CommandJson commandJson = mGson.fromJson(result, CommandJson.class);
        intent.putExtra("command", commandJson);
        Timber.e("-----OnAnyChatTransBuffer" + result + "---dwUserid:" + dwUserid);
        mContext.sendBroadcast(intent);
        switch (commandJson.getCommandType()) {
            case CommandJson.CommandType.DOORBELL_LASTED_IMG_NAMES_REQUEST:
                //收到近期图片获取请求
                int requestNum = commandJson.getFlag2();//获取请求数
                //从文件中获取响应数量的图片
                mControlCenter.sendLastedPicsNamesResponse(dwUserid, commandJson.getCommand(), requestNum);
                break;
            case CommandJson.CommandType.DOORBELL_LASTED_IMG_REQUEST:
                //发送图片
                String namesJson = commandJson.getFlag();
                List<String> names = mGson.fromJson(namesJson, new TypeToken<List<String>>() {
                }.getType());
                Timber.e("---------->>commandjson:"+commandJson);
                mControlCenter.sendLastedPics(dwUserid, commandJson.getCommand(), names);
                break;
            case CommandJson.CommandType.DOORBELL_LASTED_VIDEO_REQUEST:
                //发送视频文件
                String fileName = commandJson.getFlag();
                mControlCenter.sendLastVideo(dwUserid, fileName);
                break;
        }
//        if (result.contains("command")) {
//            try {
//                JSONObject jsonObject_all = new JSONObject(result);
//                JSONObject jsonObject_command = jsonObject_all.getJSONObject("command");
//                String type = jsonObject_command.getString("type");
//                switch (type) {
//                    case CHANGE_CAMERA:
//                        intent.putExtra("type2", CHANGE_CAMERA);
//                        String status = jsonObject_command.getString("status");
//                        if ("success".equals(status))
//                            ToastUtil.showToast(getApplicationContext(), R.string.change_succ);
//                        break;
//                    case LASTED_PICS_NAMES:
//                        intent.putExtra("type2", LASTED_PICS_NAMES);
//                        break;
//                    case MEDIA_FILE:
//                        intent.putExtra("type2", MEDIA_FILE);
//                        break;
//                    case VIDEO_NAMES:
//                        intent.putExtra("type2", VIDEO_NAMES);
//                        break;
//                    case VIDEO_THUNBNAIL:
//                        intent.putExtra("type2", VIDEO_THUNBNAIL);
//                        break;
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        sendBroadcast(intent);
//        if ("action:doorbell".equals(result) || result.contains("notification")) {
//            DoorBellBean doorBell = DoorBellControlCenter.getInstance(getApplicationContext())
// .getUserItemByUserId(dwUserid);
//            Timber.e("------->doorbell" + doorBell);
//            if (doorBell != null) {
//                Bundle bundle = new Bundle();
//                if (result.contains("notification")) {
//                    try {
//                        JSONObject jsonObject = new JSONObject(result);
//                        JSONObject jsonObject_Notification = jsonObject.getJSONObject
// ("notification");
//                        String type = jsonObject_Notification.getString("type");
//                        String trigger = jsonObject_Notification.getString(" trigger");
//                        Timber.e("----type:" + type + "---tri:" + trigger);
//                        if ("videoCall".equals(type)) {
//                            bundle.putString("trigger", jsonObject_Notification.getString("
// trigger"));
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                //存在一种情况，app解绑但设备未解绑仍收得到此信息
//                bundle.putSerializable("doorBell", doorBell);
//                //发送请求图片的指令
////            String action = "action:imageRequest";
////            mAnyChat.TransBuffer(dwUserid, action.getBytes(), action.getBytes().length);
//                intent = new Intent(KeepBackLocalService.this, CallActivity.class);
//                intent.putExtras(bundle);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
////                intent = new Intent(KeepBackLocalService.this, CallActivity.class);
////                intent.putExtras(bundle);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                ActivityCollector.finishActivity(AddDoorBellActivity.class);
////                startActivity(intent);
//            }
//        }
    }

    @Override
    public void OnAnyChatTransBufferEx(int dwUserid, byte[] lpBuf, int dwLen, int wparam, int lparam, int taskid) {
        Timber.e("-----------OnAnyChatTransBufferEx");
    }

    @Override
    public void OnAnyChatSDKFilterData(byte[] lpBuf, int dwLen) {
        Timber.e("-----------OnAnyChatSDKFilterData");
    }
}
