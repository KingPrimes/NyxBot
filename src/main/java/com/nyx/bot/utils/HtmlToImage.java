package com.nyx.bot.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.entity.Hint;
import com.nyx.bot.repo.HintRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ui.ModelMap;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.simple.Graphics2DRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j

public class HtmlToImage {


    public static final String HTML_PATH = "./DataSource/Template/";

    /**
     * 高清Html转Image
     *
     * @param url   url
     * @param width 宽度
     * @return BufferedImage
     */
    private static BufferedImage renderToImageAutoSize(String url, int width) {
        try {
            Graphics2DRenderer g2r = new Graphics2DRenderer(url);
            SharedContext sharedContext = g2r.getSharedContext();
            //设置图片清晰度
            sharedContext.setDPI(150);

            sharedContext.setDotsPerPixel(2);

            //设置字体
            getFontInputStream().forEach(sharedContext::setFontMapping);

            Dimension dim = new Dimension(width, 1000);
            BufferedImage buff = new BufferedImage((int) dim.getWidth(), (int) dim.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g = (Graphics2D) buff.getGraphics();
            g2r.layout(g, new Dimension(width, 1000));
            g.dispose();
            Rectangle rect = g2r.getMinimumSize();
            buff = new BufferedImage((int) rect.getWidth(), (int) rect.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            g = (Graphics2D) buff.getGraphics();
            g2r.render(g);
            g.dispose();
            return buff;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private static Map<String, Font> getFontInputStream() {
        String fontPath = HTML_PATH + "css/font.json";
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Font> fontMap = new HashMap<>();
        try {
            JsonNode jsonNode = mapper.readTree(new File(fontPath));
            for (JsonNode node : jsonNode) {
                File font = new File(node.get("src").asText());
                if (font.exists()) {
                    fontMap.put(node.get("name").asText(), Font.createFont(Font.TRUETYPE_FONT, font));
                } else {
                    fontMap.put(node.get("name").asText(), Font.createFont(Font.TRUETYPE_FONT, new File(HTML_PATH + "css/" + node.get("src").asText())));
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return fontMap;
    }

    /**
     * 获取宽度
     *
     * @param html html文档
     * @return 宽度
     */
    private static int getWidth(String html) {
        Document doc = Jsoup.parse(html);
        int width = 1000;
        //判断是否添加宽度标签
        if (!doc.getElementsByTag("w").isEmpty()) {
            String num = doc.getElementsByTag("w").text();
            if (MatcherUtils.isNumber(num)) width = Integer.parseInt(num);
        }
        return width;
    }

    /**
     * 删除不相干的字段
     *
     * @param html html 文档
     * @return 格式化之后的 html文档
     */
    @SuppressWarnings("all")
    private static String outH(String html) {
        Hint hint = SpringUtils.getBean(HintRepository.class).randOne();
        html = html.replaceAll("<!--", "<xx>").replaceAll("-->", "</xx>");
        Document doc = Jsoup.parse(html);
        if (!doc.getElementsByTag("xx").isEmpty()) {
            int i = doc.getElementsByTag("xx").size();
            for (; i > 0; i--) {
                html = new StringBuilder(html).replace(html.indexOf("<xx>"), html.indexOf("</xx>") + 5, "").toString().trim();
            }
        }
        if (!doc.getElementsByTag("w").isEmpty()) {
            html = new StringBuilder(html).replace(html.indexOf("<w>"), html.indexOf("</w>") + 4, "").toString().trim();
        }
        html = html.replaceAll("/css/{0,}", "./css/").replaceAll("/img/{0,}", "./img/");
        StringBuilder str = getBuilder(html, hint);
        return str.toString();
    }

    @SuppressWarnings("all")
    private static StringBuilder getBuilder(String html, Hint hint) {
        StringBuilder str = new StringBuilder(html);
        if (str.indexOf("</body>") > 1) {
            if (hint != null) {
                str.insert(str.indexOf("</body>"),
                        """
                                        <div style="width: 100%; bottom: 0; text-align: center;">
                                                %s <br\\>
                                                Posted by:KingPrimes
                                        </div>
                                """.formatted(hint.getHint())
                );
            } else {
                str.insert(str.indexOf("</body>"), """
                        <div style="width: 100%; bottom: 0; text-align: center;">
                                Posted by:KingPrimes
                        </div>
                        """);
            }
        }
        return str;
    }

    /***
     * 根据Html文本生成图片字节流
     * @param html html文本
     * @param width 图片宽度
     * @return 字节流
     */
    private static ByteArrayOutputStream tmpHtmlToImageByteArray(String html, int width) {
        String path = HTML_PATH;
        path = path + "/" + new Date().getTime() + StringUtils.getRandomString() + ".html";
        try {
            FileOutputStream fo = new FileOutputStream(path);
            OutputStreamWriter os = new OutputStreamWriter(fo, StandardCharsets.UTF_8);
            os.write(html);
            os.flush();
            os.close();
        } catch (Exception e) {
            log.error("生成临时文件出错\n\t\t地址：{}", path, e);
        }
        return convertHtmlToImage(path, width);
    }

    /**
     * html 文档转成 字节流
     *
     * @param htmlFilePath html文件路径
     * @param width        生成图片的宽度
     * @return 字节流
     */
    @SuppressWarnings("all")
    private static ByteArrayOutputStream convertHtmlToImage(String htmlFilePath, int width) {
        try {
            File htmlFile = new File(htmlFilePath);
            String url = htmlFile.toURI().toURL().toExternalForm();
            BufferedImage image = renderToImageAutoSize(url, width);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Optional.ofNullable(image).ifPresent(i -> {
                try {
                    ImageIO.write(i, "png", os);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            htmlFile.delete();
            return os;
        } catch (Exception e) {
            log.error("html渲染字节流出错，文件路径：{}", htmlFilePath, e);
        }
        return null;
    }

    /**
     * 通用模板图片生成方法
     *
     * @param templateName  Thymeleaf模板名称
     * @param modelSupplier 模板数据提供者
     * @return 图片字节流
     */
    public static ByteArrayOutputStream generateImage(String templateName, Supplier<ModelMap> modelSupplier) throws HtmlToImageException, DataNotInfoException {
        // 通过SpringUtils获取Thymeleaf模板引擎
        SpringTemplateEngine templateEngine = SpringUtils.getBean("customTemplateEngine");

        // 获取模板数据
        ModelMap model = modelSupplier.get();
        if (model == null) {
            throw new HtmlToImageException("模板数据不能为空");
        }

        // 创建Thymeleaf上下文并设置变量
        Context context = new Context();
        context.setVariables(model);
        // 渲染HTML模板
        String html = templateEngine.process(templateName, context);

        // 生成图片字节流
        return converseHtml(html);
    }

    /**
     * 通用模板图片生成方法
     *
     * @param templateName Thymeleaf模板名称
     * @param data         模板数据
     * @param modelName    模板数据名称
     * @return 图片字节流
     */
    public static byte[] generateImageBytes(String templateName, Object data, String modelName) throws HtmlToImageException, DataNotInfoException {
        ModelMap model = new ModelMap();
        model.put(modelName, data);
        return generateImage(templateName, () -> model).toByteArray();
    }

    /**
     * 直接通过HTML字符串生成图片
     *
     * @param html HTML字符串
     * @return 图片流
     */
    private static ByteArrayOutputStream converseHtml(String html) throws HtmlToImageException {
        if (html == null) {
            throw new HtmlToImageException(I18nUtils.message("error.html.image"));
        }
        int width = getWidth(html);
        html = outH(html);
        return tmpHtmlToImageByteArray(html, width);
    }
}
