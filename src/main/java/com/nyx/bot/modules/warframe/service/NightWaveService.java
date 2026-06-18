package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.data.ExportFilePath;
import com.nyx.bot.modules.warframe.entity.exprot.NightWave;
import com.nyx.bot.modules.warframe.repo.exprot.NightWaveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * 午夜电波数据初始化服务
 *
 * @author KingPrimes
 */
@Slf4j
@Service
public class NightWaveService {

    private final ObjectMapper objectMapper;
    private final NightWaveRepository nightWaveRepository;

    public NightWaveService(ObjectMapper objectMapper, NightWaveRepository nightWaveRepository) {
        this.objectMapper = objectMapper;
        this.nightWaveRepository = nightWaveRepository;
    }

    /**
     * 从导出文件初始化午夜电波数据（路径内部解析）
     *
     * @return 保存的数据条数
     */
    @Transactional
    public int initFromExport() {
        String exportPath = ExportFilePath.resolve("ExportSortieRewards");
        log.info("开始初始化  NightWave 数据！");
        try {
            JsonNode rootNode = objectMapper.readTree(new FileInputStream(exportPath));
            JsonNode challengesNode = rootNode.get("ExportNightwave").get("challenges");
            List<NightWave> javaList = objectMapper.convertValue(
                    challengesNode,
                    new TypeReference<>() {
                    });
            int size = nightWaveRepository.saveAll(javaList).size();
            log.info("初始化 NightWave 数据完成，共{}条", size);
            return size;
        } catch (FileNotFoundException e) {
            log.error("ExportSortieRewards 文件不存在", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("解析 NightWave 数据失败", e);
            throw new RuntimeException(e);
        }
    }
}
