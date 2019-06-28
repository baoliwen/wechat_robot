package com.dosimple.robot.util;

import java.util.concurrent.TimeUnit;

public class SleepUtils {
    /**
     * 毫秒为单位
     * @param time
     */
    public static void sleep(long time){
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
