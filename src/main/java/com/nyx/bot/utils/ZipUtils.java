package com.nyx.bot.utils;

import org.tukaani.xz.LZMAInputStream;

import java.io.*;

public class ZipUtils {

    /**
     * 解压LZMA文件
     *
     * @param inputPath  Lzma文件路径
     * @param outputPath 解压后的文件路径
     */
    public static Boolean unLzma(String inputPath, String outputPath) {

        try (InputStream fis = new BufferedInputStream(new FileInputStream(inputPath));
             LZMAInputStream lzmaIn = new LZMAInputStream(fis, -1);  // -1表示自动检测字典大小
             OutputStream fos = new BufferedOutputStream(new FileOutputStream(outputPath))) {

            byte[] buffer = new byte[8192];  // 使用8KB缓冲提升IO效率
            int bytesRead;
            while ((bytesRead = lzmaIn.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
