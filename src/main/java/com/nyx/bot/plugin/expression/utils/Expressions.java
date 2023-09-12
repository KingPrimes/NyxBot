package com.nyx.bot.plugin.expression.utils;

import com.nyx.bot.utils.image.DrawingImageUtils;
import com.nyx.bot.utils.image.ImageToGif;
import com.nyx.bot.utils.image.combiner.ImageCombiner;
import com.nyx.bot.utils.image.combiner.enums.OutputFormat;
import com.nyx.bot.utils.image.jhlabs.image.PerspectiveFilter;
import com.nyx.bot.utils.image.jhlabs.image.RotateFilter;
import com.nyx.bot.utils.image.jhlabs.image.ScaleFilter;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Expressions {


    /**
     * 检查表情文件是否存在
     *
     * @return 不存在true
     */
    static boolean exists() {
        File file = new File("./emoji_file");
        return !file.exists();
    }

    /**
     * 获取排序完成的文件列表
     *
     * @param t 文件目录
     * @return 文件列表
     */
    static List<File> gitFiles(File t) {
        File[] names = t.listFiles();
        return Arrays.stream(names).sorted((o1, o2) -> {
            if (Integer.parseInt(o1.getName().replaceAll(".png", "")) > Integer.parseInt(o2.getName().replaceAll(".png", ""))) {
                return 1;
            } else {
                return -1;
            }
        }).toList();
    }

    /**
     * Capoo-Ding 表情
     *
     * @param avatar 头像
     * @param ms     每帧间隔-毫秒
     * @return 字节流
     * @throws Exception Exception
     */
    public static ByteArrayOutputStream capooDing(BufferedImage avatar, int ms) throws Exception {
        if (exists()) {
            return null;
        }
        //图像组
        List<BufferedImage> gif = new ArrayList<>();
        //对用户头像圆角处理
        ImageCombiner ic = new ImageCombiner(avatar, OutputFormat.PNG);
        ic.setCanvasRoundCorner(200);
        avatar = ic.combine();
        //表情包目录
        File ding = new File("./emoji_file/capoo/ding");
        //获取排序完成之后的文件列表
        List<File> files = gitFiles(ding);
        //绘制Gif的每一帧
        for (int i = 0; i < files.size(); i++) {
            BufferedImage read = ImageIO.read(files.get(i));
            ImageCombiner combiner = new ImageCombiner(new BufferedImage(read.getWidth(), read.getHeight(), BufferedImage.TYPE_INT_ARGB), OutputFormat.PNG);
            PerspectiveFilter filter = new PerspectiveFilter();
            switch (i + 1) {
                case 1, 13, 25 -> {
                    filter.setCorners(
                            184,
                            63,
                            277,
                            63,
                            277,
                            155,
                            184,
                            155);
                    combiner.addImageElement(filter.filter(avatar), 184, 63);
                }
                case 2, 14, 26 -> {
                    filter.setCorners(
                            186,
                            77.7F,
                            277,
                            77.7F,
                            277,
                            169.7F,
                            186,
                            169.7F
                    );
                    combiner.addImageElement(filter.filter(avatar), 186, 77);
                }
                case 3, 15, 27 -> {
                    filter.setCorners(
                            185.9F,
                            100,
                            279,
                            100,
                            279,
                            194,
                            185.9F,
                            194
                    );
                    combiner.addImageElement(filter.filter(avatar), 185, 100);
                }
                case 4, 16, 28 -> {
                    combiner.addImageElement(
                            Thumbnails.of(avatar)
                                    .scale(1)
                                    .asBufferedImage()
                            , 185
                            , 112);
                }
                case 5, 17, 29 -> {
                    filter.setCorners(
                            159,
                            196,
                            304,
                            196,
                            304,
                            238,
                            159,
                            238);
                    combiner.addImageElement(filter.filter(avatar), 159, 196);
                }
                case 6, 18, 30 -> {
                    filter.setCorners(
                            180,
                            137,
                            296,
                            137,
                            296,
                            202,
                            180,
                            196);
                    combiner.addImageElement(filter.filter(avatar), 180, 137);
                }
                case 7, 19, 31 -> {
                    filter.setCorners(
                            177,
                            69,
                            294,
                            69,
                            294,
                            147,
                            177,
                            147);
                    combiner.addImageElement(filter.filter(avatar), 177, 69);
                }
                case 8, 20, 32 -> {
                    filter.setCorners(
                            172,
                            45,
                            296,
                            45,
                            296,
                            135,
                            172,
                            135);
                    combiner.addImageElement(filter.filter(avatar), 172, 45);
                }
                case 9, 21, 33 -> {
                    filter.setCorners(
                            176,
                            36.5F,
                            291,
                            36.5F,
                            291,
                            126,
                            176,
                            126);
                    combiner.addImageElement(filter.filter(avatar), 176, 36);
                }
                case 10, 22, 34 -> {
                    filter.setCorners(
                            181,
                            38,
                            286,
                            38,
                            286,
                            124,
                            181,
                            124);
                    combiner.addImageElement(filter.filter(avatar), 181, 38);
                }
                case 11, 23, 35 -> {
                    filter.setCorners(
                            183,
                            57,
                            280,
                            57,
                            280,
                            143,
                            183,
                            143);
                    combiner.addImageElement(filter.filter(avatar), 183, 57);
                }
                case 12, 24, 36 -> {
                    filter.setCorners(
                            185,
                            60,
                            277,
                            60,
                            277,
                            147,
                            185,
                            147);
                    combiner.addImageElement(filter.filter(avatar), 185, 60);
                }
                case 37 -> {
                    filter.setCorners(
                            185,
                            72,
                            271,
                            72,
                            271,
                            164,
                            185,
                            164);
                    combiner.addImageElement(filter.filter(avatar), 185, 72);
                }
                case 38 -> {
                    filter.setCorners(
                            183,
                            134,
                            268,
                            134,
                            268,
                            289,
                            183,
                            289);
                    combiner.addImageElement(filter.filter(avatar), 183, 134);
                }
            }
            //将表情添加到最顶层
            combiner.addImageElement(read, 0, 0);
            //合成图片添加到GIF帧组中
            gif.add(combiner.combine());
        }
        //判断是否设置了间隔时间
        if (ms > 0) {
            return ImageToGif.imagesToGif(gif, ms);
        } else {
            return ImageToGif.imagesToGif(gif);
        }
    }

    /**
     * Capoo-T 表情
     *
     * @param avatar 头像
     * @param ms     每帧间隔-毫秒
     * @return 字节流
     * @throws Exception Exception
     */
    public static ByteArrayOutputStream capooT(BufferedImage avatar, int ms) throws Exception {
        if (exists()) {
            return null;
        }
        //图像组
        List<BufferedImage> gif = new ArrayList<>();
        //对用户头像进行圆角处理
        ImageCombiner ic = new ImageCombiner(avatar, OutputFormat.PNG);
        ic.setCanvasRoundCorner(200);
        avatar = ic.combine();
        //表情包目录
        File t = new File("./emoji_file/capoo/t");
        //获取排序完成之后的文件列表
        List<File> files = gitFiles(t);
        //绘制Gif的每一帧
        for (int i = 0; i < files.size(); i++) {
            BufferedImage read = ImageIO.read(files.get(i));
            ImageCombiner combiner = new ImageCombiner(new BufferedImage(read.getWidth(), read.getHeight(), BufferedImage.TYPE_INT_ARGB), OutputFormat.PNG);
            PerspectiveFilter filter = new PerspectiveFilter();
            switch (i + 1) {
                case 1 -> {
                    filter.setCorners(
                            137,
                            242,
                            269,
                            242,
                            269,
                            285,
                            137,
                            285
                    );
                    combiner.addImageElement(filter.filter(avatar), 137, 242);
                }
                case 2 -> {
                    filter.setCorners(
                            137,
                            245,
                            269,
                            245,
                            269,
                            285,
                            137,
                            285
                    );
                    combiner.addImageElement(filter.filter(avatar), 137, 245);
                }
                case 3, 4, 5 -> {
                    filter.setCorners(
                            151,
                            191,
                            253,
                            191,
                            253,
                            283,
                            151,
                            283
                    );
                    combiner.addImageElement(filter.filter(avatar), 151, 191);
                }
                case 6 -> {
                    filter.setCorners(
                            147,
                            198,
                            254,
                            198,
                            254,
                            283,
                            147,
                            283
                    );
                    combiner.addImageElement(filter.filter(avatar), 147, 198);
                }
                case 7, 8 -> {
                    filter.setCorners(
                            147,
                            225,
                            254,
                            225,
                            254,
                            283,
                            147,
                            283
                    );
                    combiner.addImageElement(filter.filter(avatar), 147, 225);
                }
            }
            //将表情添加到最顶层
            combiner.addImageElement(read, 0, 0);
            //合成图片添加到GIF帧组中
            gif.add(combiner.combine());
        }
        //判断是否设置了间隔时间
        if (ms > 0) {
            return ImageToGif.imagesToGif(gif, ms);
        } else {
            return ImageToGif.imagesToGif(gif);
        }
    }


    /**
     * 转圈滑稽
     *
     * @param avatar 用户头像
     * @param ms     每帧间隔
     * @return 字节流
     * @throws Exception Exception
     */
    public static ByteArrayOutputStream emailFunny(BufferedImage avatar, int ms) throws Exception {
        if (exists()) {
            return null;
        }
        //图像组
        List<BufferedImage> gif = new ArrayList<>();
        //对用户头像进行圆角处理
        ImageCombiner ic = new ImageCombiner(avatar, OutputFormat.PNG);
        ic.setCanvasRoundCorner(200);
        avatar = ic.combine();
        //表情包目录
        File t = new File("./emoji_file/email-funny");
        //获取排序完成之后的文件列表
        List<File> files = gitFiles(t);
        int x = 0;
        for (File file : files) {
            BufferedImage read = ImageIO.read(file);
            ImageCombiner combiner = new ImageCombiner(new BufferedImage(read.getWidth(), read.getHeight(), BufferedImage.TYPE_INT_ARGB), OutputFormat.PNG);
            //写头像
            combiner.addImageElement(
                    //缩放图像
                    new ScaleFilter(80, 80)
                            //处理图像
                            .filter(
                                    //旋转图像
                                    DrawingImageUtils.rotateImage(avatar, x)), 110, 80);
            //将表情添加到最顶层
            combiner.addImageElement(read, 0, 0);
            //合成图片添加到GIF帧组中
            gif.add(combiner.combine());
            x += 22;
        }
        //判断是否设置了间隔时间
        if (ms > 0) {
            return ImageToGif.imagesToGif(gif, ms);
        } else {
            return ImageToGif.imagesToGif(gif);
        }
    }


    /**
     * 精神支柱 表情包
     *
     * @param avatar 用户头像
     * @return 字节流
     * @throws Exception Exception
     */
    public static ByteArrayOutputStream spiritualPillars(BufferedImage avatar) throws Exception {
        if (exists()) {
            return null;
        }
        RotateFilter filter = new RotateFilter();
        filter.setAngle(0.40F);
        ImageCombiner combiner = new ImageCombiner(Objects.requireNonNull(ImageIO.read(new File("./emoji_file/emo-supt/emo.png"))), OutputFormat.PNG);
        combiner.addImageElement(
                Thumbnails.of(filter.filter(avatar)).size(1600, 1600).keepAspectRatio(false).asBufferedImage(),
                -145,
                -5
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(combiner.combine()).scale(1).outputFormat("png").toOutputStream(out);
        return out;
    }
}
