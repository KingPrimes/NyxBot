package com.nyx.bot.utils.onebot;

public class PrivateUtils {
    /**
     * 根据QQ号获取头像
     * 无法获取头像！本方法已弃用
     *
     * @param userId QQ账号
     */
    @Deprecated
    public static String getPrivateHeadImage(long userId) {
        //返回拼接的QQ头像网址
        return String.format("http://q2.qlogo.cn/headimg_dl?dst_uin=%s&spec=100", userId);
    }

    /**
     * 获取高清头像
     *
     * @param userId qq账号
     * @return 链接
     */
    public static String getPrivateHeadHDImage(long userId) {
        return String.format("http://q1.qlogo.cn/g?b=qq&nk=%s&s=640", userId);
    }

    /**
     * 获取用户头像
     *
     * @param userId qq账号
     * @return 链接
     */
    public static String getUserQzone(long userId) {
        return getUserQzone(userId, 100);
    }

    /**
     * 获取用户头像
     *
     * @param userId qq账号
     * @return 链接
     */
    public static String getUserQzone(long userId, int size) {
        return String.format("http://qlogo2.store.qq.com/qzone/%s/%s/%s", userId, userId, size);
    }
}
