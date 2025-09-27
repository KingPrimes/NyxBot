package com.nyx.bot.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("all")
@Slf4j
public class FileUtils {
    //读取文件到字符串
    public static String readFileToString(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return "";
        }
        byte[] b = new byte[0];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            int len = inputStream.available();
            b = new byte[len];
            inputStream.read(b);
        } catch (Exception e) {
            log.error("读取文件错误:{}", e.getMessage());
        }
        return new String(b, StandardCharsets.UTF_8);
    }

    /**
     * 读取文本到列表
     * @param path 文件路径
     * @return 列表
     */
    public static List<String> readFileToList(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return List.of();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().toList();
        } catch (Exception e) {
            log.error("读取文件错误:{}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 创建目录如果目录不存在
     *
     * @param file 文件
     */
    public static void createDir(File file) {
        if (!file.exists() || !file.isDirectory()) {
            File dir = file.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    /**
     * 写入文件
     *
     * @param fileName 文件路径+文件名字
     * @param content  要写入的内容
     */
    public static void writeFile(String fileName, String content) {
        Path path = Paths.get(fileName);
        try {
            //判断目录是否存在不存在则创建
            File file = new File(fileName);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("写入文件错误:{}", e.getMessage());
        }
    }

    /**
     * 写入文件
     *
     * @param bytes 字节数组
     * @param path  文件路径
     */
    public static boolean writeToFile(byte[] bytes, String path) {
        try {
            //判断目录是否存在不存在则创建
            File file = new File(path);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            Path write = Files.write(Paths.get(path), bytes);
            return Files.size(write) == bytes.length;
        } catch (IOException e) {
            log.error("写入文件失败:{}", e.getMessage());
            return false;
        }
    }

    /**
     * 写入文件
     *
     * @param fileName 文件路径+文件名字
     * @param content  要写入的内容
     */
    public static void writeFile(String fileName, byte[] content) {
        File file = new File(fileName);
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(content);
        } catch (Exception e) {
            log.error("写入文件错误:{}", e.getMessage());
        }
    }

    /**
     * 复制文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @throws IOException IOException
     */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        if (!destFile.exists()) {
            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

    }

    /**
     * 复制文件
     *
     * @param in       输入流
     * @param destFile 目标文件
     * @throws IOException IOException
     */
    public static void copyFile(InputStream in, File destFile) throws IOException {
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        if (!destFile.exists()) {
            Files.copy(in, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

    }

    /**
     * 删除文件
     *
     * @param filePath 文件
     * @return 是否删除
     */
    public static boolean deleteFile(String filePath) {
        boolean flag;
        File file = new File(filePath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            flag = file.delete();
        } else {
            return true;
        }
        return flag;
    }

    /**
     * 获取目录下所有的文件名
     *
     * @param path 路径
     * @return 文件名数组
     */
    public static Optional<List<String>> getFilesName(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            return Optional.of(Arrays.stream(Objects.requireNonNull(file.list())).toList());
        }
        return Optional.empty();
    }

    /**
     * 删除指定文件夹下所有内容并删除该文件夹
     *
     * @param path 文件夹路径
     * @return 是否成功删除文件夹
     */
    public static boolean delAllFile(String path) {
        File file = new File(path);
        // 路径不存在直接返回成功
        if (!file.exists()) {
            return true;
        }
        // 不是目录返回失败
        if (!file.isDirectory()) {
            return false;
        }

        // 获取目录下所有文件/子目录
        File[] files = file.listFiles();
        if (files != null) {
            for (File childFile : files) {
                if (childFile.isFile()) {
                    // 删除文件
                    childFile.delete();
                } else if (childFile.isDirectory()) {
                    // 递归删除子目录（会删除子目录内容及本身）
                    delAllFile(childFile.getAbsolutePath());
                }
            }
        }

        // 最后删除当前空文件夹
        return file.delete();
    }


}
