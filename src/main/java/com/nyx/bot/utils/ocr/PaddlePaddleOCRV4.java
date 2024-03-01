package com.nyx.bot.utils.ocr;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import com.nyx.bot.utils.ocr.common.RotatedBox;
import com.nyx.bot.utils.ocr.common.RotatedBoxCompX;
import com.nyx.bot.utils.ocr.detection.OcrV4Detection;
import com.nyx.bot.utils.ocr.recognition.OcrV4Recognition;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class PaddlePaddleOCRV4 {

    public static List<String> ocr(String url) throws Exception {
        Image image = OpenCVImageFactory.getInstance().fromUrl(url);
        return ocr(image);
    }

    public static List<String> ocr(URL resource) throws Exception {
        Image image = OpenCVImageFactory.getInstance().fromUrl(resource);
        return ocr(image);
    }

    public static List<String> ocr(byte[] fileData) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(fileData);
        Image image = ImageFactory.getInstance().fromInputStream(is);
        return ocr(image);
    }

    public static List<String> ocr(File imageFile) throws Exception {
        Path path = imageFile.toPath();
        Image image = OpenCVImageFactory.getInstance().fromFile(path);
        return ocr(image);
    }

    public static List<String> ocr(Image image) throws Exception {
        System.setProperty("ai.djl.pytorch.graph_optimizer", "false");
        OcrV4Recognition recognition = new OcrV4Recognition();
        List<String> fullText = new ArrayList<>();
        try (ZooModel detectionModel = ModelZoo.loadModel(OcrV4Detection.chDetCriteria());
             Predictor detector = detectionModel.newPredictor();
             ZooModel recognitionModel = ModelZoo.loadModel(OcrV4Recognition.chRecCriteria());
             Predictor<Image, String> recognizer = recognitionModel.newPredictor();
             NDManager manager = NDManager.newBaseManager()) {

            List<RotatedBox> detections = recognition.predict(manager, image, detector, recognizer);

            // 对检测结果根据坐标位置，根据从上到下，从做到右，重新排序，下面算法对图片倾斜旋转角度较小的情形适用
            // 如果图片旋转角度较大，则需要自行改进算法，需要根据斜率校正计算位置。
            List<RotatedBox> initList = new ArrayList<>(detections);
            for (RotatedBox result : detections) {
                // put low Y value at the head of the queue.
                initList.add(result);
            }
            Collections.sort(initList);

            List<ArrayList<RotatedBoxCompX>> lines = new ArrayList<>();
            List<RotatedBoxCompX> line = new ArrayList<>();
            RotatedBoxCompX firstBox = new RotatedBoxCompX(initList.get(0).getBox(), initList.get(0).getText());
            line.add(firstBox);
            lines.add((ArrayList) line);
            for (int i = 1; i < initList.size(); i++) {
                RotatedBoxCompX tmpBox = new RotatedBoxCompX(initList.get(i).getBox(), initList.get(i).getText());
                float y1 = firstBox.getBox().toFloatArray()[1];
                float y2 = tmpBox.getBox().toFloatArray()[1];
                float dis = Math.abs(y2 - y1);
                if (dis < 20) { // 认为是同 1 行 - Considered to be in the same line
                    line.add(tmpBox);
                } else { // 换行 - Line break
                    firstBox = tmpBox;
                    Collections.sort(line);
                    line = new ArrayList<>();
                    line.add(firstBox);
                    lines.add((ArrayList) line);
                }
            }

            for (ArrayList<RotatedBoxCompX> rotatedBoxCompXES : lines) {
                for (RotatedBoxCompX rotatedBoxCompX : rotatedBoxCompXES) {
                    String text = rotatedBoxCompX.getText();
                    if (text.trim().isEmpty())
                        continue;
                    fullText.add(text);
                }
            }
        }
        return fullText;

    }

}
