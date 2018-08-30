package cn.jcyh.peephole.event.online;

import android.content.Context;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.event.EventSubscribeService;
import com.netease.nimlib.sdk.event.EventSubscribeServiceObserver;
import com.netease.nimlib.sdk.event.model.Event;
import com.netease.nimlib.sdk.event.model.NimOnlineStateEvent;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.NetworkUtil;
import cn.jcyh.peephole.utils.Util;

/**
 * 在线状态事件管理
 */

public class OnlineStateEventManager {

    /**
     * 发布事件有效期7天
     */
    private static final long EVENT_EXPIRY = 60 * 60 * 24 * 7;

    private static final String NET_TYPE_2G = "2G";
    private static final String NET_TYPE_3G = "3G";
    private static final String NET_TYPE_4G = "4G";
    private static final String NET_TYPE_WIFI = "WiFi";
    private static final String UNKNOWN = "未知";

    // 已发布的网络状态
    private static int pubNetState = -1;

    private static boolean enable = false;
    // 在线状态变化监听
    private static OnlineStateChangeObservable onlineStateChangeObservable;
    // contact 变化监听
    private static ContactChangedObservable contactChangedObservable;

    public static void init() {
        if (!enableOnlineStateEvent()) {
            return;
        }
        registerEventObserver(true);
        registerOnlineStatusObserver();
        getContactChangedObservable().registerObserver(observer, true);
    }

    public static OnlineStateChangeObservable getOnlineStateChangeObservable() {
        if (onlineStateChangeObservable == null) {
            onlineStateChangeObservable = new OnlineStateChangeObservable(Util.getApp());
        }
        return onlineStateChangeObservable;
    }

    private static ContactChangedObservable getContactChangedObservable() {
        if (contactChangedObservable == null) {
            contactChangedObservable = new ContactChangedObservable(Util.getApp());
        }
        return contactChangedObservable;
    }

    private static ContactChangedObserver observer = new ContactChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> accounts) {
            if (accounts == null || accounts.isEmpty()) {
                return;
            }
            List<String> subs = new ArrayList<>();
            for (String account : accounts) {
                if (!OnlineStateEventCache.hasSubscribed(account)) {
                    subs.add(account);
                }
            }
            OnlineStateEventSubscribe.subscribeOnlineStateEvent(subs, OnlineStateEventSubscribe.SUBSCRIBE_EXPIRY);
        }

        @Override
        public void onDeletedFriends(final List<String> accounts) {
            // 如果最近会话里面存在该用户，则不取消订阅
            if (accounts == null || accounts.isEmpty()) {
                return;
            }
            NIMClient.getService(MsgService.class).queryRecentContacts().setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
                @Override
                public void onResult(int code, List<RecentContact> result, Throwable exception) {
                    // 取消订阅名单
                    List<String> unSubs = new ArrayList<>();

                    if (code != ResponseCode.RES_SUCCESS || result == null) {
                        unSubs = accounts;
                    } else {
                        Set<String> recentContactSet = new HashSet<>();
                        for (RecentContact recentContact : result) {
                            if (recentContact.getSessionType() == SessionTypeEnum.P2P) {
                                recentContactSet.add(recentContact.getContactId());
                            }
                        }
                        for (String account : accounts) {
                            if (!recentContactSet.contains(account)) {
                                unSubs.add(account);
                            }
                        }
                    }
                    if (!unSubs.isEmpty()) {
                        OnlineStateEventSubscribe.unSubscribeOnlineStateEvent(unSubs);
                    }
                }
            });
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {

        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {

        }
    };

    /**
     * 在登陆状态变为已登录之后，发布自己的在线状态，订阅事件
     */
    private static void registerOnlineStatusObserver() {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(new Observer<StatusCode>() {
            @Override
            public void onEvent(StatusCode statusCode) {
                if (statusCode != StatusCode.LOGINED) {
                    return;
                }
                // 发布自己的在线状态
                pubNetState = -1;
                L.e("-------------登录成功发布");
//                publishOnlineStateEvent();
                // 订阅在线状态，包括好友以及最近联系人
                OnlineStateEventSubscribe.initSubscribes();
            }
        }, true);
    }

