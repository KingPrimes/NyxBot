package com.nyx.bot.utils.ocr.detection;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import com.nyx.bot.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文字检测
 *
 * @author Calvin
 * @mail 179209347@qq.com
 * @website www.aias.top
 */
@Slf4j
public final class OcrV4Detection {

    static String path = System.getProperty("user.dir");
    static String modePath = path + "\\models\\ch_PP-OCRv4_det_infer.zip";

    public OcrV4Detection() {
    }

    /**
     * 中文文本检测
     *
     * @return
     */
    public static Criteria<Image, NDList> chDetCriteria() throws IOException {
        ClassPathResource resource = new ClassPathResource("models/ch_PP-OCRv4_det_infer.zip");
        FileUtils.copyFile(resource.getInputStream(), new File(modePath));
        return Criteria.builder()
                .optEngine("OnnxRuntime")
//                        .optModelName("inference")
                .setTypes(Image.class, NDList.class)
                .optModelPath(Paths.get(modePath))
                .optTranslator(new OCRDetectionTranslator(new ConcurrentHashMap<String, String>()))
                .optProgress(new ProgressBar())
                .build();
    }

}
