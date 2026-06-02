package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.data.ExportFilePath;
import com.nyx.bot.modules.warframe.entity.exprot.Warframes;
import com.nyx.bot.modules.warframe.repo.exprot.WarframesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.util.List;

/**
 * 战甲数据初始化服务
 *
 * @author KingPrimes
 */
@Slf4j
@Service
public class WarframeService {

    private final ObjectMapper objectMapper;
    private final WarframesRepository warframesRepository;

    public WarframeService(ObjectMapper objectMapper, WarframesRepository warframesRepository) {
        this.objectMapper = objectMapper;
        this.warframesRepository = warframesRepository;
    }

    /**
     * 从导出文件初始化战甲数据（路径内部解析）
     *
     * @return 保存的数据条数
     */
    @Transactional
    public int initFromExport() {
        String exportPath = ExportFilePath.resolve("ExportWarframes");
        log.info("开始初始化 Warframes 数据！");
        try {
            JsonNode rootNode = objectMapper.readTree(new FileInputStream(exportPath));
            JsonNode arrayNode = rootNode.get("ExportWarframes");
            List<Warframes> warframes = objectMapper.convertValue(
                    arrayNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Warframes.class));
            int size = warframesRepository.saveAll(warframes).size();
            log.info("初始化 Warframes 数据完成，共{}条", size);
            return size;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Warframes from " + exportPath, e);
        }
    }
}