//    private static BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            String action = intent.getAction();
//            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//                NetworkInfo info = cm.getActiveNetworkInfo();
//                if (info == null || !info.isAvailable()) {
//                    return;
//                }
//                if (NIMClient.getStatus() == StatusCode.LOGINED) {
//                    publishOnlineStateEvent();
//                }
//            }
//        }
//    };

//    private static void registerNetTypeChangeObserver() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        Util.getApp().registerReceiver(receiver, filter);
//    }


    /**
     * 从事件中获取该账户的多端在线状态信息
     *
     * @param event
     * @return
     */
    private static Map<Integer, OnlineState> getOnlineStateFromEvent(Event event) {
        if (!NimOnlineStateEvent.isOnlineStateEvent(event)) {
            return null;
        }
        // 解析
        List<Integer> clients = NimOnlineStateEvent.getOnlineClients(event);
        if (clients == null) {
            return null;
        }
        Map<Integer, OnlineState> onlineStates = new HashMap<>();
        for (int i = 0; i < clients.size(); i++) {
            int clientType = clients.get(i);
            OnlineState state = OnlineStateEventConfig.parseConfig(event.getConfigByClient(clientType), clientType);
            if (state == null) {
                state = new OnlineState(clientType, NetStateCode.Unkown, OnlineStateCode.Online);
            }
            onlineStates.put(clientType, state);
        }

        return onlineStates;
    }

    /**
     * 构建一个在线状态事件
     *
     * @param netState            当前在线网络状态
     * @param syncSelfEnable      是否多端同步
     * @param broadcastOnlineOnly 是否只广播给在线用户
     * @param expiry              事件有效期，单位秒
     * @return event
     */
    public static Event buildOnlineStateEvent(int netState, int onlineState, boolean syncSelfEnable, boolean broadcastOnlineOnly, long expiry) {
        Event event = new Event(NimOnlineStateEvent.EVENT_TYPE, NimOnlineStateEvent.MODIFY_EVENT_CONFIG, expiry);
        event.setSyncSelfEnable(syncSelfEnable);
        event.setBroadcastOnlineOnly(broadcastOnlineOnly);
        event.setConfig(OnlineStateEventConfig.buildConfig(netState, onlineState));
        return event;
    }


    /**
     * 发布自己在线状态
     */
    public static void publishOnlineStateEvent() {
//        if (!enable) {
//            return;
//        }
        int netState = getNetWorkTypeName(Util.getApp());
//        if (!force && netState == pubNetState) {
//            return;
//        }
//        pubNetState = netState;
        Event event = buildOnlineStateEvent(netState, OnlineStateCode.Online.getValue(), true, false, EVENT_EXPIRY);
        NIMClient.getService(EventSubscribeService.class).publishEvent(event);
    }

    // 获取网络类型
    private static int getNetWorkTypeName(Context context) {
        return NetworkUtil.getNetworkTypeForLink(context);
    }

    public static OnlineState getDisplayOnlineState(Event event) {
        OnlineState result;
        //判断是否离线
        if (event.getEventValue() == NimOnlineStateEvent.OnlineStateEventValue.Logout.getValue() ||
                event.getEventValue() == NimOnlineStateEvent.OnlineStateEventValue.DisConnect.getValue()) {
            result = new OnlineState(ClientType.Android, NetStateCode.Unkown, OnlineStateCode.Offline);
            return result;
        }
        result = new OnlineState(ClientType.Android, NetStateCode.Unkown, OnlineStateCode.Online);
        return result;
    }

    private static boolean isOnline(OnlineState state) {
        return state != null && state.getOnlineState() != (OnlineStateCode.Offline);
    }

    private static boolean validNetType(OnlineState state) {
        if (state == null) {
            return false;
        }
        NetStateCode netState = state.getNetState();
        return netState != null && netState != NetStateCode.Unkown;
    }

    /**
     * 在线状态显示文案
     *
     * @return
     */
