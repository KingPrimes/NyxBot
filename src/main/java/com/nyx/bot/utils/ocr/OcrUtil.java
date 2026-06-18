package com.nyx.bot.utils.ocr;

import com.benjaminwan.ocrlibrary.OcrResult;
import com.nyx.bot.utils.http.HttpFileDownloader;
import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;
import io.github.mymonstercat.ocr.config.ParamConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class OcrUtil {

    /**
     * OCR 推理使用平台线程池隔离 JNI 调用，避免 pin 住虚拟线程的载体线程。
     * 2 线程适配 2 核低配服务器，daemon 避免阻止 JVM 退出。
     */
    private static final ExecutorService OCR_EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "ocr-jni-worker");
        t.setDaemon(true);
        return t;
    });


    /**
     * OCR识别图片
     *
     * @param url 图片url
     * @return 识别结果
     */

    public static List<String> ocr(String url) {
        String filePath = "./" + UUID.randomUUID() + ".png";
        try {
            if (!HttpFileDownloader.sendGetForFile(url, filePath)) {
                log.warn("下载图片失败: {}", url);
                return List.of();
            }
            return ocrPath(filePath);
        } finally {
            new File(filePath).delete();
        }
    }


    public static List<String> ocrPath(String imgPath) {
        Future<List<String>> future = OCR_EXECUTOR.submit(() -> {
            ParamConfig paramConfig = ParamConfig.getDefaultConfig();
            paramConfig.setPadding(50);
            // 限制图像最大边长，大图全分辨率检测会将拉丁字母拆成独立字符
            paramConfig.setMaxSideLen(960);
            paramConfig.setBoxScoreThresh(0.6F);
            paramConfig.setBoxThresh(0.3F);
            paramConfig.setUnClipRatio(2.5F);
            paramConfig.setMostAngle(true);
            InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4);
            OcrResult result = engine.runOcr(imgPath, paramConfig);
            // 移除 \r 干扰字符（大图检测时 PP-OCR 会在同行的每个字符后插入 \r）
            String clean = result.getStrRes().replace("\r", "");
            return Arrays.stream(clean.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        });
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("OCR推理被中断");
            return List.of();
        } catch (ExecutionException e) {
            log.error("OCR推理执行异常", e.getCause());
            throw new RuntimeException(e.getCause());
        }
    }

}
