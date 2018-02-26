package cn.jcyh.peephole.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.zxing.WriterException;
import com.zxing.encoding.EncodingHandler;

import java.util.List;

import butterknife.BindView;
import cn.jcyh.peephole.MyApp;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.CommandJson;
import cn.jcyh.peephole.bean.User;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.HintDialogFragmemt;
import cn.jcyh.peephole.utils.CameraProvider;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.ToastUtil;
import timber.log.Timber;

import static cn.jcyh.peephole.control.DoorBellControlCenter.sIsBinding;

public class BindActivity extends BaseActivity {
    @BindView(R.id.iv_icon)
    ImageView ivIcon;
    private DoorBellControlCenter mControlCenter;
    private MyReceiver mReceiver;
    private DialogHelper mDialogHelper;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind;
    }

    @Override
    protected void init() {
        sIsBinding = true;
        decordQR();
        mReceiver = new MyReceiver();
        mControlCenter = DoorBellControlCenter.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intentFilter.addAction(ConstantUtil.ACTION_ANYCHAT_USER_INFO_EVENT);
        registerReceiver(mReceiver, intentFilter);
        mControlCenter.enterRoom(1, "");
        HintDialogFragmemt hintDialogFragmemt = new HintDialogFragmemt();
        mDialogHelper = new DialogHelper(this, hintDialogFragmemt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mControlCenter.leaveRoom(1);
        sIsBinding = false;
        unregisterReceiver(mReceiver);
    }

    private void decordQR() {
//        Bitmap logo = BitmapFactory.decodeResource(getResources(), 0);
        /**
         * 第一个参数：需要转换成二维码的内容
         * 第二个参数：二维码的宽度
         * 第三个参数：二维码的高度
         * 第四个参数：Logo图片
         */
        Bitmap bitmap = null;
        try {
            bitmap = EncodingHandler.createQRCode(MyApp.sImei, 500);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        ivIcon.setImageBitmap(bitmap);
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT.equals(intent.getAction())) {
                String type = intent.getStringExtra("type");
                final int dwUserid = intent.getIntExtra("dwUserid", -1);
                if (ConstantUtil.TYPE_ANYCHAT_TRANS_BUFFER.equals(type)) {
                    final CommandJson commandJson = intent.getParcelableExtra("command");
                    Timber.e("---------com:"+commandJson);
                    switch (commandJson.getCommandType()) {
                        case CommandJson.CommandType.BIND_DOORBELL_REQUEST:
                            if (commandJson.getCommand().equals(MyApp.sImei)) {
                                if (mDialogHelper.isShowing()) {
                                    return;
                                }
                                responseBindRequest(dwUserid, commandJson);
                            }
                            break;
                        case CommandJson.CommandType.BIND_DOORBELL_COMPLETED:
                            //绑定猫眼成功
                            Timber.e("-------------绑定猫眼成功，刷新列表");
                            // TODO: 2018/2/26  绑定猫眼成功，刷新列表
                            ToastUtil.showToast(getApplicationContext(), R.string.bind_succ);
                            finish();
                            break;
                    }
//                    if (result.equals(mDeviceNo)) {
//                        dwTargetUserId = dwUserid;
//                        //开始绑定设备
//                        startBind();
//                    } else if ("13".equals(result)) {
//                        //拒绝绑定
//                        if (mProgressDialog != null && mProgressDialog.isShowing())
//                            mProgressDialog.dismiss();
//                        ToastUtil.showToast(getApplicationContext(), getString(R.string.reject_bind));
//                    } else if ("14".equals(result)) {
//                        //设备已经绑定
//                        dwTargetUserId = dwUserid;
//                    }
                }
            } else if (ConstantUtil.ACTION_ANYCHAT_USER_INFO_EVENT.equals(intent.getAction())) {
                String type = intent.getStringExtra("type");
                if (ConstantUtil.TYPE_ANYCHAT_USER_AT_ROOM.equals(type)) {
                }
            }
        }
    }

    /**
     * 处理绑定请求
     */
    private void responseBindRequest(final int dwUserid, final CommandJson commandJson) {
        //先判断是否已绑定
        HttpAction.getHttpAction(getApplicationContext()).getBindUsers(MyApp.sImei, new IDataListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users != null && users.size() != 0) {
                    for (User user :
                            users) {
                        if (user.getAid() == dwUserid) {
                            //已绑定
                            commandJson.setFlag("2");
                            commandJson.setCommandType(CommandJson.CommandType.BIND_DOORBELL_RESPONSE);
                            mControlCenter.sendBindResponse(dwUserid, commandJson);
                            return;
                        }
                    }
                }
                Timber.e("-------responseBindRequest");
                //未绑定
                HintDialogFragmemt dialogFragment = (HintDialogFragmemt) mDialogHelper.getDialogFragment();
                dialogFragment.setHintContent(String.format(getString(R.string.request_bind_hint), commandJson.getFlag()));
                dialogFragment.setOnHintDialogListener(new HintDialogFragmemt.OnHintDialogListener() {
                    @Override
                    public void onConfirm(boolean isConfirm) {
                        if (isConfirm) {
                            commandJson.setFlag("1");
                            int flag2 = CameraProvider.hasFrontFacingCamera() && CameraProvider.hasBackFacingCamera() ? 1 : 0;
                            commandJson.setCommandType(CommandJson.CommandType.BIND_DOORBELL_RESPONSE);
                            commandJson.setFlag2(flag2);
                            mControlCenter.sendBindResponse(dwUserid, commandJson);
                        } else {
                            commandJson.setFlag("0");
                            commandJson.setCommandType(CommandJson.CommandType.BIND_DOORBELL_RESPONSE);
                            mControlCenter.sendBindResponse(dwUserid, commandJson);
                        }
                        mDialogHelper.dismiss();
                    }
                });
                mDialogHelper.commit();
            }

            @Override
            public void onFailure(int errorCode) {
                //请求失败
                commandJson.setFlag("4");
                commandJson.setCommandType(CommandJson.CommandType.BIND_DOORBELL_RESPONSE);
                mControlCenter.sendBindResponse(dwUserid, commandJson);
            }
        });
    }
}
