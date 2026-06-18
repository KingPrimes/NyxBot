package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.data.ExportFilePath;
import com.nyx.bot.modules.warframe.entity.exprot.Nodes;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.utils.ApiDataSourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.util.List;

/**
 * 星图节点数据初始化服务
 *
 * @author KingPrimes
 */
@Slf4j
@Service
public class NodeService {

    private final ObjectMapper objectMapper;
    private final NodesRepository nodesRepository;
    private final ApiDataSourceUtils apiDataSourceUtils;

    public NodeService(ObjectMapper objectMapper, NodesRepository nodesRepository,
                       ApiDataSourceUtils apiDataSourceUtils) {
        this.objectMapper = objectMapper;
        this.nodesRepository = nodesRepository;
        this.apiDataSourceUtils = apiDataSourceUtils;
    }

    /**
     * 从导出文件 + CDN 初始化节点数据（路径内部解析）
     */
    @Transactional
    public void initData() {
        initFromExportFile();
        initFromCdn();
    }

    private void initFromExportFile() {
        String exportPath = ExportFilePath.resolve("ExportRegions");
        log.info("开始初始化  Nodes 数据！");
        try {
            JsonNode rootNode = objectMapper.readTree(new FileInputStream(exportPath));
            JsonNode arrayNode = rootNode.get("ExportRegions");
            List<Nodes> nodesList = objectMapper.convertValue(
                    arrayNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Nodes.class));
            int size = nodesRepository.saveAll(nodesList).size();
            log.info("初始化 Nodes 数据完成，共{}条", size);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Nodes from " + exportPath, e);
        }
    }

    private void initFromCdn() {
        log.info("开始初始化 自定义 Nodes nodes.json 数据！");
        List<Nodes> nodes = apiDataSourceUtils.getDataFromSources(
                ApiUrl.warframeDataSourceNodes(),
                new TypeReference<>() {
                });
        int size = nodesRepository.saveAll(nodes).size();
        log.info("初始化 自定义 Nodes nodes.json 数据完成，共{}条", size);
    }
}
