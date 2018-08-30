package cn.jcyh.peephole.ui.activity;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.zxing.encoding.EncodingHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.command.CommandControl;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.CommandJson;
import cn.jcyh.peephole.event.NIMMessageAction;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.HintDialogFragmemt;

import static cn.jcyh.peephole.control.ControlCenter.sIsBinding;

public class BindActivity extends BaseActivity {
    @BindView(R.id.iv_icon)
    ImageView ivIcon;
    @BindView(R.id.tv_imei)
    TextView tvImei;
    @BindView(R.id.tv_count_down)
    TextView tvCountDown;
    private DialogHelper mDialogHelper;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mCount = 30;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind;
    }

    @Override
    protected void init() {
        sIsBinding = true;
        tvImei.setText(String.format(getString(R.string.device_no_), IMEI));
        decordQR();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mCount--;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCount <= 0) {
                            finish();
                        }
                        if (isFinishing() || getSupportFragmentManager() == null) return;
                        tvCountDown.setText(String.format(getString(R.string
                                .close_window_hint_format), mCount));
                    }
                });
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sIsBinding = false;
        mTimer.cancel();
        mTimer = null;
        mTimerTask.cancel();
        mTimerTask = null;
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    /**
     * 生成二维码
     */
    private void decordQR() {
        Bitmap bitmap = null;
        try {
            bitmap = EncodingHandler.createQRCode(IMEI, 500);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        ivIcon.setImageBitmap(bitmap);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNIMMessageAction(NIMMessageAction nimMessageAction) {
        CommandJson commandJson = nimMessageAction.getParcelableExtra(Constant.COMMAND);
        String fromAccount = nimMessageAction.getStringExtra(Constant.FROM_ACCOUNT);
        if (CommandJson.CommandType.BIND_DOORBELL_REQUEST.equals(commandJson.getCommandType())) {
            responseBindRequest(fromAccount, commandJson);//响应绑定请求
        }
    }

    /**
     * 处理绑定请求
     */
    private void responseBindRequest(final String fromAccount, final CommandJson commandJson) {
        //先判断是否已绑定
        boolean binded = ControlCenter.getUserManager().isBinded(fromAccount);
        if (binded) {
            CommandControl.sendBindResponse(fromAccount, CommandJson.CommandType.BIND_RESPONSE_FLAG_BINDED);
            return;
        }
        if (mDialogHelper != null && mDialogHelper.isShowing()) {//先取消之前的弹窗
            mDialogHelper.dismiss();
            mDialogHelper = null;
        }
        HintDialogFragmemt hintDialogFragmemt = new HintDialogFragmemt();
        hintDialogFragmemt.setHintContent(String.format(getString(R.string
                        .request_bind_hint)
                , commandJson.getFlag()));
        hintDialogFragmemt.setOnHintDialogListener(new HintDialogFragmemt
                .OnHintDialogListener() {
            @Override
            public void onConfirm(boolean isConfirm) {
                if (isConfirm) {
                    CommandControl.sendBindResponse(fromAccount, CommandJson.CommandType.BIND_RESPONSE_FLAG_RECEIVED);
                } else {
                    CommandControl.sendBindResponse(fromAccount, CommandJson.CommandType.BIND_RESPONSE_FLAG_REJECT);
                }
                mDialogHelper.dismiss();
            }
        });
        mDialogHelper = new DialogHelper(this, hintDialogFragmemt);
        //未绑定
        mDialogHelper.commit();
    }
}
