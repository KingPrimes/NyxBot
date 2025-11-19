package com.nyx.bot.utils;

import io.github.kingprimes.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;

@Slf4j
public class SystemInfoUtils {

    private static final int OSHI_WAIT_SECOND = 1000;
    private static final SystemInfo systemInfo = new SystemInfo();
    private static final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private static final OperatingSystem operatingSystem = systemInfo.getOperatingSystem();

    /**
     * 获取CPU信息
     *
     * @return CpuInfo对象，包含CPU的详细信息，如型号、核心数、频率和各种使用率
     */
    public static CpuInfo getCpuInfo() {
        CpuInfo cpuInfo = new CpuInfo();
        CentralProcessor processor = hardware.getProcessor();
        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(OSHI_WAIT_SECOND);
        long[] ticks = processor.getSystemCpuLoadTicks();

        // 计算各类CPU时间片差值
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long wait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + wait + irq + softirq + steal;
        // CPU信息
        cpuInfo.setModel(processor.getProcessorIdentifier().getName());
        // CPU核数
        cpuInfo.setCores(processor.getLogicalProcessorCount());
        // CPU频率
        cpuInfo.setFrequency(processor.getMaxFreq());
        // CPU使用率
        cpuInfo.setUserUsage(Double.parseDouble(new DecimalFormat("#.##%").format(user * 1.0 / totalCpu)));
        // CPU当前等待率
        cpuInfo.setWaitUsage(Double.parseDouble(new DecimalFormat("#.##%").format(wait * 1.0 / totalCpu)));
        // CPU系统使用率
        cpuInfo.setSysUsage(Double.parseDouble(new DecimalFormat("#.##%").format(cSys * 1.0 / totalCpu)));
        // CPU当前使用率
        cpuInfo.setIdleUsage(Double.parseDouble(new DecimalFormat("#.##%").format(idle * 1.0 / totalCpu)));
        return cpuInfo;
    }

    /**
     * 系统jvm信息
     */
    public static JvmInfo getJvmInfo() {
        JvmInfo jvmInfo = new JvmInfo();
        Properties props = System.getProperties();
        Runtime runtime = Runtime.getRuntime();
        long jvmTotalMemoryByte = runtime.totalMemory();
        long freeMemoryByte = runtime.freeMemory();
        //jvm总内存
        jvmInfo.setMaxMemory(runtime.totalMemory());
        //空闲空间
        jvmInfo.setFreeMemory(runtime.freeMemory());
        //jvm最大可申请
        jvmInfo.setMaxMemory(runtime.maxMemory());
        //vm已使用内存
        jvmInfo.setUsedMemory(jvmTotalMemoryByte - freeMemoryByte);
        //jvm内存使用率
        jvmInfo.setUsedMemoryRatio((jvmTotalMemoryByte - freeMemoryByte) * 1.0 / jvmTotalMemoryByte);
        // jvm 空闲内存占比
        jvmInfo.setFreeMemoryRatio(freeMemoryByte * 1.0 / jvmTotalMemoryByte);
        //jdk版本
        jvmInfo.setVersion(props.getProperty("java.version"));
        return jvmInfo;
    }

    /**
     * 系统内存信息
     */
    public static MemInfo getMemInfo() {
        MemInfo memInfo = new MemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        //总内存
        long totalByte = memory.getTotal();
        //剩余
        long acaliableByte = memory.getAvailable();
        //总内存
        memInfo.setTotalMemory(totalByte);
        // 已使用内存
        memInfo.setUsedMemory(totalByte - acaliableByte);
        // 剩余内存
        memInfo.setFreeMemory(acaliableByte);
        //使用率
        memInfo.setUsedMemoryRatio(Double.parseDouble(new DecimalFormat("#.##%").format((totalByte - acaliableByte) * 1.0 / totalByte)));
        // 剩余内存占比
        memInfo.setFreeMemoryRatio(Double.parseDouble(new DecimalFormat("#.##%").format(acaliableByte * 1.0 / totalByte)));
        return memInfo;
    }

    /**
     * 系统盘符信息
     */
    public static SysFileInfos getSysFileInfo() {
        SysFileInfos sysFileInfos = new SysFileInfos();
        List<SysFileInfos.SysFileInfo> sysFiles = new ArrayList<>();
        FileSystem fileSystem = operatingSystem.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        //限制获取盘符
        if (fsArray.size() >= 4) {
            fsArray = fsArray.subList(0, 4);
        }
        for (OSFileStore fs : fsArray) {
            SysFileInfos.SysFileInfo fileInfo = new SysFileInfos.SysFileInfo();
            //盘符路径
            fileInfo.setDirName(fs.getMount());
            //盘符类型
            fileInfo.setTypeName(fs.getType());
            //文件类型
            fileInfo.setFileType(fs.getName());
            //总大小
            fileInfo.setTotal(fs.getTotalSpace());
            //已经使用量
            fileInfo.setUsed(fs.getUsableSpace());

            sysFiles.add(fileInfo);
        }
        sysFileInfos.setSysFileInfos(sysFiles);
        return sysFileInfos;
    }

    /**
     * 系统信息
     */
    public static io.github.kingprimes.model.SystemInfo getSysInfo() throws UnknownHostException {
        io.github.kingprimes.model.SystemInfo systemInfo = new io.github.kingprimes.model.SystemInfo();
        Properties props = System.getProperties();
        //操作系统名
        systemInfo.setOsName(props.getProperty("os.name"));
        //系统架构
        systemInfo.setOsArch(props.getProperty("os.arch"));
        //服务器名称
        systemInfo.setComputerName(props.getProperty("computerName"));
        //服务器Ip
        systemInfo.setComputerIp(InetAddress.getLocalHost().getHostAddress());
        return systemInfo;
    }

    /**
     * 所有系统信息
     */
    public static AllInfo getInfo() throws UnknownHostException {
        return new AllInfo()
                .setCpuInfo(getCpuInfo())
                .setJvmInfo(getJvmInfo())
                .setSystemInfo(getSysInfo())
                .setSysFileInfos(getSysFileInfo())
                .setPackageVersion(new AllInfo.PackageVersion("NyxBot", getJarVersion()));
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
