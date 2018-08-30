package cn.jcyh.peephole.utils;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.nimlib.sdk.misc.MiscService;

import java.util.ArrayList;
import java.util.List;

import cn.jcyh.peephole.constant.Constant;

/**
 * Created by jogger on 2018/8/21.
 */
public class CacheUtil {
    public static void init() {
        SPUtil.getInstance().put(Constant.LAST_CACHE_DATE, System.currentTimeMillis());
    }

    public static void clearCache() {
        long currentTimeMillis = System.currentTimeMillis();
        long lastTime = SPUtil.getInstance().getLong(Constant.LAST_CACHE_DATE, 0);
        int date = (int) ((currentTimeMillis - lastTime) / 1000f / 60f / 60f / 24f);
        if (date >= 7) {
            clear();
            SPUtil.getInstance().put(Constant.LAST_CACHE_DATE, currentTimeMillis);
        }
    }

    private static void clear() {
        List<DirCacheFileType> fileTypes = new ArrayList<>();
        fileTypes.add(DirCacheFileType.LOG);
        fileTypes.add(DirCacheFileType.THUMB);
        fileTypes.add(DirCacheFileType.IMAGE);
        fileTypes.add(DirCacheFileType.AUDIO);
        NIMClient.getService(MiscService.class).clearDirCache(fileTypes, 0, 0);
    }
}
