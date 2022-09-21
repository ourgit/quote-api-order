package utils;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

/**
 * Created by Administrator on 2017/3/21.
 */
public class IdGenerator {
    public static final char[] DEFAULT_NUMBER = "0123456789".toCharArray();

    /**
     * 实现不重复的时间
     */

    public static String getId() {
//        AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());
//        return lastTime.incrementAndGet();
        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, DEFAULT_NUMBER, 13);
    }
}
