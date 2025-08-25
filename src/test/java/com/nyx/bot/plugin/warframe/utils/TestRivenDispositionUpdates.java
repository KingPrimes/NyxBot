package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.RivenTrendEnum;
import com.nyx.bot.enums.RivenTrendTypeEnum;
import com.nyx.bot.modules.warframe.entity.RivenTrend;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestRivenDispositionUpdates {


    /**
     * 获取查询最新的论坛Url
     */
    private static List<String> getRivenDispositionUpdateUrl() {
        HttpUtils.Body body = HttpUtils.sendGet("https://forums.warframe.com/search/?&q=Riven&type=forums_topic&quick=1&nodes=123&search_and_or=and&search_in=titles&sortby=relevancy");
        //返回变量
        List<String> newRiven = new ArrayList<>();
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            log.error("获取论坛搜索列表失败！状态码:{}", body.getCode());
            return newRiven;
        }
        String html = body.getBody();

        //解析Html文档
        Document document = Jsoup.parse(html);
        Elements ols = document.getElementsByTag("ol");
        if (!ols.isEmpty()) {
            for (Element ol : ols) {
                Elements lis = ol.getElementsByAttribute("data-role");
                for (Element li : lis) {
                    Elements as = li.getElementsByAttribute("data-linkType");
                    for (Element url : as) {
                        newRiven.add(url.attr("href"));
                    }
                    break;
                }
            }
        }

        return newRiven;
    }

    @Test
    public void getRivenDispositionUpdateUrlTest() {
        List<String> rivenDispositionUpdates = getRivenDispositionUpdateUrl();
        log.info("论坛搜索列表元素数：{}", rivenDispositionUpdates.size());
        log.info("论坛搜索列表：{}", rivenDispositionUpdates);
    }

    /**
     * 获取Warframe论坛中的紫卡倾向列表
     */
    @Test
    public void getRivenDispositionUpdates() {
        //获取文档Url地址
        List<String> urls = getRivenDispositionUpdateUrl();
        //用于存放返回的结果
        List<RivenTrend> trends = new ArrayList<>();
        String dateTime = "";
        boolean falg = false;
        for (String url : urls) {
            //Get请求获取Html文档
            HttpUtils.Body body = HttpUtils.sendGet(url);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                continue;
            }
            String html = body.getBody();
            Document document = Jsoup.parse(html);
            Elements times = document.getElementsByClass("ipsType_light ipsType_reset");
            // 获取发帖日期
            for (Element el : times) {
                dateTime = el.getElementsByTag("time").attr("datetime").replace("T", " ").replace("Z", "").trim();
                break;
            }
            // 获取 内容
            Elements ipsTypeNormalIpsTypeRichTextIpsPaddingBottomIpsContained = document.getElementsByClass("ipsType_normal ipsType_richText ipsPadding_bottom ipsContained");
            // 武器类型标志
            String type = "";
            for (Element element : ipsTypeNormalIpsTypeRichTextIpsPaddingBottomIpsContained) {
                for (Element div : element.getElementsByTag("div")) {
                    boolean ipsSpoilerContentsIpsClearfix = div.hasClass("ipsSpoiler_contents ipsClearfix");
                    if (ipsSpoilerContentsIpsClearfix) {
                        falg = true;
                        Elements ps = element.getElementsByTag("p");
                        for (Element p : ps) {
                            RivenTrend rt = new RivenTrend();
                            Elements strong = p.getElementsByTag("strong");

                            if (!strong.text().trim().isEmpty()) {
                                type = strong.text();
                            }
                            switch (type.toUpperCase()) {
                                case "PRIMARIES" -> rt.setType(RivenTrendTypeEnum.RIFLE);
                                case "SECONDARIES" -> rt.setType(RivenTrendTypeEnum.PISTOL);
                                case "MELEES" -> rt.setType(RivenTrendTypeEnum.MELEE);
                                case "ARCHGUNS" -> rt.setType(RivenTrendTypeEnum.ARCHGUN);
                            }
                            Elements span = p.getElementsByTag("span");
                            String riven = span.html();
                            if (!riven.trim().isEmpty()) {
                                riven = StringEscapeUtils.unescapeHtml4(riven);
                                String[] rivenSplit = riven.split("<br>");
                                for (String r : rivenSplit) {
                                    String[] wep = r.split(":");
                                    String[] number = wep[1].split("->");
                                    rt.setTrendName(wep[0].replaceAll("\n", "").trim());
                                    String newNum = number[1];
                                    if (newNum.contains("(")) {
                                        newNum = newNum.replaceAll(newNum.substring(newNum.indexOf("(")), "");
                                    }
                                    newNum = newNum.replaceAll("[^\\d.]", "");
                                    rt.setNewNum(Double.valueOf(newNum));
                                    rt.setOldNum(Double.valueOf(number[0]));
                                    //设置新的倾向点
                                    rt.setNewDot(RivenTrendEnum.getRivenTrendDot(Double.parseDouble(newNum)));
                                    //设置旧的倾向点
                                    rt.setOldDot(RivenTrendEnum.getRivenTrendDot(Double.parseDouble(number[0])));
                                    //设置更改日期
                                    rt.setIsDate(Timestamp.valueOf(dateTime));
                                    trends.add(rt);
                                }
                            }
                        }
                        break;
                    }
                }
            }
            if (falg) {
                break;
            }
        }
        log.info("变动日期:{}\n获取到的紫卡倾向变动具体数值:{}", dateTime, trends);
    }


}
