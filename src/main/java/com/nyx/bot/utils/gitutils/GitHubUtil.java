package com.nyx.bot.utils.gitutils;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.Data;

import java.util.List;

public class GitHubUtil {


    /**
     * 获取最新版本
     *
     * @param repoName 用户名称
     * @param depot    仓库名称
     * @return Release
     */
    public static Release getReleasesLatestVersion(String repoName, String depot) {
        HttpUtils.Body latest = HttpUtils.sendGet("https://api.github.com/repos/" + repoName + "/" + depot + "/releases/latest");
        if (!latest.getCode().equals(HttpCodeEnum.SUCCESS)) {
            return null;
        }
        return JSON.parseObject(latest.getBody(), Release.class);
    }

    /**
     * 获取最新版本
     *
     * @return Release
     */
    private static Release getReleasesLatestVersion() {
        return getReleasesLatestVersion("KingPrimes", "NyxBot");
    }


    /**
     * 获取最新下载地址
     *
     * @return String
     */
    public static String getLatestDownLoadUrl() {
        return getReleasesLatestVersion().getAssets().get(0).getBrowserDownloadUrl();
    }

    /**
     * 获取最新版本号
     *
     * @return String
     */
    public static String getLatestTagName() {
        return getReleasesLatestVersion().getTagName();
    }

    /**
     * 判断是否为最新版本
     *
     * @param version 版本号
     * @return Boolean
     */
    public static Boolean isLatestVersion(String version) {
        return getLatestTagName().equals("v" + version);
    }

    /**
     * 获取最新版本压缩包
     *
     * @return Boolean 是否完成
     */
    public static Boolean getLatestZip(String path) {
        return HttpUtils.sendGetForFile(getLatestDownLoadUrl(), path);
    }

    /**
     * 获取最新版本更新日志
     *
     * @return String
     */
    public static String getBody() {
        return getReleasesLatestVersion().getBody();
    }

    @Data
    public static class Release {
        @JsonProperty("assets")
        // 资产
        List<Assets> assets;
        @JsonProperty("assets_url")
        // 资产
        String assetsUrl;
        @JsonProperty("author")
        // 作者
        Uploader author;
        @JsonProperty("body")
        // 留言
        String body;
        @JsonProperty("created_at")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        // 创建于
        String createdAt;
        @JsonProperty("draft")
        // 草案
        Boolean draft;
        @JsonProperty("html_url")
        //
        String htmlUrl;
        @JsonProperty("id")
        // ID
        Long id;
        @JsonProperty("name")
        // 名称
        String name;
        @JsonProperty("node_id")
        // nodeId
        String nodeId;
        @JsonProperty("prerelease")
        //
        Boolean prerelease;
        @JsonProperty("published_at")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        // 出版于
        String publishedAt;
        @JsonProperty("tag_name")
        // TagName 版本
        String tagName;
        @JsonProperty("tarball_url")
        // 源文件压缩包网址
        String tarballUrl;
        @JsonProperty("target_commitish")
        // 提交目标
        String targetCommitish;
        @JsonProperty("upload_url")
        // 上传网址
        String uploadUrl;
        @JsonProperty("url")
        // 地址
        String url;
        @JsonProperty("zipball_url")
        // 源码压缩包 ZIP格式
        String zipballUrl;

        @Data
        public static class Assets {
            @JsonProperty("id")
            // ID
            Long id;
            @JsonProperty("node_id")
            // nodeId
            String nodeId;
            @JsonProperty("url")
            // url
            String url;
            @JsonProperty("name")
            // 文件名称
            String name;
            @JsonProperty("label")
            // 标签
            String label;
            @JsonProperty("uploader")
            // 上传者
            Uploader uploader;
            @JsonProperty("content_type")
            //内容类型
            String contentType;
            @JsonProperty("state")
            // 类型
            String state;
            @JsonProperty("size")
            // 文件大小
            Long size;
            @JsonProperty("download_count")
            // 下载计数
            Long downloadCount;
            @JsonProperty("created_at")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
            // 创建于
            String createdAt;
            @JsonProperty("updated_at")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
            // 更新在
            String updateAt;
            @JsonProperty("browser_download_url")
            // 附件下载地址
            String browserDownloadUrl;
        }

        @Data
        public static class Uploader {
            @JsonProperty("id")
            //id
            Long id;
            @JsonProperty("node_id")
            //nodeId
            String nodeId;
            @JsonProperty("url")
            //url
            String url;
            @JsonProperty("login")
            //登录用户
            String login;
            @JsonProperty("avatar_url")
            //头像网址
            String avatarUrl;
            @JsonProperty("gravatar_id")
            //gravatarId
            String gravatarId;
            @JsonProperty("html_url")
            //htmlUrl
            String htmlUrl;
            @JsonProperty("followers_url")
            //
            String followersUrl;
            @JsonProperty("following_url")
            //
            String followingUrl;
            @JsonProperty("gists_url")
            //
            String gistsUrl;
            @JsonProperty("starred_url")
            //
            String starredUrl;
            @JsonProperty("subscriptions_url")
            //
            String subscriptionsUrl;
            @JsonProperty("organizations_url")
            //
            String organizationsUrl;
            @JsonProperty("repos_url")
            //
            String reposUrl;
            @JsonProperty("events_url")
            //
            String eventsUrl;
            @JsonProperty("received_events_url")
            // 事件网址
            String receivedEventsUrl;
            @JsonProperty("type")
            // 上传者类型
            String type;
            @JsonProperty("site_admin")
            // 是否管理员
            Boolean siteAdmin;
        }
    }

}
