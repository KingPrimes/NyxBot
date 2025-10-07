package com.nyx.bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class SvgToImageUtils {

    public static final String HTML_PATH = "./DataSource/Template/";

    /**
     * 渲染 SVG 模板并转换为 PNG 图片
     *
     * @param templateName 模板名称（不含 .svg 后缀）
     * @param data         模板数据
     * @return PNG 图片字节数组
     */
    public static byte[] generatePng(String templateName, Map<String, Object> data) {
        // 1. 使用 Thymeleaf 渲染 SVG 模板
        Context context = new Context();
        context.setVariables(data);
        SpringTemplateEngine templateEngine = SpringUtils.getBean("customTemplateEngine");
        String svgContent = templateEngine.process(templateName, context);

        if (!svgContent.contains("xmlns=\"http://www.w3.org/2000/svg\"")) {
            svgContent = svgContent.replace("<svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"");
        }

        // 3. 使用 Batik 将 SVG 转换为 PNG
        try (ByteArrayInputStream svgStream = new ByteArrayInputStream(svgContent.getBytes(StandardCharsets.UTF_8));
             ByteArrayOutputStream pngStream = new ByteArrayOutputStream()) {
            TranscoderInput input = new TranscoderInput(svgStream);
            input.setURI(HTML_PATH);  // 设置基础URI，让Batik能解析相对路径
            TranscoderOutput output = new TranscoderOutput(pngStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.transcode(input, output);

            return pngStream.toByteArray();
        } catch (Exception e) {
            log.error("SVG to PNG conversion failed", e);
            return null;
        }
    }
}
