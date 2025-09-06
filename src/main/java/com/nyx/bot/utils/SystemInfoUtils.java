package com.nyx.bot.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;

@Slf4j
public class SystemInfoUtils {

    private static final int OSHI_WAIT_SECOND = 1000;
    private static final SystemInfo systemInfo = new SystemInfo();
    private static final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private static final OperatingSystem operatingSystem = systemInfo.getOperatingSystem();

    public static JSONObject getCpuInfo() {
        JSONObject cpuInfo = new JSONObject();
        CentralProcessor processor = hardware.getProcessor();
        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(OSHI_WAIT_SECOND);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        //cpu核数
        cpuInfo.put("cpuNum", processor.getLogicalProcessorCount());
        //cpu系统使用率
        cpuInfo.put("cSys", new DecimalFormat("#.##%").format(cSys * 1.0 / totalCpu));
        //cpu用户使用率
        cpuInfo.put("user", new DecimalFormat("#.##%").format(user * 1.0 / totalCpu));
        //cpu当前等待率
        cpuInfo.put("iowait", new DecimalFormat("#.##%").format(iowait * 1.0 / totalCpu));
        //cpu当前使用率
        cpuInfo.put("idle", new DecimalFormat("#.##%").format(1.0 - (idle * 1.0 / totalCpu)));
        return cpuInfo;
    }

    /**
     * 系统jvm信息
     */
    public static JSONObject getJvmInfo() {
        JSONObject cpuInfo = new JSONObject();
        Properties props = System.getProperties();
        Runtime runtime = Runtime.getRuntime();
        long jvmTotalMemoryByte = runtime.totalMemory();
        long freeMemoryByte = runtime.freeMemory();
        //jvm总内存
        cpuInfo.put("total", formatByte(runtime.totalMemory()));
        //空闲空间
        cpuInfo.put("free", formatByte(runtime.freeMemory()));
        //jvm最大可申请
        cpuInfo.put("max", formatByte(runtime.maxMemory()));
        //vm已使用内存
        cpuInfo.put("user", formatByte(jvmTotalMemoryByte - freeMemoryByte));
        //jvm内存使用率
        cpuInfo.put("usageRate", new DecimalFormat("#.##%").format((jvmTotalMemoryByte - freeMemoryByte) * 1.0 / jvmTotalMemoryByte));
        //jdk版本
        cpuInfo.put("jdkVersion", props.getProperty("java.version"));
        //jdk路径
        cpuInfo.put("jdkHome", props.getProperty("java.home"));
        return cpuInfo;
    }

    /**
     * 系统内存信息
     */
    public static JSONObject getMemInfo() {
        JSONObject cpuInfo = new JSONObject();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        //总内存
        long totalByte = memory.getTotal();
        //剩余
        long acaliableByte = memory.getAvailable();
        //总内存
        cpuInfo.put("total", formatByte(totalByte));
        //使用
        cpuInfo.put("used", formatByte(totalByte - acaliableByte));
        //剩余内存
        cpuInfo.put("free", formatByte(acaliableByte));
        //使用率
        cpuInfo.put("usageRate", new DecimalFormat("#.##%").format((totalByte - acaliableByte) * 1.0 / totalByte));
        return cpuInfo;
    }

    /**
     * 系统盘符信息
     */
    public static JSONArray getSysFileInfo() {
        JSONObject cpuInfo;
        JSONArray sysFiles = new JSONArray();
        FileSystem fileSystem = operatingSystem.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        //限制获取盘符
        if (fsArray.size() >= 10) {
            fsArray = fsArray.subList(0, 10);
        }
        for (OSFileStore fs : fsArray) {
            cpuInfo = new JSONObject();
            //盘符路径
            cpuInfo.put("dirName", fs.getMount());
            //盘符类型
            cpuInfo.put("sysTypeName", fs.getType());
            //文件类型
            cpuInfo.put("typeName", fs.getName());
            //总大小
            cpuInfo.put("total", formatByte(fs.getTotalSpace()));
            //剩余大小
            cpuInfo.put("free", formatByte(fs.getUsableSpace()));
            //已经使用量
            cpuInfo.put("used", formatByte(fs.getTotalSpace() - fs.getUsableSpace()));
            if (fs.getTotalSpace() == 0) {
                //资源的使用率
                cpuInfo.put("usage", 0);
            } else {
                cpuInfo.put("usage", new DecimalFormat("#.##%").format((fs.getTotalSpace() - fs.getUsableSpace()) * 1.0 / fs.getTotalSpace()));
            }
            sysFiles.add(cpuInfo);
        }
        return sysFiles;
    }

