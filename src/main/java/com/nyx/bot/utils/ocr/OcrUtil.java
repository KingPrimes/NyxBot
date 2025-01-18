package com.nyx.bot.utils.ocr;

import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class OcrUtil {


    /**
     * OCR识别图片
     *
     * @param url 图片url
     * @return 识别结果
     */
    public static List<String> ocr(String url) {
        List<String> strings;
        UUID uuid = UUID.randomUUID();
        File file = new File("./" + uuid + ".png");
        URI uri = URI.create(url);
        try (InputStream is = uri.toURL().openStream();
             FileOutputStream fos = new FileOutputStream(file)
        ) {
            byte[] byteArray = toByteArray(is);
            fos.write(byteArray);
            strings = ocrPath(file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        file.delete();
        return strings;
    }


    private static List<String> ocrPath(String imgPath) {
        InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4);
        return Arrays.stream(engine.runOcr(imgPath).getStrRes().split("\n")).toList();
    }

    private static byte[] toByteArray(InputStream is) throws IOException {
        byte[] buf = new byte[81920];
        int read;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(81920);
        while ((read = is.read(buf)) != -1) {
            bos.write(buf, 0, read);
        }
        bos.close();
        return bos.toByteArray();
    }

}
