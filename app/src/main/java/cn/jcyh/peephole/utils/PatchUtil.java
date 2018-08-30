package cn.jcyh.peephole.utils;

/**
 * Created by jogger on 2018/8/28.
 */
public class PatchUtil {
    static {
        System.loadLibrary("dspatch");
    }
    public static native void patch(String oldVersionPath, String newVersionPath, String patchPath);
}
