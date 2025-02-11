package com.nyx.bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@Slf4j
public class IoUtils {

    //启动完成之后自动打开浏览器并访问 Url 地址
    public static void index() {
        String url = "http://localhost:" + SpringUtils.getPort();
        // 获取操作系统的名字
        try {
            // 苹果的打开方式
            if (SystemUtils.IS_OS_MAC) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        String.class);
                openURL.invoke(null, url);
                return;
            }
            // windows的打开方式。
            if (SystemUtils.IS_OS_WINDOWS) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                return;
            }
            //Unix or Linux的打开方式
            if (SystemUtils.IS_OS_UNIX || SystemUtils.IS_OS_LINUX) {
                //Docker 环境不打开浏览器
                String str = Arrays.toString(Files.readAllBytes(Paths.get("/proc/1/cgroup")));
                if (str.contains("/docker/")) return;

                String[] browsers = {"firefox", "opera", "konqueror", "epiphany",
                        "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                    // 执行代码，在brower有值后跳出，
                    // 这里是如果进程创建成功了，==0是表示正常结束。
                    if (Runtime.getRuntime()
                            .exec(new String[]{"which", browsers[count]})
                            .waitFor() == 0)
                        browser = browsers[count];
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else
                    // 这个值在上面已经成功的得到了一个进程。
                    Runtime.getRuntime().exec(new String[]{browser, url});
            }
        } catch (Exception e) {
            log.warn("Browser opening error", e);
        }
    }
}