    /**
     * 系统信息
     */
    public static JSONObject getSysInfo() throws UnknownHostException {
        JSONObject cpuInfo = new JSONObject();
        Properties props = System.getProperties();
        //操作系统名
        cpuInfo.put("osName", props.getProperty("os.name"));
        //系统架构
        cpuInfo.put("osArch", props.getProperty("os.arch"));
        //服务器名称
        cpuInfo.put("computerName", InetAddress.getLocalHost().getHostName());
        //服务器Ip
        cpuInfo.put("computerIp", InetAddress.getLocalHost().getHostAddress());
        //项目路径
        cpuInfo.put("userDir", props.getProperty("user.dir"));
        return cpuInfo;
    }

    /**
     * 所有系统信息
     */
    public static JSONObject getInfo() throws UnknownHostException {
        JSONObject info = new JSONObject();
        info.put("cpuInfo", getCpuInfo());
        info.put("jvmInfo", getJvmInfo());
        info.put("memInfo", getMemInfo());
        info.put("sysInfo", getSysInfo());
        info.put("sysFileInfo", getSysFileInfo());
        info.put("jarVersion", getJarVersion());
        return info;
    }

    /**
     * 获取当前jar包版本
     *
     * @return 版本号
     */
    public static String getJarVersion() {
        //return Objects.requireNonNull(JarManifest.manifestFromClasspath()).getMainAttributes().getValue("version");
        String version = SystemInfoUtils.class
                .getPackage()
                .getImplementationVersion();
        if (version != null && !version.isEmpty()) {
            return version;
        }
        // 1. 优先尝试从JAR包Manifest获取（生产环境）
        try {
            Manifest manifest = JarManifest.manifestFromClasspath();
            if (manifest != null) {
                version = manifest.getMainAttributes().getValue("version");
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (Exception e) {
            // 非JAR环境（如IDE运行）会进入此处，忽略异常继续尝试
        }

        // 2. IDE运行时直接解析pom.xml获取版本（开发环境）
        try {
            // 定位项目根目录下的pom.xml（IDEA运行时working directory通常为项目根目录）
            File pomFile = new File("pom.xml");
            if (pomFile.exists() && pomFile.isFile()) {
                // 使用JDK内置XML解析器读取pom.xml
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(pomFile);
                doc.getDocumentElement().normalize();

                // 使用XPath精确定位项目根节点下的version标签
                XPath xPath = XPathFactory.newInstance().newXPath();
                XPathExpression expr = xPath.compile("/project/version"); // 直接定位项目自身版本
                Node versionNode = (Node) expr.evaluate(doc, XPathConstants.NODE);

                if (versionNode != null) {
                    String projectVersion = versionNode.getTextContent().trim();
                    if (!projectVersion.isEmpty()) {
                        return projectVersion;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to read version from pom.xml", e);
        }

        // 3. 所有方式失败时返回默认值
        return "unknown-version";
    }

    /**
     * 单位转换
     */
    private static String formatByte(long byteNumber) {
        //换算单位
        double FORMAT = 1024.0;
        double kbNumber = byteNumber / FORMAT;
        if (kbNumber < FORMAT) {
            return new DecimalFormat("#.##KB").format(kbNumber);
        }
        double mbNumber = kbNumber / FORMAT;
        if (mbNumber < FORMAT) {
            return new DecimalFormat("#.##MB").format(mbNumber);
        }
        double gbNumber = mbNumber / FORMAT;
        if (gbNumber < FORMAT) {
            return new DecimalFormat("#.##GB").format(gbNumber);
        }
        double tbNumber = gbNumber / FORMAT;
        return new DecimalFormat("#.##TB").format(tbNumber);
    }

}
