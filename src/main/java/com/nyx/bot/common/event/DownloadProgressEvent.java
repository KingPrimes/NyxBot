package com.nyx.bot.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 文件下载进度事件
 * 由 HttpUtils 发布，供 SSE/WebSocket 推送下载进度到前端
 *
 * @author KingPrimes
 */
@Getter
public class DownloadProgressEvent extends ApplicationEvent {

    private final String url;
    private final long downloaded;
    private final long total;
    private final boolean done;

    public DownloadProgressEvent(Object source, String url, long downloaded, long total, boolean done) {
        super(source);
        this.url = url;
        this.downloaded = downloaded;
        this.total = total;
        this.done = done;
    }
}
