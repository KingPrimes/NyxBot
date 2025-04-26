package com.nyx.bot.utils.gitutils;

import com.nyx.bot.entity.git.GitHubUserProvider;
import com.nyx.bot.repo.git.GitHubUserProviderRepository;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
public class JgitUtil {

    public static String lockPath = "./DataSource";

    private final CredentialsProvider provider;
    String urlPath;
    String localPath;

    public JgitUtil(String urlPath, String localPath, CredentialsProvider provider) {
        this.urlPath = urlPath;
        if (!localPath.isEmpty()) {
            this.localPath = localPath;
        } else {
            this.localPath = lockPath;
        }
        this.provider = provider;
    }

    /**
     * @param urlPath   远程仓库地址
     * @param localPath 本地仓库路径
     */
    public static JgitUtil Build(String urlPath, String localPath) {
        List<GitHubUserProvider> all = SpringUtils.getBean(GitHubUserProviderRepository.class).findAll();
        JgitUtil jgitUtil;
        if (!all.isEmpty()) {
            GitHubUserProvider gitHubUserProvider = all.get(0);
            jgitUtil = new JgitUtil(urlPath, localPath, new UsernamePasswordCredentialsProvider(gitHubUserProvider.getUserName(), gitHubUserProvider.getPassWord()));
        } else {
            jgitUtil = new JgitUtil(urlPath, localPath, null);
        }
        return jgitUtil;
    }

    /**
     * 构建 Jgit工具类 进行拉取操作
     */
    public static JgitUtil Build() {
        return Build("https://github.com/KingPrimes/DataSource", "");
    }

