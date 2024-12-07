package com.nyx.bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
public class UpdateJarUtils {
    private static final ApplicationContext APP = SpringUtils.getApplicationContext();

    @NotNull
    private static FileWriter getFileWriterForMac(String fileName, String path) throws IOException {
        // 创建 backup 目录
        File backup = new File(path + "/backup");
        if (!backup.exists()) {
            backup.mkdirs();
        }

        // 创建 run.sh 脚本
        File run = new File(path + "/run.sh");
        FileWriter runWriter = new FileWriter(run);
        runWriter.write("#!/bin/bash\n");
        runWriter.write("nohup java -jar " + fileName + " >/dev/null 2>&1 &");
        runWriter.close();
        // 使脚本可执行
        Runtime.getRuntime().exec("chmod +x " + run.getAbsolutePath());

        // 创建 update.sh 脚本
        File update = new File(path + "/update.sh");
        FileWriter writer = new FileWriter(update);
        writer.write("#!/bin/bash\n");
        writer.write("cp " + fileName + " ./backup/baka_" + DateUtils.getDate() + "_"
                + fileName + "\n");
        writer.write("cp -r ./tmp/* ./" + fileName + "\n");
        writer.write("./run.sh\n");
        // 使脚本可执行

        return writer;
    }

    @NotNull
    private static FileWriter getFileWriterForWindows(String fileName, String path) throws IOException {
        File backup = new File(path + "\\backup");
        if (!backup.exists()) {
            backup.mkdirs();
        }
        File run = new File(path + "\\run.bat");
        FileWriter runWriter = new FileWriter(run);
        runWriter.write("java -jar " + fileName + "\nexit");
        runWriter.close();

        File file = new File(path + "\\updata.bat");
        FileWriter writer = new FileWriter(file);
        String builder = "@echo off " +
                "@ping 127.0.0.1 -n 8 & copy /Y .\\" +
                fileName +
                " .\\backup\\baka_" +
                DateUtils.getDate() +
                "_" +
                fileName +
                " & xcopy /s/e/y .\\tmp  .\\" +
                fileName +
                " & .\\run.bat";
        writer.write(builder);
        return writer;
    }

    @NotNull
    private static FileWriter getWriterForLinux(String fileName, String path) throws IOException {
        File backup = new File(path + "/backup");
        if (!backup.exists()) {
            backup.mkdirs();
        }
        File run = new File(path + "/run.sh");
        FileWriter runWriter = new FileWriter(run);
        runWriter.write("nohup java -jar " + fileName + " >/dev/null 2>&1 &");
        runWriter.close();
        return getFileWriter(fileName, path);
    }

    @NotNull
    private static FileWriter getFileWriter(String fileName, String path) throws IOException {
        File file = new File(path + "/updata.sh");
        FileWriter writer = new FileWriter(file);
        String builder = "#!/bin/bash " +
                "sleep 8s " +
                "cp ./" +
                fileName +
                " ./backup/baka_" +
                DateUtils.getDate() +
                "_" +
                fileName +
                " " +
                "cp -r ./tmp/* ./" +
                fileName +
                " " +
                "./run.sh";
        writer.write(builder);
        return writer;
    }

    /**
     * 结束当前程序，并将当前程序备份到 backup 目录下
     * 然后从 tmp 目录下复制 jar 包到当前目录下，并重新启动程序
     *
     * @param fileName 文件名
     */
    public void restartUpdate(String fileName) {
        String path = System.getProperty("user.dir");
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                FileWriter writer = getFileWriterForWindows(fileName, path);
                writer.close();
                Runtime.getRuntime().exec("cmd /k start " + path + "\\updata.bat");
                int exitCode = SpringApplication.exit(APP, () -> 0);
                System.exit(exitCode);
            } catch (IOException e) {
                log.error(e.getMessage());
            }

        }
        if (SystemUtils.IS_OS_LINUX) {
            try {
                FileWriter writer = getWriterForLinux(fileName, path);
                writer.close();
                Runtime.getRuntime().exec("bash " + path + "/updata.sh");
                int exitCode = SpringApplication.exit(APP, () -> 0);
                System.exit(exitCode);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        // TODO: 2023/3/29 待测试 MAC 自动更新是否可执行
        if (SystemUtils.IS_OS_MAC) {
            try {
                FileWriter writer = getFileWriterForMac(fileName, path);
                writer.close();
                // 使脚本可执行
                Runtime.getRuntime().exec("chmod +x " + path + "/update.sh");
                // 执行 update.sh 脚本
                Runtime.getRuntime().exec("sh " + path + "/update.sh");
                int exitCode = SpringApplication.exit(APP, () -> 0);
                System.exit(exitCode);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
