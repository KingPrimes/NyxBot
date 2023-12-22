package com.nyx.bot.utils.ocr.detection;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文字检测
 *
 * @author Calvin
 * @mail 179209347@qq.com
 * @website www.aias.top
 */
public final class OcrV4Detection {

    public OcrV4Detection() {
    }

    /**
     * 中文文本检测
     *
     * @return
     */
    public Criteria<Image, NDList> chDetCriteria() {
        URL resource = this.getClass().getClassLoader().getResource("models/ch_PP-OCRv4_det_infer.zip");
        Criteria<Image, NDList> criteria =
                null;
        try {
            criteria = Criteria.builder()
                    .optEngine("OnnxRuntime")
//                        .optModelName("inference")
                    .setTypes(Image.class, NDList.class)
                    .optModelPath(Paths.get(resource.toURI()))
                    .optTranslator(new OCRDetectionTranslator(new ConcurrentHashMap<String, String>()))
                    .optProgress(new ProgressBar())
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return criteria;
    }

}
