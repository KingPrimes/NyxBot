package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.data.ExportFilePath;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.util.List;

/**
 * 武器数据初始化服务
 *
 * @author KingPrimes
 */
@Slf4j
@Service
public class WeaponService {

    private final ObjectMapper objectMapper;
    private final WeaponsRepository weaponsRepository;

    public WeaponService(ObjectMapper objectMapper, WeaponsRepository weaponsRepository) {
        this.objectMapper = objectMapper;
        this.weaponsRepository = weaponsRepository;
    }

    /**
     * 从导出文件初始化武器数据（路径内部解析）
     */
    @Transactional
    public void initFromExport() {
        String exportPath = ExportFilePath.resolve("ExportWeapons");
        log.info("开始初始化  Weapons 数据！");
        try {
            JsonNode rootNode = objectMapper.readTree(new FileInputStream(exportPath));
            JsonNode arrayNode = rootNode.get("ExportWeapons");
            List<Weapons> weapons = objectMapper.convertValue(
                    arrayNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Weapons.class));
            weapons.forEach(w -> w.setEnglishName(w.contEnglishName()));
            int size = weaponsRepository.saveAll(weapons).size();
            log.info("初始化 Weapons 数据完成，共{}条", size);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Weapons from " + exportPath, e);
        }
    }
}
