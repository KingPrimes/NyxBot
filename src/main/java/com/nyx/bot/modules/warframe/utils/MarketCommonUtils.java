package com.nyx.bot.modules.warframe.utils;

import com.nyx.bot.modules.warframe.entity.Alias;
import com.nyx.bot.modules.warframe.entity.MarketResult;
import com.nyx.bot.modules.warframe.repo.AliasRepository;
import com.nyx.bot.modules.warframe.repo.NameRegexJpaRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Market工具类公共方法
 * <p>提供所有Market工具类共用的方法，减少代码重复</p>
 *
 * @author KingPrimes
 */
@Slf4j
public class MarketCommonUtils {

    /**
     * 处理别名替换
     * <p>遍历所有别名，将输入字符串中的中文别名替换为英文</p>
     *
     * @param input           输入字符串
     * @param aliasRepository 别名仓库
     * @return 替换后的字符串
     */
    public static String processAliases(String input, AliasRepository aliasRepository) {
        List<Alias> aliases = aliasRepository.findAll();

        for (Alias alias : aliases) {
            if (input.contains(alias.getCn())) {
                return input.replace(alias.getCn(), alias.getEn());
            }
        }
        return input;
    }

    /**
     * 尝试匹配实体
     * <p>通用的匹配方法，可用于任何实体类型的查找</p>
     *
     * @param <E>    实体类型
     * @param <R>    结果类型
     * @param market Market结果对象
     * @param key    查找关键字
     * @param lookup 查找函数
     * @return Optional包装的实体
     */
    public static <E, R> boolean tryMatch(
            MarketResult<E, R> market,
            String key,
            Function<String, Optional<E>> lookup
    ) {
        Optional<E> apply = lookup.apply(key);
        apply.ifPresent(market::setEntity);
        return apply.isPresent();
    }

    /**
     * 标准化输入字符串
     * <p>将字符串转为小写并进行常见的替换操作</p>
     *
     * @param input 输入字符串
     * @return 标准化后的字符串
     */
    public static String normalizeInput(String input) {
        return input.toLowerCase(java.util.Locale.ROOT)
                .replace("总图", "蓝图");
    }

    /**
     * 处理Prime关键词
     * <p>将简写的"p"替换为完整的"Prime"</p>
     *
     * @param key 输入关键词
     * @return 处理后的关键词
     */
    public static String processPrimeKeyword(String key) {
        if (!key.contains("prime") && key.contains("p")) {
            return key.replace("p", "Prime");
        }
        return key;
    }

    /**
     * 尝试使用正则表达式进行名称匹配来查找市场物品
     * 该方法通过截取关键字的前缀和后缀构建正则表达式模式，然后在仓库中查找匹配的实体。
     * 它会尝试不同长度的前缀（最多4个字符）来进行匹配搜索。
     *
     * @param <E>          实体类型
     * @param <R>          返回结果类型
     * @param <REPOSITORY> 继承自BaseJpaRepository的仓库类型
     * @param market       包装市场查询结果的对象
     * @param key          用于匹配的关键字
     * @param re           实体仓库，用于执行查询操作
     * @return 如果找到匹配项则返回true，否则返回false
     */
    public static <E, R, REPOSITORY extends NameRegexJpaRepository<E, ?>> boolean tryRegexNameMatch(MarketResult<E, R> market, String key, REPOSITORY re) {
        if (key.length() < 2) {
            return false;
        }
        // 获取最后一个字符
        String end = key.substring(key.length() - 1);

        // 确定最大前缀长度 - 最多4个字符，但不能超过字符串总长度-1
        int maxPrefixLength = Math.min(4, key.length() - 1);
        // 从最长前缀开始尝试，逐步减少到1个字符
        for (int prefixLength = maxPrefixLength; prefixLength >= 1; prefixLength--) {
            String header = key.substring(0, prefixLength);
            log.info("tryRegexNameMatch - Prefix : '{}' -- Suffix: '{}'", header, end);
            Optional<E> items = re.findByNameRegex("^" + header + ".*?" + end + ".*?");
            if (items.isPresent()) {
                market.setEntity(items.get());
                return true;
            }
        }

        return false;
    }
}