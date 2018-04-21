package cn.jcyh.peephole.utils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {
    public static final String TAG = "spotmau:Utils";
    public static final String IR_LED_EN_STATE = "/sys/devices/platform/CUSTDriver/driver/IrLedEnState";
    public static final String PIR_EN_STATE = "/sys/devices/platform/CUSTDriver/driver/PirEnState";
    public static final String PIR_STATE = "/sys/devices/platform/CUSTDriver/driver/PirState";
    public static final String LOCK_DETECT_STATE = "/sys/devices/platform/CUSTDriver/driver/LockDetectState";
    public static final String LIGHT_SENSOR_VALUE = "/sys/devices/platform/CUSTDriver/driver/LightSensor";
    public static final String SPK1_STATE = "/sys/bus/platform/drivers/mt-soc-codec/ExtspkampState";
    public static final String SPK2_STATE = "/sys/bus/platform/drivers/mt-soc-codec/Extspkamp2State";
    public static final String RING_KEY_LED_STATE = "/sys/class/leds/button-backlight/brightness";

    public Utils() {
    }

    public static int readKeyStatus(String filepath) {
        int keystatus = 0;

        try {
            char[] e = new char[100];
            File devFile = new File(filepath);
            if(devFile.exists()) {
                FileReader readComm = new FileReader(devFile);
                int count = readComm.read(e);
                readComm.close();
                String temp = String.valueOf(e, 0, count - 1);
                if(count > 0) {
                    keystatus = Integer.parseInt(temp);
                }
            }

            Log.d("spotmau:Utils", "Read " + filepath + " = " + keystatus);
        } catch (FileNotFoundException var7) {
            var7.printStackTrace();
            Log.e("spotmau:Utils", "Read " + filepath, var7);
        } catch (IOException var8) {
            var8.printStackTrace();
            Log.e("spotmau:Utils", "Read " + filepath, var8);
        }

        return keystatus;
    }

    public static void setKeyStatus(String filepath, int value) {
        File file = new File(filepath);
        if(file.exists()) {
            try {
                FileWriter e = new FileWriter(file);
                e.write(String.valueOf(value));
                e.close();
                Log.d("spotmau:Utils", "Write " + filepath + " = " + value);
            } catch (IOException var4) {
                var4.printStackTrace();
                Log.e("spotmau:Utils", "Write " + filepath + " = " + value, var4);
            }
        } else {
            Log.e("spotmau:Utils", "Write " + filepath + " = " + value + ", not exist!!");
        }

    }
}
