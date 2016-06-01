package utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Adminis on 2016/5/28.
 */
public final class CloseUtils {

    /**
     * 用来关闭实现了closeable的类.
     */
    private CloseUtils() {

    }

    public static void close(Closeable closeable) {

        if (closeable != null) {
            try {
                closeable.close();

            } catch (IOException e) {
                MyLog.LogE(closeable.getClass().getSimpleName(), "close工具无法关闭该closeable!");
                e.printStackTrace();
            }
        }
    }

}
