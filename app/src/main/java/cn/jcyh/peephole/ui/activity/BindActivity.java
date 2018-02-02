package cn.jcyh.peephole.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.widget.ImageView;


import com.google.zxing.WriterException;
import com.zxing.encoding.EncodingHandler;

import butterknife.BindView;
import cn.jcyh.peephole.MyApp;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.CommandJson;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.ui.dialog.DialogFactory;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.utils.CameraProvider;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.ToastUtil;
import timber.log.Timber;

public class BindActivity extends BaseActivity {
    @BindView(R.id.iv_icon)
    ImageView ivIcon;
    private DoorBellControlCenter mControlCenter;
    private MyReceiver mReceiver;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind;
    }

    @Override
    protected void init() {
        decordQR();
        mReceiver = new MyReceiver();
        mControlCenter = DoorBellControlCenter.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intentFilter.addAction(ConstantUtil.ACTION_ANYCHAT_USER_INFO_EVENT);
        registerReceiver(mReceiver, intentFilter);
        mControlCenter.enterRoom(1, "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mControlCenter.leaveRoom(1);
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
                    switch (commandJson.getCommandType()) {
                        case CommandJson.CommandType.BIND_DOORBELL_REQUEST:
                            if (commandJson.getCommand().equals(MyApp.sImei)) {
                                DialogFactory.getDialogFactory().create(BindActivity.this, String.format(getString(R.string.request_bind_hint), commandJson.getFlag()), DialogFactory.DIALOG_COMMON_HINT)
                                        .setOnDialogListener(new OnDialogListener() {
                                            @Override
                                            public void onConfirm(Object isConfirm) {
                                                DialogFactory.getDialogFactory().dismiss();
                                                if ((Boolean) isConfirm) {
                                                    commandJson.setFlag("1");
                                                } else {
                                                    commandJson.setFlag("0");
                                                }
                                                int flag2 = CameraProvider.hasFrontFacingCamera() && CameraProvider.hasBackFacingCamera() ? 1 : 0;
                                                commandJson.setCommandType(CommandJson.CommandType.BIND_DOORBELL_RESPONSE);
                                                commandJson.setFlag2(flag2);
                                                mControlCenter.sendBindResponse(dwUserid, commandJson);
                                            }
                                        }).commit();
                            }
                            break;
                        case CommandJson.CommandType.BIND_DOORBELL_COMPLETED:
                            //绑定猫眼成功
                            Timber.e("-------------绑定猫眼成功，刷新列表");
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
}
