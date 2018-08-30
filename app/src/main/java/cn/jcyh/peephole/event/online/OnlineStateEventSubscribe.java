package cn.jcyh.peephole.event.online;

import android.os.Handler;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.event.EventSubscribeService;
import com.netease.nimlib.sdk.event.model.EventSubscribeRequest;
import com.netease.nimlib.sdk.event.model.NimOnlineStateEvent;
import com.netease.nimlib.sdk.friend.FriendService;

import java.util.List;

import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.SPUtil;
import cn.jcyh.peephole.utils.Util;

/**
 * Created by chenkang on 2017/4/26.
 */

public class OnlineStateEventSubscribe {

    // 订阅好友并同步当前在线状态的频率控制30 s，对同一账号连续2次订阅时间间隔在30s 以上
    private static final long SUBS_FREQ = 30 * 1000;

    private static long lastSubsTime = -1;

    private static boolean initSubsFinished = true;

    private static boolean waitInitSubs = false;
    // 订阅有效期 1天，单位秒
    public static final long SUBSCRIBE_EXPIRY = 60 * 60 * 24;

    public static void initSubscribes() {
        // 正在进行
        if (waitInitSubs || !initSubsFinished) {
            return;
        }

        final long timeInterval = getSubsTimeInterval();
        if (timeInterval <= SUBS_FREQ) {
            waitInitSubs = true;
            long delay = SUBS_FREQ - timeInterval + 1000;
            Handler handler = Handlers.sharedHandler(Util.getApp());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 延迟订阅
                    waitInitSubs = false;
                    initSubscribes();
                }
            }, delay);
            return;
        }

        initSubsFinished = false;

        // 重置事件、订阅关系缓存
        OnlineStateEventCache.resetCache();

        // 重置订阅有效期管理
        SubscribeExpiryManager.reset();

        // 订阅好友、最近联系人中非好友的在线状态事件
        subscribeAllOnlineStateEvent();
    }


    private static long getSubsTimeInterval() {
        if (lastSubsTime < 0) {
            lastSubsTime = SPUtil.getInstance().getLong(Constant.KEY_SUBSCRIBE_TIME, 0);
        }
        return System.currentTimeMillis() - lastSubsTime;
    }

    private static void updateLastSubsTime() {
        lastSubsTime = System.currentTimeMillis();
        SPUtil.getInstance().put(Constant.KEY_SUBSCRIBE_TIME, lastSubsTime);
    }

    /**
     * 订阅好友、最近联系人的在线状态事件
     */
    public static void subscribeAllOnlineStateEvent() {
        final List<String> accounts = NIMClient.getService(FriendService.class).getFriendAccounts();
        initSubsFinished = true;
        subscribeOnlineStateEvent(accounts, SUBSCRIBE_EXPIRY);
    }


    /**
     * 订阅指定账号的在线状态事件
     *
     * @param accounts 目标账号
     */
    public static void subscribeOnlineStateEvent(final List<String> accounts, long expiry) {
        if (waitInitSubs || !initSubsFinished || accounts == null || accounts.isEmpty()) {
            return;
        }
        EventSubscribeRequest eventSubscribeRequest = new EventSubscribeRequest();
        eventSubscribeRequest.setEventType(NimOnlineStateEvent.EVENT_TYPE);
        eventSubscribeRequest.setPublishers(accounts);
        eventSubscribeRequest.setExpiry(expiry);
        eventSubscribeRequest.setSyncCurrentValue(true);
        OnlineStateEventCache.addSubsAccounts(accounts);
        updateLastSubsTime();
        NIMClient.getService(EventSubscribeService.class).subscribeEvent(eventSubscribeRequest).setCallback(new RequestCallbackWrapper<List<String>>() {
            @Override
            public void onResult(int code, List<String> result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    // 可能网络比较慢，所以再更新一把时间
                    updateLastSubsTime();
                    SubscribeExpiryManager.subscribeSuccess();
                    if (result != null) {
                        // 部分订阅失败的账号。。。
                        OnlineStateEventCache.removeSubsAccounts(result);
                    }
                } else {
                    L.e("-------------------订阅失败" + code);
                    OnlineStateEventCache.removeSubsAccounts(accounts);
                }
            }
        });
    }


    /**
     * 取消订阅指定账号的在线状态事件
     *
     * @param accounts 目标账号
     */
    public static void unSubscribeOnlineStateEvent(List<String> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        L.e("-----------------unSubscribe OnlineStateEvent " + accounts);

        OnlineStateEventCache.removeSubsAccounts(accounts);
        OnlineStateEventCache.removeOnlineState(accounts);

        EventSubscribeRequest eventSubscribeRequest = new EventSubscribeRequest();
        eventSubscribeRequest.setEventType(NimOnlineStateEvent.EVENT_TYPE);
        eventSubscribeRequest.setPublishers(accounts);

        NIMClient.getService(EventSubscribeService.class).unSubscribeEvent(eventSubscribeRequest);
    }

    /**
     * 订阅有效期管理，快到期时重新订阅
     */
    private static class SubscribeExpiryManager {

        private static boolean firstSubs = true;

        public static void reset() {
            Handler handler = Handlers.sharedHandler(Util.getApp());
            handler.removeCallbacks(runnable);
            firstSubs = true;
        }

        private static Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 如果不是好友并且不在最近联系人里面，则不会续订
                L.e("-----------------time task subscribe again");
                initSubscribes();
            }
        };

        private static void startTimeTask() {
            L.e("-----------------time task start");
            Handler handler = Handlers.sharedHandler(Util.getApp());
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, SUBSCRIBE_EXPIRY * 1000);
        }

        public static void subscribeSuccess() {
            if (firstSubs) {
                firstSubs = false;
                startTimeTask();
            }
        }
    }
}