package com.nyx.bot.utils.onebot;

public class PrivateUtils {
    /**
     * 根据QQ号获取头像
     *
     * @param userId QQ账号
     */
    public static String getPrivateHeadImage(Long userId) {
        //返回拼接的QQ头像网址
        return "https://q2.qlogo.cn/headimg_dl?dst_uin=" + userId + "&spec=100";
    }

    public static String getPrivateHeadHDImage(Long userId) {
        return "http://q1.qlogo.cn/g?b=qq&nk=" + userId + "&s=640";
    }
}
