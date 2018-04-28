package cn.jcyh.peephole.ui.fragment;

import android.database.Cursor;
import android.media.RingtoneManager;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.ChooseSetAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.ui.dialog.ChooseSetDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.utils.FileUtil;
import timber.log.Timber;

/**
 * Created by jogger on 2018/4/28.
 * 设置铃声和音量
 */

public class DoorbellRingVolumeSetFragment extends BaseFragment {
    @BindView(R.id.tv_doorbell_ring)
    TextView tvDoorbellRing;
    @BindView(R.id.tv_alarm_ring)
    TextView tvAlarmRing;
    private DialogHelper mDoorbellRingDialog;
    private DialogHelper mAlarmRingDialog;
    private List<String> mDoorbellRings;
    private List<String> mAlarmRings;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_doorbell_ring_volume_set;
    }

    @Override
    public void init() {
        mDoorbellRings = new ArrayList<>();
        mAlarmRings = new ArrayList<>();
        List<String> ringNames = new ArrayList<>();
        List<Integer> ringIds = new ArrayList<>();
        RingtoneManager ringtoneManager = new RingtoneManager(mActivity);
        Cursor cursor = ringtoneManager.getCursor();
        String sdCardPath = FileUtil.getInstance().getSDCardPath();
        File file = new File(sdCardPath);
        searchFile(file);
        while (cursor != null && cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex("title"));
            if (title.startsWith("A") || title.startsWith("B")) {
                ringNames.add(title);
                ringIds.add(cursor.getInt(cursor.getColumnIndex("_id")));
                mDoorbellRings.add(title);
            } else if (title.startsWith("C") || title.startsWith("D")) {
                mAlarmRings.add(title);
            }
        }
        if (cursor != null)
            cursor.close();
        Timber.e("-----ring:" + ringNames + "\n" + "---" + ringIds);
    }

    private void searchFile(File file) {
        if (file.isDirectory() && file.list() != null) {
            for (int i = 0; i < file.list().length; i++) {
                Timber.e("--------file:" + file.list()[i]);
                File file2 = new File(file.getAbsolutePath() + File.separator + file.list()[i]);
                searchFile(file2);
            }
        }
    }

    @OnClick({R.id.rl_volume, R.id.rl_doorbell_ring, R.id.rl_alarm_ring})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_volume:
                break;
            case R.id.rl_doorbell_ring:
                if (mDoorbellRingDialog == null) {
                    ChooseSetDialog chooseSetDialog = new ChooseSetDialog();
                    chooseSetDialog.setTitle(getString(R.string.doorbell_ring));
                    ChooseSetAdapter adapter = new ChooseSetAdapter();
                    adapter.loadData(mDoorbellRings);
                    chooseSetDialog.setAdapter(adapter);
                    mDoorbellRingDialog = new DialogHelper((BaseActivity) mActivity, chooseSetDialog);
                }
                mDoorbellRingDialog.commit();
                break;
            case R.id.rl_alarm_ring:
                if (mAlarmRingDialog == null) {
                    ChooseSetDialog chooseSetDialog = new ChooseSetDialog();
                    chooseSetDialog.setTitle(getString(R.string.alarm_ring));
                    ChooseSetAdapter adapter = new ChooseSetAdapter();
                    adapter.loadData(mAlarmRings);
                    chooseSetDialog.setAdapter(adapter);
                    mAlarmRingDialog = new DialogHelper((BaseActivity) mActivity, chooseSetDialog);
                }
                mAlarmRingDialog.commit();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAlarmRingDialog != null)
            mAlarmRingDialog.dismiss();
        if (mDoorbellRingDialog != null)
            mDoorbellRingDialog.dismiss();
    }
}
