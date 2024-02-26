package com.nyx.bot.utils;

import com.nyx.bot.entity.Hint;
import com.nyx.bot.exception.HtmlToImageException;
import com.nyx.bot.repo.HintRepository;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.simple.Graphics2DRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Slf4j

public class HtmlToImage {

    private static final String HTML_PATH = "./nyxTemplates/";

    /**
     * 高清Html转Image
     *
     * @param url   url
     * @param width 宽度
     * @return BufferedImage
     */
    private static BufferedImage renderToImageAutoSize(String url, int width) {
        try {
            Graphics2DRenderer g2r = new Graphics2DRenderer();
            g2r.setDocument(url);
            SharedContext sharedContext = g2r.getSharedContext();
            //设置图片清晰度
            sharedContext.setDPI(72);
            sharedContext.setDotsPerPixel(3);
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
        html = html.replaceAll("/css/{0,}", "./css/")
                .replaceAll("/img/{0,}", "./img/");
        StringBuilder str = getBuilder(html, hint);
        return str.toString();
    }

    @NotNull
    private static StringBuilder getBuilder(String html, Hint hint) {
        StringBuilder str = new StringBuilder(html);
        if (str.indexOf("</body>") > 1) {
            if (hint != null) {
                str.insert(str.indexOf("</body>"), "<div class=\"foot-by\">\n" +
                        "\tPosted by:KingPrimes<br/>\n" +
                        "\t" +
                        hint.getHint() +
                        "\n</div>\n");
            } else {
                str.insert(str.indexOf("</body>"), """
                        <div class="foot-by">
                        \tPosted by:KingPrimes<br/>
                        \t
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
        path = path + "/" + new Date().getTime() + ".html";
        try {
            FileOutputStream fo = new FileOutputStream(path);
            OutputStreamWriter os = new OutputStreamWriter(fo, StandardCharsets.UTF_8);
            os.write(html);
            os.flush();
            os.close();
        } catch (Exception e) {
            log.error("生成临时文件出错\n\t\t地址：{}\n\t\t错误信息：{}", path, e.getMessage());
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
            log.error("html渲染字节流出错，文件路径：{}\n\t\t错误信息：{}", htmlFilePath, e.getMessage());
        }
        return null;
    }

    /**
     * @param url Html Url地址
     * @return 图片流
     */
    public static ByteArrayOutputStream conver(String url) throws HtmlToImageException {
        String html = HttpUtils.sendGet(url).getBody();
        if (html == null) {
            throw new HtmlToImageException(I18nUtils.message("error.html.image"));
        }
        int width = getWidth(html);
        html = outH(html);
        return tmpHtmlToImageByteArray(html, width);
    }

    /**
     * @param url Html Url地址
     * @return 图片流
     */
    public static ByteArrayOutputStream converPost(String url, String json) throws HtmlToImageException {
        String html = HttpUtils.sendPost(url, json).getBody();
        if (html == null) {
            throw new HtmlToImageException(I18nUtils.message("error.html.image"));
        }
        int width = getWidth(html);
        html = outH(html);
        return tmpHtmlToImageByteArray(html, width);
    }

}
