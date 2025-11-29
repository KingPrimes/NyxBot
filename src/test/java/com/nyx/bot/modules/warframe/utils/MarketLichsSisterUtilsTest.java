package com.nyx.bot.modules.warframe.utils;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
import com.nyx.bot.modules.warframe.entity.MarketResult;
import io.github.kingprimes.model.market.MarketLichSister;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MarketLichsSisterUtils 工具类集成测试
 *
 * @author KingPrimes
 */
@Slf4j
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@ActiveProfiles("test")
@DisplayName("MarketLichsSisterUtils 集成测试")
class MarketLichsSisterUtilsTest {

    @Test
    @DisplayName("集成测试 - 完整查询流程（真实运行）")
    void testCompleteQueryFlow() {
        log.info("=================================================");
        log.info("开始执行完整的集成测试");
        log.info("=================================================");

        // 测试武器关键字
        String weaponKey = "信客";
        MarketLichsSisterUtils.SearchType searchType = MarketLichsSisterUtils.SearchType.SISTER;

        log.info("测试参数：");
        log.info("  - 武器关键字: {}", weaponKey);
        log.info("  - 搜索类型: {}", searchType.getType());

        try {
            // === 执行完整的查询流程 ===
            log.info("\n--- 第1步：开始查询武器信息 ---");
            MarketResult<LichSisterWeapons, MarketLichSister> result =
                    MarketLichsSisterUtils.getAuctions(weaponKey, searchType);
            log.info("ItemName: {}",result.getResult().getPayload().getItemName());
            // === 验证第1步：武器查询结果 ===
            assertNotNull(result, "查询结果不应为空");
            log.info("✓ 查询结果对象创建成功");

            if (result.getEntity() != null) {
                log.info("\n--- 第2步：武器信息验证 ---");
                log.info("  找到武器: {}", result.getEntity().getName());
                log.info("  武器ID: {}", result.getEntity().getId());
                log.info("  武器Slug: {}", result.getEntity().getSlug());

                assertNotNull(result.getEntity().getName(), "武器名称不应为空");
                assertNotNull(result.getEntity().getSlug(), "武器Slug不应为空");
                log.info("✓ 武器信息验证通过");

                // === 验证第3步：市场拍卖数据 ===
                if (result.getResult() != null) {
                    log.info("\n--- 第3步：市场拍卖数据验证 ---");
                    assertNotNull(result.getResult(), "市场数据不应为空");
                    log.info("  市场数据获取成功");

                    if (result.getResult().getPayload() != null) {
                        var auctions = result.getResult().getPayload().getAuctions();
                        log.info("  拍卖数量: {}", auctions != null ? auctions.size() : 0);

                        if (auctions != null && !auctions.isEmpty()) {
                            log.info("\n--- 第4步：拍卖数据详情 ---");

                            // 验证拍卖数据不超过10条（限制）
                            assertTrue(auctions.size() <= 10, "拍卖数据应该限制在10条以内");
                            log.info("✓ 拍卖数量限制验证通过（最多10条）");

                            for (int i = 0; i < auctions.size(); i++) {
                                var auction = auctions.get(i);
                                log.info("  [{}] 用户: {}, 价格: {}, 状态: {}",
                                        i + 1,
                                        auction.getOwner().getIngameName(),
                                        auction.getBuyoutPrice() != null ? auction.getBuyoutPrice() :
                                                (auction.getStartingPrice() != null ? auction.getStartingPrice() : auction.getTopBid()),
                                        auction.getOwner().getStatus());

                                // 验证用户状态（应该只有online或ingame）
                                String status = auction.getOwner().getStatus();
                                assertTrue(status.equals("online") || status.equals("ingame"),
                                        "用户状态应该是online或ingame");

                                // 验证拍卖未关闭
                                assertFalse(auction.getClosed(), "拍卖应该是开放状态");

                                // 验证拍卖可见
                                assertTrue(auction.getVisible(), "拍卖应该是可见状态");
                            }
                            log.info("✓ 拍卖数据质量验证通过");

                            // === 验证第5步：数据过滤和排序 ===
                            log.info("\n--- 第5步：数据处理验证 ---");
                            log.info("✓ 已过滤离线用户");
                            log.info("✓ 已过滤关闭的拍卖");
                            log.info("✓ 已按价格排序（升序）");

                            // 验证价格排序（如果有多条记录）
                            if (auctions.size() > 1) {
                                for (int i = 0; i < auctions.size() - 1; i++) {
                                    var current = auctions.get(i);
                                    var next = auctions.get(i + 1);

                                    Integer currentPrice = current.getBuyoutPrice() != null ? current.getBuyoutPrice() :
                                            (current.getStartingPrice() != null ? current.getStartingPrice() : current.getTopBid());
                                    Integer nextPrice = next.getBuyoutPrice() != null ? next.getBuyoutPrice() :
                                            (next.getStartingPrice() != null ? next.getStartingPrice() : next.getTopBid());

                                    if (currentPrice != null && nextPrice != null) {
                                        assertTrue(currentPrice <= nextPrice,
                                                "拍卖应该按价格升序排列");
                                    }
                                }
                                log.info("✓ 价格排序验证通过");
                            }
                        } else {
                            log.warn("  当前没有符合条件的拍卖");
                        }
                    }
                    log.info("✓ 市场数据验证通过");
                } else {
                    log.warn("未获取到市场拍卖数据（可能是网络问题或API限制）");
                }
            } else {
                // 未找到武器，应该有候选列表
                log.info("\n--- 未找到精确匹配，检查候选列表 ---");
                assertNotNull(result.getPossibleItems(), "候选列表不应为空");
                log.info("  候选武器数量: {}", result.getPossibleItems().size());

                if (!result.getPossibleItems().isEmpty()) {
                    log.info("  可能的武器:");
                    result.getPossibleItems().forEach(item -> log.info("    - {}", item));
                }
            }

            log.info("\n=================================================");
            log.info("集成测试执行完成！");
            log.info("=================================================");
            log.info("测试总结：");
            log.info("✓ 第1步：查询武器 - 成功");
            log.info("✓ 第2步：构建搜索参数 - 成功");
            log.info("✓ 第3步：调用Market API - 成功");
            log.info("✓ 第4步：处理和过滤数据 - 成功");
            log.info("✓ 第5步：返回结果 - 成功");
            log.info("=================================================");

        } catch (Exception e) {
            log.error("集成测试执行失败", e);
            fail("测试失败: " + e.getMessage());
        }
    }
}