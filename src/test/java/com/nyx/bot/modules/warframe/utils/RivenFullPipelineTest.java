package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.utils.riven_calculation.RivenAttributeCompute;
import com.nyx.bot.utils.ocr.OcrUtil;
import io.github.kingprimes.model.RivenAnalyseTrendModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * 紫卡分析全链路测试 — 从本地图片OCR到综合分析
 * <p>
 * 将紫卡截图放入 ./temp/test-riven/ 目录（支持 .png .jpg），
 * 测试会自动扫描、OCR识别、解析属性、计算偏差、综合分析并打印结果。
 * </p>
 */
@Slf4j
@SpringBootTest(classes = NyxBotApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.NEVER)
public class RivenFullPipelineTest {

    private static final Path IMAGE_DIR = Paths.get("./temp/test-riven");

    @Autowired
    RivenAttributeCompute rivenAttributeCompute;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * 扫描测试图片目录，返回所有支持的图片路径
     */
    static List<Path> findTestImages() throws IOException {
        if (!Files.exists(IMAGE_DIR)) {
            log.warn("测试图片目录不存在: {}", IMAGE_DIR.toAbsolutePath());
            return List.of();
        }
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

    /**
     * 单张图片全链路测试
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("findTestImages")
    @DisplayName("紫卡图片全链路测试")
    void testFullPipelineFromImage(Path imagePath) throws Exception {
        String filename = imagePath.getFileName().toString();
        log.info("╔══════════════════════════════════════╗");
        log.info("║  测试图片: {} ", filename);
        log.info("╚══════════════════════════════════════╝");

        // Phase 1: OCR
        log.info("[Phase 1] OCR 识别...");
        List<String> ocrText = OcrUtil.ocrPath(imagePath.toAbsolutePath().toString());
        assertNotNull(ocrText, "OCR结果不应为null");
        assertFalse(ocrText.isEmpty(), "OCR结果不应为空");
        log.info("OCR 识别到 {} 行文字:", ocrText.size());
        ocrText.forEach(line -> log.info("  > {}", line));

        // Phase 2: 解析
        log.info("[Phase 2] 解析紫卡属性...");
        RivenAnalyseTrendCompute riven = RivenAttributeCompute.getRiven(new ArrayList<>(ocrText));
        assertNotNull(riven, "解析结果不应为null");
        assertNotNull(riven.getWeaponsName(), "武器名称不应为null: " + riven);
        assertFalse(riven.getAttributes().isEmpty(), "至少应有一条属性: " + riven);
        log.info("武器: {}, 紫卡名: {}, 属性数: {}",
                riven.getWeaponsName(), riven.getRivenName(), riven.getAttributes().size());
        riven.getAttributes().forEach(a -> log.info("  {} {}", a.getAttributeName(), a.getAttribute()));

        // Phase 3: 偏差计算 + 综合分析
        log.info("[Phase 3] 偏差计算 + 综合分析...");
        List<RivenAnalyseTrendModel> models = rivenAttributeCompute.setAttributeNumber(riven);
        assertNotNull(models, "计算结果不应为null");
        assertFalse(models.isEmpty(), "至少应有一个武器匹配");

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(models);
        log.info("分析结果 ({} 个匹配武器):\n{}", models.size(), json);

        // Phase 4: 逐条验证
        for (RivenAnalyseTrendModel model : models) {
            log.info("--- {} (倾向: {}) ---", model.getWeaponName(), model.getNum());
            assertNotNull(model.getWeaponName());
            assertNotNull(model.getAttributes());
            for (var attr : model.getAttributes()) {
                log.info("  [{}] 实际:{} | 范围:{}-{} | 偏差:{} | 比率:{} | 评分:{} | 致命:{} | 分析:{}",
                        attr.getAttributeName(),
                        attr.getAttr(),
                        attr.getLowAttr(), attr.getHighAttr(),
                        attr.getAttrDiff(),
                        attr.getRatio() != null ? attr.getRatio() : "-",
                        attr.getGrade() != null ? attr.getGrade() : "-",
                        attr.getLethalLevel() != null ? attr.getLethalLevel() : "-",
                        attr.getAnalysis() != null ? attr.getAnalysis() : "");
                // 验证必要字段非空
                assertNotNull(attr.getAttrDiff(), "偏差不应为null: " + attr.getAttributeName());
            }
        }

        log.info("✅ 全链路测试通过: {}", filename);
    }

    /**
     * 如果图片目录为空，跳过参数化测试，用硬编码数据跑一次完整链路
     */
    @Test
    @DisplayName("硬编码数据全链路验证（无需图片）")
    void testFullPipelineWithHardcodedData() throws Exception {
        log.info("使用硬编码数据验证全链路...");

        // 模拟 OCR 结果: 科林斯 紫卡
        List<String> ocrText = List.of(
                "科林斯 Visi-critatis",
                "+137.3% 暴击几率",
                "+153.3% 多重射击",
                "+47.9% 武器后坐力",
                "段位 14"
        );

        log.info("模拟 OCR 文字:");
        ocrText.forEach(line -> log.info("  > {}", line));

        RivenAnalyseTrendCompute riven = RivenAttributeCompute.getRiven(new ArrayList<>(ocrText));
        assertNotNull(riven);
        assertNotNull(riven.getWeaponsName(), "武器名称识别失败");
        assertEquals(3, riven.getAttributes().size(), "应识别出3条属性");

        List<RivenAnalyseTrendModel> models = rivenAttributeCompute.setAttributeNumber(riven);
        assertNotNull(models);
        assertFalse(models.isEmpty());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(models);
        log.info("全链路硬编码数据结果:\n{}", json);

        // 验证综合分析字段
        RivenAnalyseTrendModel model = models.getFirst();
        for (var attr : model.getAttributes()) {
            log.info("  {} | 偏差:{} | 比率:{} | 评分:{} | 致命:{} | {}",
                    attr.getAttributeName(),
                    attr.getAttrDiff(),
                    attr.getRatio() != null ? attr.getRatio() : "N/A",
                    attr.getGrade() != null ? attr.getGrade() : "N/A",
                    attr.getLethalLevel() != null ? attr.getLethalLevel() : "N/A",
                    attr.getAnalysis() != null ? attr.getAnalysis() : "");
        }
    }
}
