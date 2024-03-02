package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.entity.warframe.RivenTrend;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.RivenTrendEnum;
import com.nyx.bot.repo.warframe.RivenTrendRepository;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

            //解析Html文档
            Document document = Jsoup.parse(html);
            Elements elements = document.getElementsByClass("ipsSpoiler_contents ipsClearfix");
            if (elements.size() > 1) {
                falg = true;
                //获取发帖日期
                Elements times = document.getElementsByClass("ipsType_light ipsType_reset");
                for (Element el : times) {
                    dateTime = el.getElementsByTag("time").attr("datetime").replace("T", " ").replace("Z", "").trim();
                    break;
                }
                //根据类选择器 获取此类中的元素
                //Elements elements = document.getElementsByClass("ipsSpoiler_contents ipsClearfix");
                //遍历此类中元素
                for (Element element : elements) {
                    //根据标签选择器 获取此标签中的元素
                    Elements p = element.getElementsByTag("p");
                    //遍历 p 标签中的元素
                    for (Element e1 : p) {
                        //格式化 br更改为\n换行 出去多余的strong标签
                        String br = String.valueOf(e1).replace("<br>", "\n").replaceAll("<strong>.*?</strong>", "");
                        if (br.trim().equals("<p>&nbsp;</p>") || br.replace("\n", "").trim().isEmpty()) {
                            continue;
                        }
                        //重新解析格式化后的Html
                        Document doc = Jsoup.parse(br);
                        //遍历此标签中的元素
                        for (Element e2 : doc.getElementsByTag("span")) {
                            //根据换行字符裁剪内容
                            String[] rives = e2.text().split("\n");
                            for (String riven : rives) {
                                //根据：裁剪内容
                                String[] wep = riven.split(":");
                                //根据->裁剪内容
                                String[] rent = wep[1].split("->");
                                //新建倾向变更实体类
                                RivenTrend trend = new RivenTrend();
                                //根据：裁剪内容的第0位下标是武器名
                                trend.setTrendName(wep[0]);
                                //根据->裁剪内容的第1位下标是更改后的倾向值
                                String newNum = rent[1];
                                if (newNum.contains("(")) {
                                    newNum = newNum.replaceAll(newNum.substring(newNum.indexOf("(")), "");
                                }
                                newNum = newNum.replaceAll("[^\\d.]", "");
                                //设置新的倾向值
                                trend.setNewNum(Double.valueOf(newNum));
                                //设置旧的倾向值
                                trend.setOldNum(Double.valueOf(rent[0]));
                                //设置新的倾向点
                                trend.setNewDot(RivenTrendEnum.getRivenTrendDot(Double.parseDouble(newNum)));
                                //设置旧的倾向点
                                trend.setOldDot(RivenTrendEnum.getRivenTrendDot(Double.parseDouble(rent[0])));
                                //设置更改日期
                                trend.setIsDate(dateTime);
                                //保存
                                trends.add(trend);
                            }
                        }
                    }
                }
            }
            if (falg) {
                break;
            }
        }
        return trends;
    }

    public void upRivenTrend() {
        log.info("正在执行任务...");
        RivenTrendRepository bean = SpringUtils.getBean(RivenTrendRepository.class);
        List<RivenTrend> rivenTrends = getRivenDispositionUpdates();
        if (rivenTrends.isEmpty()) {
            return;
        }
        rivenTrends.forEach(rivenTrend -> {
            RivenTrend byTrendName = bean.findByTrendName(rivenTrend.getTrendName());
            if (byTrendName != null) {
                rivenTrend.setId(byTrendName.getId());
                bean.save(rivenTrend);
            } else {
                bean.save(rivenTrend);
            }
        });
    }


}
