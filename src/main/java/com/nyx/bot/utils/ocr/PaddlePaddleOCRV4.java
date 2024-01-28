package com.nyx.bot.utils.ocr;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import com.nyx.bot.utils.ocr.common.RotatedBox;
import com.nyx.bot.utils.ocr.common.RotatedBoxCompX;
import com.nyx.bot.utils.ocr.detection.OcrV4Detection;
import com.nyx.bot.utils.ocr.recognition.OcrV4Recognition;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Tong Li on 11/23/2023_2:09 AM
 */
@Slf4j
public enum PaddlePaddleOCRV4 {
    INSTANCE;

    private final OcrV4Recognition recognition;
    private final Predictor<Image, NDList> detector;
    private final Predictor<Image, String> recognizer;
    private final NDManager manager;

    PaddlePaddleOCRV4() {
        OcrV4Detection detection = new OcrV4Detection();
        recognition = new OcrV4Recognition();
        ZooModel detectionModel = null;
        ZooModel recognitionModel = null;
        try {
            detectionModel = ModelZoo.loadModel(detection.chDetCriteria());
            recognitionModel = ModelZoo.loadModel(recognition.chRecCriteria());
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            error(e);
        }
        detector = detectionModel.newPredictor();
        recognizer = recognitionModel.newPredictor();
        manager = NDManager.newBaseManager();
    }

    // noting not to do.but init
    public void init() {

    }

    private void error(Exception e) {
        log.error(e.getMessage());
    }

    public List<String> ocr(String url) throws Exception {
        Image image = OpenCVImageFactory.getInstance().fromUrl(url);
        return ocr(image);
    }

    public List<String> ocr(URL resource) throws Exception {
        Image image = OpenCVImageFactory.getInstance().fromUrl(resource);
        return ocr(image);
    }

    public List<String> ocr(byte[] fileData) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(fileData);
        Image image = ImageFactory.getInstance().fromInputStream(is);
        return ocr(image);
    }

    public List<String> ocr(File imageFile) throws Exception {
        Path path = imageFile.toPath();
        Image image = OpenCVImageFactory.getInstance().fromFile(path);
        return ocr(image);
    }

    public List<String> ocr(Image image) throws Exception {
        List<RotatedBox> detections = recognition.predict(manager, image, detector, recognizer);

        // put low Y value at the head of the queue.
        List<RotatedBox> initList = new ArrayList<>(detections);
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

        List<String> fullText = new ArrayList<>();
        for (ArrayList<RotatedBoxCompX> rotatedBoxCompXES : lines) {
            for (RotatedBoxCompX rotatedBoxCompX : rotatedBoxCompXES) {
                String text = rotatedBoxCompX.getText();
                if (text.trim().isEmpty())
                    continue;
                fullText.add(text);
            }
        }
        return fullText;
    }

    public void close() {
        detector.close();
        recognizer.close();
    }

}
