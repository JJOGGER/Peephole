package cn.jcyh.peephole.adapter.callback;

/**
 * Created by jogger on 2018/8/18.
 */
public interface OnSystemSettingListener {
    void onWLANClick();

    void onBluetoothClick();

    void onFlowUseClick();

    void onMoreClick();

    void onShowClick();

    void onBatteryClick();

    void onStorageClick();

    void onAppClick();

    void onLanguageClick();

//    void onMarkResetClick();

    void onDateTimeClick();
}