//    public static String getOnlineClientContent(Context context, OnlineState state, boolean simple) {
//        if (!enable) {
//            return null;
//        }
//        // 离线
//        if (!isOnline(state)) {
//            return context.getString("离线");
//        }
//        // 忙碌
//        if (state.getOnlineState() == OnlineStateCode.Busy) {
//            return context.getString(R.string.on_line_busy);
//        }
//        int type = state.getOnlineClient();
//        String result = null;
//        switch (type) {
//            case ClientType.Windows:
//                result = "pc在线";
//                break;
//            case ClientType.MAC:
//                result = "mc在线";
//                break;
//            case ClientType.Web:
//                result = "web在线";
//                break;
//            case ClientType.Android:
//                result = getMobileOnlineClientString(context, state, false, simple);
//                break;
//            case ClientType.iOS:
//                result = getMobileOnlineClientString(context, state, true, simple);
//                break;
//            default:
//                break;
//        }
//        return result;
//    }

//    private static String getMobileOnlineClientString(Context context, OnlineState state, boolean ios, boolean simple) {
//        String result;
//        String client = ios ? "iOS" : "Android";
//        if (!validNetType(state)) {
//            result = client + context.getString(R.string.online);
//        } else {
//            if (simple) {
//                // 简单展示
//                result = getDisplayNetState(state.getNetState()) + context.getString(R.string.online);
//            } else {
//                // 详细展示
//                result = client + " - " + getDisplayNetState(state.getNetState()) + context.getString(R.string.online);
//            }
//        }
//        return result;
//    }
    private static String getDisplayNetState(NetStateCode netStateCode) {
        if (netStateCode == null || netStateCode == NetStateCode.Unkown) {
            return UNKNOWN;
        }
        if (netStateCode == NetStateCode._2G) {
            return NET_TYPE_2G;
        } else if (netStateCode == NetStateCode._3G) {
            return NET_TYPE_3G;
        } else if (netStateCode == NetStateCode._4G) {
            return NET_TYPE_4G;
        } else {
            return NET_TYPE_WIFI;
        }
    }

    /**
     * 注册事件观察者
     */
    private static void registerEventObserver(boolean register) {
        NIMClient.getService(EventSubscribeServiceObserver.class).observeEventChanged(new Observer<List<Event>>() {
            @Override
            public void onEvent(List<Event> events) {
                // 过滤掉旧的事件
                events = EventFilter.getInstance().filterOlderEvent(events);
                if (events == null || events.size() == 0) {
                    return;
                }
                // 筛选出在线状态事件
                List<Event> onlineStateEvents = new ArrayList<>();
                for (int i = 0; i < events.size(); i++) {
                    Event e = events.get(i);
                    if (NimOnlineStateEvent.isOnlineStateEvent(e)) {
                        onlineStateEvents.add(e);
                    }
                }
                // 处理在线状态事件
                receivedOnlineStateEvents(onlineStateEvents);
            }
        }, register);
    }

    /**
     * 接收到在线状态事件
     */
    private static void receivedOnlineStateEvents(List<Event> events) {
        Set<String> changed = new HashSet<String>();
        for (Event event : events) {
            if (NimOnlineStateEvent.isOnlineStateEvent(event)) {
                // 获取优先级最高的在线客户端的状态
                OnlineState state = getDisplayOnlineState(event);
                if (state == null) {
                    return;
                }
//                if (isOnline(state)) {
//                    //有用户上线，再次发布自己的在线状态
//                    publishOnlineStateEvent();
//                }
                changed.add(event.getPublisherAccount());
                // 将事件缓存
                OnlineStateEventCache.cacheOnlineState(event.getPublisherAccount(), state);
            }
        }
        //通知在线状态变化
        getOnlineStateChangeObservable().notifyOnlineStateChange(changed);
    }

    /**
     * 检查是否已经订阅过
     */
//    public static void checkSubscribe(String account) {
//        if (!enable) {
//            return;
//        }
//        if (OnlineStateEventSubscribe.subscribeFilter(account)) {
//            return;
//        }
//        // 未曾订阅过
//        if (!OnlineStateEventCache.hasSubscribed(account)) {
//            List<String> accounts = new ArrayList<>(1);
//            accounts.add(account);
//            OnlineStateEventSubscribe.subscribeOnlineStateEvent(accounts, OnlineStateEventSubscribe.SUBSCRIBE_EXPIRY);
//        }
//    }
    public static boolean isEnable() {
        return enable;
    }

    /**
     * 允许在线状态事件,开发者开通在线状态后修改此处直接返回true
     */
    private static boolean enableOnlineStateEvent() {
        return true;
    }
}