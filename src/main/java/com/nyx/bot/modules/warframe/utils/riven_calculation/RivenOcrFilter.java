package com.nyx.bot.modules.warframe.utils.riven_calculation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * OCR 结果过滤器 — 从粗糙的 OCR 输出中提取紫卡相关的有效文本
 */
public final class RivenOcrFilter {

    /**
     * 含中文 + 可选英文的行（武器名 + 紫卡名）
     */
    private static final Pattern WEAPON_RIVEN_LINE =
            Pattern.compile("[一-龥]+.*[A-Za-z]+.*|[一-龥]{2,}");

    /**
     * 属性行：数值 + 中文
     */
    private static final Pattern ATTR_VALUE =
            Pattern.compile("[+-]?\\d+(\\.\\d+)?%?");

    /**
     * 属性行：含中文描述
     */
    private static final Pattern ATTR_CHINESE =
            Pattern.compile("[一-龥]{2,}");

    /**
     * 噪音：纯数字/段位/短字母/标点/单字
     */
    private static final Pattern NOISE = Pattern.compile(
            "^(\\d{1,3}([\\s-]\\d{1,3})?|段位\\d*|[A-Za-z]?[\\p{Punct}]*[A-Za-z]?|×\\d+)$");

    /**
     * 连字符碎片：可能在属性断行处
     */
    private static final Pattern FRAGMENT =
            Pattern.compile("^[时是]?[x×X]\\d*\\]?\\)?\\)?$|^\\).*");

    /**
     * 元素图标被 OCR 误识别的残留符号
     */
    private static final Pattern ICON_ARTIFACT = Pattern.compile("[*＞><∣|★☆●○◆◇▲△▼▽♢♤♧♡]");

    /**
     * 残留在数值和中文之间的半角括号
     */
    private static final Pattern STRAY_PAREN = Pattern.compile("(?<=[%\\d])\\s*[（(](?=[一-龥])");

    /**
     * OCR 常将相邻的标点符号吸入文本区域尾部，如 "fevacron,"、"段位14."
     */
    private static final Pattern TRAILING_PUNCT = Pattern.compile("[,\\.;:，。；：]+$");

    /**
     * 过滤 OCR 原始文本，仅保留武器名、紫卡名、属性词条。
     * 同时清除元素图标误识别的残留符号，尝试合并被换行拆分的属性。
     */
    public static List<String> clean(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();

        // 1. 过滤明显噪音 + 清除图标残留
        List<String> filtered = raw.stream()
                .map(String::trim)
                .filter(s -> s.length() >= 2)
                .filter(s -> !NOISE.matcher(s).matches())
                .map(s -> ICON_ARTIFACT.matcher(s).replaceAll(""))
                .map(s -> STRAY_PAREN.matcher(s).replaceFirst(""))
                .map(s -> TRAILING_PUNCT.matcher(s).replaceFirst(""))
                .map(String::trim)
                .filter(s -> s.length() >= 2)
                .toList();

        // 2. 合并断行 + 仅保留有效行
        List<String> result = new ArrayList<>();
        StringBuilder buf = new StringBuilder();

        for (String line : filtered) {
            boolean hasAttrVal = ATTR_VALUE.matcher(line).find();
            boolean hasChinese = ATTR_CHINESE.matcher(line).find();
            boolean isFragment = FRAGMENT.matcher(line).matches();

            // 再次对属性行做图标清理
            String cleanLine = hasAttrVal ? ICON_ARTIFACT.matcher(line).replaceAll("") : line;
            cleanLine = STRAY_PAREN.matcher(cleanLine).replaceFirst("");
            cleanLine = TRAILING_PUNCT.matcher(cleanLine).replaceFirst("");

            if (hasAttrVal && hasChinese) {
                if (!buf.isEmpty()) {
                    result.add(buf.toString());
                    buf.setLength(0);
                }
                result.add(cleanLine);
            } else if (isFragment) {
                // 断行碎片 → 追加到上一行
                if (!result.isEmpty()) {
                    int last = result.size() - 1;
                    result.set(last, result.get(last) + cleanLine);
                }
            } else if (hasChinese || WEAPON_RIVEN_LINE.matcher(cleanLine).matches()) {
                if (!buf.isEmpty()) {
                    result.add(buf.toString());
                    buf.setLength(0);
                }
                result.add(cleanLine);
            } else {
                buf.append(cleanLine);
            }
        }

        if (!buf.isEmpty()) {
            result.add(buf.toString());
        }

        return result;
    }
}
