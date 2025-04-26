package com.nyx.bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
public class UpdateJarUtils {
    private static final ApplicationContext APP = SpringUtils.getApplicationContext();
    private static final String winRun = """
            java -jar %s
            """;
    private static final String macRun = """
              #!/bin/bash
              nohup java -jar %s >/dev/null 2>&1 &
            """;
    private static final String macUpdate = """
                #!/bin/bash
                cp %s ./backup/back_%s_%s
                cp -r ./tmp/* ./%s
                ./run.sh
            """;
    private static final String winUpdate = """
            @echo off
            @ping 127.0.0.1 -n 8 & copy /Y .\\ %s .\\backup\\back_%s_%s  & xcopy /s/e/y .\\tmp  .\\%s  & .\\run.bat
            """;
    private static final String linuxRun = """
            nohup java -jar %s >/dev/null 2>&1 &
            """;
    private static final String linuxUpdate = """
            #!/bin/bash
            sleep 8s
            cp ./%s ./backup/back_%s_%s
            cp -r ./tmp/* ./%s
            ./run.sh
            """;

    private static void writerScript(String fileName) throws IOException {
        String path = System.getProperty("user.dir");
        log.debug("备份更新作正在进行中！");
        log.debug("文件名：{} -- 文件路径：{}", fileName, path);
        File backup = new File(path + "/backup");
        if (!backup.exists()) {
            log.debug("备份目录 不存在，正在创建备份目录");
            backup.mkdirs();
        }
        log.debug("备份目录创建成功");
        if (SystemUtils.IS_OS_WINDOWS) {
            log.debug("OS Windows");
            log.debug("创建和编写run.bat");
            File run = new File(path + "/run.bat");
            FileWriter runWriter = new FileWriter(run);
            runWriter.write(winRun.formatted(fileName));
            runWriter.close();
            log.debug("已成功创建 run.bat 文件:{}", winRun.formatted(fileName));
            log.debug("创建和编写update.bat");
            File file = new File(path + "/update.bat");
            FileWriter writer = new FileWriter(file);
            String update = winUpdate.formatted(fileName, DateUtils.getDate(), fileName, fileName);
            writer.write(update);
            log.debug("已成功创建 update.bat 文件:{}", update);
            writer.close();
            Process exec = Runtime.getRuntime().exec("cmd /k start " + path + "/update.bat");
            log.debug("运行 update.bat PID:{}", exec.pid());
            int exitCode = SpringApplication.exit(APP, () -> 0);
            System.exit(exitCode);
        }
        if (SystemUtils.IS_OS_LINUX) {
            log.debug("OS Linux");
            log.debug("创建和编写 run.sh");
            File run = new File(path + "/run.sh");
            FileWriter runWriter = new FileWriter(run);
            String runTxt = linuxRun.formatted(fileName);
            runWriter.write(runTxt);
            runWriter.close();
            log.debug("已成功创建 run.sh 文件:{}", winRun.formatted(fileName));
            log.debug("创建和编写 update.sh");
            Runtime.getRuntime().exec("chmod +x " + run.getAbsolutePath());
            File update = new File(path + "/update.sh");
            FileWriter writer = new FileWriter(update);
            String builder = linuxUpdate.formatted(
                    fileName,
                    DateUtils.getDate(),
                    fileName,
                    fileName
            );
            writer.write(builder);
            writer.close();
            log.debug("已成功创建 update.bat 文件:{}", builder);
            Runtime.getRuntime().exec("chmod +x " + update.getAbsolutePath());
            Process exec = Runtime.getRuntime().exec("bash " + path + "/update.sh");
            log.debug("运行 update.sh PID:{}", exec.pid());
            int exitCode = SpringApplication.exit(APP, () -> 0);
            System.exit(exitCode);
        }
        // TODO: 2023/3/29 待测试 MAC 自动更新是否可执行
        if (SystemUtils.IS_OS_MAC) {
            log.debug("OS MAC");
            log.debug("创建和编写 run.sh");
            File run = new File(path + "/run.sh");
            FileWriter runWriter = new FileWriter(run);
            String runTxt = macRun.formatted(fileName);
            runWriter.write(runTxt);
            runWriter.close();
            log.debug("已成功创建 run.sh 文件:{}", winRun.formatted(fileName));
            log.debug("创建和编写 update.sh");

            Runtime.getRuntime().exec("chmod +x " + run.getAbsolutePath());

            // 创建 update.sh 脚本
            File update = new File(path + "/update.sh");
            FileWriter writer = new FileWriter(update);
            String builder = macUpdate.formatted(fileName, fileName, DateUtils.getDate(), fileName);
            writer.write(builder);
            writer.close();
            log.debug("已成功创建 update.bat 文件:{}", builder);
            Runtime.getRuntime().exec("chmod +x " + update.getAbsolutePath());
            Process exec = Runtime.getRuntime().exec("sh " + path + "/update.sh");
            log.debug("运行 update.sh PID:{}", exec.pid());
            int exitCode = SpringApplication.exit(APP, () -> 0);
            System.exit(exitCode);
        }

    }

    /**
     * 结束当前程序，并将当前程序备份到 backup 目录下
     * 然后从 tmp 目录下复制 jar 包到当前目录下，并重新启动程序
     *
     * @param fileName 文件名
     */
    public static void restartUpdate(String fileName) {
        try {
            writerScript(fileName);
        } catch (IOException e) {
            log.error("自动更新错误", e);
        }
    }
}
