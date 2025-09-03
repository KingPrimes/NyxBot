package com.nyx.bot.modules.warframe.utils;

import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.RivenTrendEnum;
import com.nyx.bot.enums.RivenTrendTypeEnum;
import com.nyx.bot.modules.warframe.entity.RivenTrend;
import com.nyx.bot.modules.warframe.repo.RivenTrendRepository;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RivenDispositionUpdates {

    /**
     * 获取查询最新的论坛Url
     *
     * @return Url地址
     */
    private static List<String> getRivenDispositionUpdateUrl() {
        HttpUtils.Body body = HttpUtils.sendGet("https://forums.warframe.com/search/?&q=Riven&type=forums_topic&quick=1&nodes=123&search_and_or=and&search_in=titles&sortby=relevancy");
        //返回变量
        List<String> newRiven = new ArrayList<>();
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
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

    /**
     * 获取Warframe论坛中的紫卡倾向列表
     *
     * @return 紫卡倾向集
     */
    private static List<RivenTrend> getRivenDispositionUpdates() {
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

            //用于存放返回的结果
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
        return trends;
    }

    public Integer upRivenTrend() {
        log.info("正在执行任务...");
        RivenTrendRepository bean = SpringUtils.getBean(RivenTrendRepository.class);
        List<RivenTrend> rivenTrends = getRivenDispositionUpdates();
        if (rivenTrends.isEmpty()) {
            return -1;
        }
        rivenTrends.forEach(rivenTrend -> bean.findByTrendName(rivenTrend.getTrendName()).ifPresentOrElse(rt -> {
                    rivenTrend.setId(rt.getId());
                    rivenTrend.setType(rt.getType());
                    bean.save(rivenTrend);
                },
                () -> bean.save(rivenTrend)
        ));
        log.info("更新完毕...");
        return rivenTrends.size();
    }


}
