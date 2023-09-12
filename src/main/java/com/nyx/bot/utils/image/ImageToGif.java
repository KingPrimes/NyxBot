package com.nyx.bot.utils.image;


import com.madgag.gif.fmsware.AnimatedGifEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ImageToGif {
    /**
     * Image到GIF图像
     *
     * @param imageList Image图像列表
     * @param ms        间隔毫秒
     * @return 字节流
     */
    public static ByteArrayOutputStream imagesToGif(List<BufferedImage> imageList, int ms) {
        // 拆分一帧一帧的压缩之后合成
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        encoder.start(os);
        //无线播放
        encoder.setRepeat(0);
        for (BufferedImage bufferedImage :
                imageList) {
            //每帧间隔时间
            encoder.setDelay(ms);
            int height = bufferedImage.getHeight();
            int width = bufferedImage.getWidth();
            BufferedImage zoomImage = new BufferedImage(width, height, 3);
            Image image = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            Graphics gc = zoomImage.getGraphics();
            gc.setColor(Color.WHITE);
            gc.drawImage(image, 0, 0, null);
            encoder.addFrame(zoomImage);
        }
        //刷新所有挂起的数据并关闭输出文件。如果写入输出流，则流不会关闭。
        encoder.finish();
        return os;
    }

    /**
     * Image到GIF图像
     *
     * @param imageList Image图像列表 默认帧间隔时间25ms
     * @return 字节流
     */
    public static ByteArrayOutputStream imagesToGif(List<BufferedImage> imageList) {
        return imagesToGif(imageList, 25);
    }
}