    /**
     * 获取远端仓库地址
     *
     * @param localPath 本地仓库路径
     * @return 远端仓库地址
     */
    public static String getOriginUrl(String localPath) {
        try (Git git = Git.open(new File(localPath))) {
            return git.getRepository().getConfig().getString("remote", "origin", "url");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更改本地仓库的远端地址
     *
     * @param originUrl 远端地址
     * @param localPath 本地仓库路径
     */
    public static void restOriginUrl(String originUrl, String localPath) {
        try (Git git = Git.open(new File(localPath))) {
            git.remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish(originUrl)).call();
        } catch (IOException | GitAPIException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Git openRpo(String path) {
        Git git;
        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(Paths.get(path, ".git").toFile())
                .build()
        ) {
            git = new Git(repository);
            return git;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化(init)——git init
     */
    public JgitUtil init() throws GitAPIException {
        try (Git ignored = Git.init().setDirectory(new File(localPath)).call()) {
            return this;
        }
    }

    /**
     * 添加到暂存区(Add)git add .
     * git add Delete.txt
     * 删除和移动的文件不能使用git.add(),需要使用git.rm()的方式，就算参数是“.”也需要使用 git.rm()方法
     *
     * @param fileName 文件名称
     */
    public JgitUtil add(String fileName) throws GitAPIException {
        openRpo(localPath).add().addFilepattern(Optional.ofNullable(fileName).orElse(".").isEmpty() ? "." : fileName).call();
        return this;
    }

    /**
     * 添加所有更改得文件到暂存区
     */
    public JgitUtil add() throws GitAPIException {
        openRpo(localPath).add().addFilepattern(".").call();
        return this;
    }

    /**
     * 移除文件
     *
     * @param fileName 文件名称
     */
    public JgitUtil rm(String fileName) throws GitAPIException {
        openRpo(localPath).rm().addFilepattern(fileName).call();
        return this;
    }

    /**
     * 提交(Commit) git commit -m"first commit"
     *
     * @param commitInfo 提交消息
     */
    public JgitUtil commit(String commitInfo) throws GitAPIException {
        openRpo(localPath).commit().setMessage(Optional.ofNullable(commitInfo).orElse("default commit info")).call();
        return this;
    }

    /**
     * 移动(mv)
     *
     * @param sourcePath 移动的文件
     * @param targetPath 移动到哪里
     * @param file       文件
     */
    public void mv(String sourcePath, String targetPath, File file) {
        try {
            File newDir = new File(targetPath);
            if (!newDir.exists()) {
                newDir.mkdirs();
            }
            File newFile = new File(newDir, file.getName());
            boolean success = file.renameTo(newFile);
            if (success) {
                add(".")
                        .rm(sourcePath)
                        .commit("File moved to new directory")
                        .push(localPath);
            } else {
                // handle error
                log.error("文件移动异常！");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /*
     * ===============================分支操作=============================
     */

    /**
     * 状态(status) git status
     */
    public Map<String, String> status() throws GitAPIException {
        Map<String, String> map = new HashMap<>();
        Status status = openRpo(localPath).status().call();
        map.put("Added", status.getAdded().toString());
        map.put("Changed", status.getChanged().toString());
        map.put("Conflicting", status.getConflicting().toString());
        map.put("ConflictingStageState", status.getConflictingStageState().toString());
        map.put("IgnoredNotInIndex", status.getIgnoredNotInIndex().toString());
        map.put("Missing", status.getMissing().toString());
        map.put("Modified", status.getModified().toString());
        map.put("Removed", status.getRemoved().toString());
        map.put("UntrackedFiles", status.getUntracked().toString());
        map.put("UntrackedFolders", status.getUntrackedFolders().toString());
        return map;
    }

    /**
     * 创建分支(Create Branch) git branch dev
     *
     * @param branchName 分支名称
     */
    public JgitUtil branch(String branchName) throws GitAPIException {
        Git git = openRpo(localPath);
        //检测是否存在此分支
        List<Ref> list = git.branchList().call().stream().filter(ref -> ref.getName().equals(branchName)).toList();
        if (!list.isEmpty()) {
            return this;
        }
        git.branchCreate()
                .setName(branchName)
                .call();
        return this;
    }

    /**
     * 删除分支(Delete Branch) git branch -d dev
     *
     * @param branchName 分支名称
     */
    public JgitUtil delBranch(String branchName) throws GitAPIException {
        openRpo(localPath).branchDelete()
                .setBranchNames(branchName)
                .call();
        return this;
    }

    /**
     * 切换分支(Checkout Branch) git checkout dev
     *
     * @param branchName 分支名称
     */
    public JgitUtil checkoutBranch(String branchName) throws GitAPIException {
        openRpo(localPath).checkout()
                .setName(branchName)
                .call();
        return this;
    }

    /**
     * 所有分支(BranchList) git branch
     */
    public List<Ref> listBranch() throws GitAPIException {
        return openRpo(localPath).branchList().call();
    }

    /**
     * 合并分支(Merge Branch) git merge dev
     *
     * @param branchName 分支名称
     * @param commitMsg  合并信息
     */
    public JgitUtil mergeBranch(String branchName, String commitMsg) throws GitAPIException {
        //切换分支获取分支信息存入Ref对象里
        Ref refdev = openRpo(localPath).checkout().setName(branchName).call();
        //切换回main分支
        openRpo(localPath).checkout().setName("main").call();
        // 合并目标分支
        openRpo(localPath).merge().include(refdev)
                //同时提交
                .setCommit(true)
                // 分支合并策略NO_FF代表普通合并, FF代表快速合并
                .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                .setMessage(Optional.ofNullable(commitMsg).orElse("master Merge"))
                .call();
        return this;
    }

    /**
     * 推送(Push) git push origin master
     */
    public JgitUtil push() throws GitAPIException {
        if (provider == null) {
            throw new RuntimeException("请设置Git账户");
        }
        openRpo(localPath).push()
                //设置推送的URL名称如"origin"
                .setRemote("origin")
                //设置需要推送的分支,如果远端没有则创建
                .setRefSpecs(new RefSpec("main"))
                //身份验证
                .setCredentialsProvider(provider)
                .setProgressMonitor(new TextProgressMonitor())
                .call();
        return this;
    }

    /**
     * 推送(Push)
     *
     * @param branch 推送分支
     */
    public Iterator<PushResult> push(String branch) throws GitAPIException {
        if (provider == null) {
            throw new RuntimeException("请设置Git账户");
        }
        return openRpo(localPath)
                .push()
                //设置推送的URL名称如"origin"
                .setRemote("origin")
                .setRefSpecs(new RefSpec(branch + ":refs/heads/" + branch))
                //身份验证
                .setCredentialsProvider(provider)
                .setProgressMonitor(new TextProgressMonitor())
                .call().iterator();
    }

    /**
     * 创建分支推送
     *
     * @param commit     提交信息
     * @param branchName 分支名称
     * @param files      推送文件
     */
    public Iterator<PushResult> pushBranchCheckout(String commit, String branchName, String files) throws GitAPIException {
        if (listBranch().stream().anyMatch(ref -> ref.getName().contains(branchName))) {
            return checkoutBranch(branchName).add(files).commit(commit).push(branchName);
        }
        return branch(branchName).checkoutBranch(branchName).add(files).commit(commit).push(branchName);
    }

    /**
     * 拉取(Pull) git pull origin
     */
    public void pull() throws GitAPIException {
        //判断localPath是否存在，不存在调用clone方法
        File directory = new File(localPath);
        if (!directory.exists()) {
            gitClone("main");
        }
        Git git = openRpo(localPath);
        if (provider == null) {
            git.pull()
                    .setRemoteBranchName("main")
                    .setProgressMonitor(new LoggingProgressMonitor())
                    .call();
            git.close();
            return;
        }
        git.pull()
                .setRemoteBranchName("main")
                .setCredentialsProvider(provider)
                .setProgressMonitor(new LoggingProgressMonitor())
                .call();
        git.close();
    }

    /**
     * 拉取(Pull) git pull
     *
     * @param branch 分支
     */
    public void pull(String branch) throws Exception {
        //判断localPath是否存在，不存在调用clone方法
        File directory = new File(localPath);
        if (!directory.exists()) {
            gitClone(branch);
        }
        Git git = openRpo(localPath);
        if (provider == null) {
            git.pull()
                    .setRemoteBranchName("main")
                    .setProgressMonitor(new LoggingProgressMonitor())
                    .call();
            git.close();
            return;
        }
        git.pull()
                .setRemoteBranchName(branch)
                .setCredentialsProvider(provider)
                .setProgressMonitor(new LoggingProgressMonitor())
                .call();
        git.close();
    }

    /**
     * 克隆(Clone)
     *
     * @param branch 分支
     */
    public void gitClone(String branch) throws GitAPIException {
        if (provider == null) {
            Git git = Git.cloneRepository()
                    .setURI(urlPath + ".git")
                    .setTimeout(60)
                    .setDirectory(new File(localPath))
                    //设置是否克隆子仓库
                    .setCloneSubmodules(true)
                    //设置克隆分支
                    .setBranch(branch)
                    .setProgressMonitor(new LoggingProgressMonitor())
                    .call();
            //关闭源，以释放本地仓库锁
            git.getRepository().close();
            return;
        }
        Git git = Git.cloneRepository()
                .setURI(urlPath + ".git")
                .setDirectory(new File(localPath))
                .setCredentialsProvider(provider)
                //设置是否克隆子仓库
                .setCloneSubmodules(true)
                //设置克隆分支
                .setBranch(branch)
                .setProgressMonitor(new LoggingProgressMonitor())
                .call();
        //关闭源，以释放本地仓库锁
        git.getRepository().close();
    }

    /**
     * 克隆(Clone)
     */
    public void gitClone() throws GitAPIException {
        Git git = Git.cloneRepository()
                .setURI(urlPath + ".git")
                .setDirectory(new File(localPath))
                //设置是否克隆子仓库
                .setCloneSubmodules(true)
                .setProgressMonitor(new LoggingProgressMonitor())
                .call();
        //关闭源，以释放本地仓库锁
        git.getRepository().close();
    }
}


@Slf4j
class LoggingProgressMonitor extends BatchingProgressMonitor {

    private Instant startTime;
    private int lastLoggedPercent = -10; // 初始化为-10，确保第一次输出为0%
    private String currentTaskName; // 添加一个变量来存储当前任务的名称

    @Override
    public void beginTask(String title, int work) {
        super.beginTask(title, work);
        startTime = Instant.now(); // 记录任务开始时间
        currentTaskName = title;// 存储当前任务的名称
        log.info("开始: {}", title); // 输出开始克隆的日志
    }

    @Override
    protected void onUpdate(String taskName, int workCurr, Duration duration) {
        // 不再每次更新都输出日志
    }

    @Override
    protected void onEndTask(String taskName, int workCurr, Duration duration) {
        Duration elapsed = Duration.between(startTime, Instant.now()); // 计算已用时间
        long seconds = elapsed.getSeconds();
        log.info("任务：{} 完成，耗时：{}s", taskName, seconds);
    }

    @Override
    protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone, Duration duration) {
        if (percentDone / 10 > lastLoggedPercent / 10) { // 检查是否达到下一个10%的里程碑
            lastLoggedPercent = percentDone; // 更新最后记录的百分比
            log.info("任务：{}，完成: {}% ({} / {})", taskName, percentDone, workCurr, workTotal);
        }
    }

    @Override
    protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone, Duration duration) {
        log.info("任务：{} 完成，totalWorkload: {}", currentTaskName, workTotal);
    }
}