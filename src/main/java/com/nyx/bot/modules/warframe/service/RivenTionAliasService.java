package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.common.exception.ServiceException;
import com.nyx.bot.modules.warframe.entity.RivenTionAlias;
import com.nyx.bot.modules.warframe.repo.RivenTionAliasRepository;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RivenTionAliasService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 同步锁，用于防止并发更新紫卡词条别名数据时的乐观锁冲突
     */
    private static final Object RIVEN_TION_ALIAS_UPDATE_LOCK = new Object();
    
    @Resource
    RivenTionAliasRepository rivenTionAliasRepository;

    private List<RivenTionAlias> getRivenTionAlias() {
        List<RivenTionAlias> rats = new ArrayList<>();
        for (String url : ApiUrl.WARFRAME_DATA_SOURCE_MARKET_RIVEN_TION_ALIAS) {
            HttpUtils.Body body = HttpUtils.sendGet(url);
            if (body.code().is2xxSuccessful()) {
                try {
                    rats.addAll(objectMapper.readValue(body.body(), new TypeReference<List<RivenTionAlias>>() {
                    }));
                    break;
                } catch (Exception e) {
                    // 忽略解析错误，继续处理其他数据源
                    log.warn("解析RivenTionAlias数据失败，尝试下一个数据源: {}", e.getMessage());
                }
            } else {
                log.warn("获取RivenTionAlias数据失败，尝试下一个数据源: HttpCode {} - Url:{}", body.code(), url);
            }
        }
        return rats;
    }

    /**
     * 更新紫卡词条别名数据
     * <br/>
     * 使用"智能更新"策略避免乐观锁冲突，并通过事务确保数据一致性。
     * <br/>
     * 注意：使用同步锁 + 事务保证并发安全性
     *
     * @return 更新的紫卡词条别名数据数量
     * @throws ServiceException 当紫卡词条别名数据获取失败时抛出此异常
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateRivenTionAlias() {
        synchronized (RIVEN_TION_ALIAS_UPDATE_LOCK) {
            log.debug("开始更新紫卡词条别名数据，获取数据源...");
            List<RivenTionAlias> rivenTionAliasList = getRivenTionAlias();
            if (rivenTionAliasList.isEmpty()) {
                throw new ServiceException("RivenTionAlias数据获取失败！", 500);
            }
            log.debug("获取到 {} 条紫卡词条别名数据，准备更新数据库", rivenTionAliasList.size());
            
            try {
                // 策略：查询现有数据，使用唯一约束字段 (en + cn) 进行映射
                List<RivenTionAlias> existingList = rivenTionAliasRepository.findAll();
                Map<String, RivenTionAlias> existingMap = existingList.stream()
                        .filter(rta -> rta.getEn() != null && rta.getCn() != null)
                        .collect(Collectors.toMap(
                                rta -> rta.getEn() + "|" + rta.getCn(),
                                Function.identity(),
                                (a1, a2) -> a1
                        ));
                
                log.debug("数据库中现有 {} 条紫卡词条别名数据", existingMap.size());
                
                // 处理新数据：为已存在的记录复用ID
                List<RivenTionAlias> toSave = new ArrayList<>();
                for (RivenTionAlias newAlias : rivenTionAliasList) {
                    String key = newAlias.getEn() + "|" + newAlias.getCn();
                    RivenTionAlias existing = existingMap.get(key);
                    if (existing != null) {
                        // 存在相同的 en + cn 组合，复用ID
                        newAlias.setId(existing.getId());
                    } else {
                        // 新数据，确保ID为null以便自动生成
                        newAlias.setId(null);
                    }
                    toSave.add(newAlias);
                }
                
                // 批量保存
                List<RivenTionAlias> saved = rivenTionAliasRepository.saveAll(toSave);
                rivenTionAliasRepository.flush();
                
                log.debug("紫卡词条别名数据更新完成，共 {} 条", saved.size());
                return saved.size();
            } catch (Exception e) {
                log.error("更新紫卡词条别名数据时发生异常", e);
                throw new ServiceException("更新紫卡词条别名数据失败: " + e.getMessage(), 500);
            }
        }
    }
}
