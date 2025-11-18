package com.nyx.bot.models.image;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.res.worldstate.KnownCalendarSeasons;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.ui.ModelMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestKnownCalendarSeasonsForImage {

    private static final String TEST_IMAGE_PATH = "./data/knownCalendarSeasons_test.png";
    @Resource
    StateTranslationRepository str;

    private WorldState worldState;

    public TestKnownCalendarSeasonsForImage() {
    }

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        try (FileInputStream state = new FileInputStream("./data/state-2025-11-7.json")) {
            worldState = JSON.parseObject(state, WorldState.class);
        } catch (IOException e) {
            log.error("初始化测试数据失败", e);
            fail("测试初始化失败: " + e.getMessage());
        }
    }

    @Test
    void testKnownCalendarSeasons() throws DataNotInfoException, HtmlToImageException, IOException {
        // 生成图片字节数据
        byte[] imageBytes = postKnownCalendarSeasonsImage();

        // 验证字节数据不为空
        assertNotNull(imageBytes, "生成的图片字节数据为空");
        assertTrue(imageBytes.length > 0, "生成的图片字节数据长度为0");

        // 将字节数据写入文件
        try (FileOutputStream fos = new FileOutputStream(TEST_IMAGE_PATH)) {
            fos.write(imageBytes);
        }

        // 验证文件是否生成
        File imageFile = new File(TEST_IMAGE_PATH);
        assertTrue(imageFile.exists(), "图片文件未生成");
        assertTrue(imageFile.length() > 0, "生成的图片文件为空");

        // 验证图片格式是否正确
        try (InputStream is = new FileInputStream(imageFile)) {
            BufferedImage image = ImageIO.read(is);
            assertNotNull(image, "无法解析生成的图片文件");
            assertTrue(image.getWidth() > 0 && image.getHeight() > 0, "生成的图片尺寸异常");
        }

        log.info("测试图片生成成功，文件路径: {}", imageFile.getAbsolutePath());
    }

    private byte[] postKnownCalendarSeasonsImage() throws DataNotInfoException, HtmlToImageException {
        List<KnownCalendarSeasons> seasons = worldState.getKnownCalendarSeasons().stream()
                .map(KnownCalendarSeasons::copy) // 使用优化后的copy方法
                .peek(this::processSeason) // 提取处理逻辑为独立方法
                .toList();
        log.info("KnownCalendarSeasons:{}", JSON.toJSONString(seasons));
        return generateImage(seasons);
    }


    private void processEvent(KnownCalendarSeasons.Events event) {
        switch (event.getType()) {
            case CET_CHALLENGE -> str.findByUniqueName(StringUtils.getLastThreeSegments(event.getChallenge()))
                    .ifPresent(s -> event.setChallengeInfo(new KnownCalendarSeasons.Events.Challenge(s.getName(), s.getDescription())));

            case CET_REWARD -> str.findByUniqueName(StringUtils.getLastThreeSegments(event.getReward()))
                    .ifPresent(s -> event.setReward(StringUtils.deleteBetweenAndMarkers(s.getName(), '<', '>')));

            case CET_UPGRADE -> str.findByUniqueName(StringUtils.getLastThreeSegments(event.getUpgrade()))
                    .ifPresent(s -> event.setUpgradeInfo(new KnownCalendarSeasons.Events.Upgrade(s.getName(), s.getDescription())));

        }
    }

    private byte[] generateImage(List<KnownCalendarSeasons> seasons) throws HtmlToImageException, DataNotInfoException {
        return HtmlToImage.generateImage("html/knownCalendarSeasons", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.addAttribute("seasons", seasons);
            return modelMap;
        }).toByteArray();
    }

    private void processSeason(KnownCalendarSeasons season) {
        season.processDays();
        season.setMonthDays(season.getDays().stream()
                .peek(day -> day.setEvents(
                        day.getEvents().stream()
                                .peek(this::processEvent)
                                .collect(Collectors.toList())
                )).collect(Collectors.groupingBy(KnownCalendarSeasons.Days::getMonth))
        );
        //season.setDays(null);
    }
}
