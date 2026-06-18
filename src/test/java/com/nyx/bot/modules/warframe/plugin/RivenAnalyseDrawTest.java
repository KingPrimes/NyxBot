package com.nyx.bot.modules.warframe.plugin;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.utils.riven_calculation.RivenAttributeCompute;
import com.nyx.bot.utils.ocr.OcrUtil;
import io.github.kingprimes.defaultdraw.DefaultDrawImagePlugin;
import io.github.kingprimes.model.RivenAnalyseTrendModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 紫卡分析绘制全链路测试 — OCR → 解析 → 计算 → 绘制输出 PNG
 * <p>
 * 将紫卡截图放入 ./temp/test-riven/ 目录，
 * 测试会自动扫描、OCR识别、计算偏差、综合分析、绘制图片并保存到 ./temp/test-riven-draw/
 * </p>
 */
@Slf4j
@SpringBootTest(classes = NyxBotApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@DisplayName("紫卡分析绘制全链路测试")
class RivenAnalyseDrawTest {

    private static final Path IMAGE_DIR = Paths.get("temp", "test-riven");
    private static final Path OUT_DIR = Paths.get("temp", "test-riven-draw");
    private static DefaultDrawImagePlugin drawImagePlugin;
    @Autowired
    RivenAttributeCompute rivenAttributeCompute;

    @BeforeAll
    static void init() throws IOException {
        drawImagePlugin = new DefaultDrawImagePlugin();
        Files.createDirectories(OUT_DIR);
    }

    static List<Path> findTestImages() throws IOException {
        if (!Files.exists(IMAGE_DIR)) return List.of();
        try (Stream<Path> stream = Files.list(IMAGE_DIR)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
                    })
                    .sorted()
                    .toList();
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("findTestImages")
    @DisplayName("紫卡分析绘制测试")
    void testRivenDrawFromImage(Path imagePath) throws Exception {
        String filename = imagePath.getFileName().toString();
        String baseName = filename.substring(0, filename.lastIndexOf('.'));
        log.info("=== 紫卡绘制测试: {} ===", filename);

        // Phase 1: OCR
        List<String> ocrText = OcrUtil.ocrPath(imagePath.toAbsolutePath().toString());
        assertNotNull(ocrText);
        assertFalse(ocrText.isEmpty(), "OCR结果不应为空");
        log.info("OCR: {} 行", ocrText.size());

        // Phase 2 + 3: 解析 + 计算
        RivenAnalyseTrendCompute riven = RivenAttributeCompute.getRiven(new ArrayList<>(ocrText));
        assertNotNull(riven.getWeaponsName(), "未识别到武器名: " + ocrText);
        List<RivenAnalyseTrendModel> models = rivenAttributeCompute.setAttributeNumber(riven);
        assertFalse(models.isEmpty(), "计算结果不应为空");

        // Phase 4: 绘制
        byte[] imgBytes = drawImagePlugin.drawRivenAnalyseTrendImage(models);
        assertNotNull(imgBytes);
        assertTrue(imgBytes.length > 0, "绘制结果不应为空");

        Path outFile = OUT_DIR.resolve(baseName + ".png");
        Files.write(outFile, imgBytes);
        log.info("✅ 绘制成功: {} ({}B) → {}", filename, imgBytes.length, outFile);
    }
}
