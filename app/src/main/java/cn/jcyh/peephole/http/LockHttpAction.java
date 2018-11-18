package cn.jcyh.peephole.http;

import cn.jcyh.eaglelock.entity.UnLockData;
import cn.jcyh.peephole.entity.RequestUploadElectricQuantity;

/**
 * Created by jogger on 2018/1/10.
 */

public class LockHttpAction extends BaseHttpAction {
    private static LockHttpAction sHttpAction;

    @Override
    IHttpRequest getHttpRequest() {
        return new HttpRequest();
    }

    private LockHttpAction() {
    }

    public static LockHttpAction getHttpAction() {
        if (sHttpAction == null) {
            synchronized (LockHttpAction.class) {
                if (sHttpAction == null) {
                    sHttpAction = new LockHttpAction();
                }
            }
        }
        return sHttpAction;
    }

    /**
     * 上传锁电量
     */
    public void lockUpdateElectricQuantity(RequestUploadElectricQuantity requestUploadElectricQuantity, IDataListener listener) {
        mHttpRequest.lockUpdateElectricQuantity(requestUploadElectricQuantity, listener);
    }

    /**
     * 获取解锁钥匙数据
     */
    public void getUnLockKeyData(String sn, IDataListener<UnLockData> listener) {
        mHttpRequest.getUnLockKeyData(sn, listener);
    }

    /**
     * 上传开锁日志
     */
    public void lockUploadLog(int lockId, String accessToken, String records,  IDataListener listener) {
        mHttpRequest.lockUploadLog(lockId,accessToken,records,listener);
    }
}

