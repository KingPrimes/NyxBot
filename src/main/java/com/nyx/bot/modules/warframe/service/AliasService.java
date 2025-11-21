package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.common.exception.ServiceException;
import com.nyx.bot.modules.warframe.entity.Alias;
import com.nyx.bot.modules.warframe.repo.AliasRepository;
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
public class AliasService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 同步锁，用于防止并发更新别名数据时的乐观锁冲突
     */
    private static final Object ALIAS_UPDATE_LOCK = new Object();

    @Resource
    AliasRepository aliasRepository;

    private List<Alias> getAlias() {
        List<Alias> aliasList = new ArrayList<>();
        for (String url : ApiUrl.WARFRAME_DATA_SOURCE_ALIAS) {
            HttpUtils.Body body = HttpUtils.sendGet(url);
            if (body.code().is2xxSuccessful()) {
                try {
                    aliasList.addAll(objectMapper.readValue(body.body(), new TypeReference<List<Alias>>() {
                    }));
                    break;
                } catch (Exception e) {
                    // 忽略解析错误，继续处理其他数据源
                    log.warn("解析别名数据失败，尝试下一个数据源: {}", e.getMessage());
                }
            } else {
                log.warn("获取别名数据失败，尝试下一个数据源:  HttpCode {} - Url:{}", body.code(), url);
            }
        }
        return aliasList;
    }

    /**
     * 更新别名数据
     * <br/>
     * 该方法首先获取别名列表，如果获取失败或列表为空，则抛出服务异常。<br/>
     * 使用"清空-重新插入"策略避免乐观锁冲突，并通过事务确保数据一致性。
     * <br/>
     * 注意：使用同步锁 + 事务保证并发安全性
     *
     * @return 更新的别名数据数量
     * @throws ServiceException 当别名数据获取失败时抛出此异常
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateAlias() {
        synchronized (ALIAS_UPDATE_LOCK) {
            log.debug("开始更新别名数据，获取数据源...");
            List<Alias> aliasList = getAlias();
            if (aliasList.isEmpty()) {
                throw new ServiceException("别名数据获取失败！", 500);
            }
            log.debug("获取到 {} 条别名数据，准备更新数据库", aliasList.size());
            
            try {
                // 策略：查询现有数据，智能更新
                // 1. 获取现有数据建立映射 (cn -> Alias)
                List<Alias> existingAliases = aliasRepository.findAll();
                Map<String, Alias> existingMap = existingAliases.stream()
                        .collect(Collectors.toMap(Alias::getCn, Function.identity(), (a1, a2) -> a1));
                
                log.debug("数据库中现有 {} 条别名数据", existingMap.size());
                
                // 2. 处理新数据：更新ID以避免冲突
                List<Alias> toSave = new ArrayList<>();
                for (Alias newAlias : aliasList) {
                    Alias existing = existingMap.get(newAlias.getCn());
                    if (existing != null) {
                        // 存在相同的cn，复用ID
                        newAlias.setId(existing.getId());
                    } else {
                        // 新数据，确保ID为null以便自动生成
                        newAlias.setId(null);
                    }
                    toSave.add(newAlias);
                }
                
                // 3. 批量保存（save会根据ID判断是insert还是update）
                List<Alias> saved = aliasRepository.saveAll(toSave);
                aliasRepository.flush();
                
                log.debug("别名数据更新完成，共 {} 条", saved.size());
                return saved.size();
            } catch (Exception e) {
                log.error("更新别名数据时发生异常", e);
                throw new ServiceException("更新别名数据失败: " + e.getMessage(), 500);
            }
        }
    }

}
