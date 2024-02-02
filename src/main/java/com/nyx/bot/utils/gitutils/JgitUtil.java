package com.nyx.bot.utils.gitutils;

import com.nyx.bot.entity.git.GitHubUserProvider;
import com.nyx.bot.repo.git.GitHubUserProviderRepository;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class JgitUtil {

    private final CredentialsProvider provider;
    String urlPath;
    String localPath;

    public JgitUtil(String urlPath, String localPath, CredentialsProvider provider) {
        this.urlPath = urlPath;
        this.localPath = localPath;
        this.provider = provider;
    }

    /**
     * @param urlPath   远程仓库地址
     * @param localPath 本地仓库路径
     */
    public static JgitUtil Buite(String urlPath, String localPath) {
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


    public Git openRpo(String path) {
        Git git = null;
        try {
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(Paths.get(path, ".git").toFile())
                    .build();
            git = new Git(repository);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return git;
    }

    /**
     * 初始化(init)——git init
     */
    public JgitUtil init() throws GitAPIException {
        Git.init().setDirectory(new File(localPath)).call();
        return this;
    }

    /**
     * 添加到暂存区(Add)git add .
     * git add Delete.txt
     * 删除和移动的文件不能使用git.add(),需要使用git.rm()的方式，就算参数是“.”也需要使用 git.rm()方法
     *
     * @param fileName 文件名称
     */
    public JgitUtil add(String fileName) throws Exception {
        openRpo(localPath).add().addFilepattern(Optional.ofNullable(fileName).orElse(".")).call();
        return this;
    }

    /**
     * 添加所有更改得文件到暂存区
     */
    public JgitUtil add() throws Exception {
        openRpo(localPath).add().addFilepattern(".").call();
        return this;
    }

    /**
     * 移除文件
     *
     * @param fileName 文件名称
     */
    public JgitUtil rm(String fileName) throws Exception {
        openRpo(localPath).rm().addFilepattern(fileName).call();
        return this;
    }

    /**
     * 提交(Commit) git commit -m"first commit"
     *
     * @param commitInfo 提交消息
     */
    public JgitUtil commit(String commitInfo) throws Exception {
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
                add(".");
                rm(sourcePath);
                commit("File moved to new directory");
                push(localPath);
            } else {
                // handle error
                log.error("文件移动异常！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 状态(status) git status
     */
    public Map<String, String> status() throws Exception {
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

    /*
     * ===============================分支操作=============================
     */

    /**
     * 创建分支(Create Branch) git branch dev
     *
     * @param branchName 分支名称
     */
    public JgitUtil branch(String branchName) throws Exception {
        openRpo(localPath).branchCreate()
                .setName(branchName)
                .call();
        return this;
    }

    /**
     * 删除分支(Delete Branch) git branch -d dev
     *
     * @param branchName 分支名称
     */
    public JgitUtil delBranch(String branchName) throws Exception {
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
    public JgitUtil checkoutBranch(String branchName) throws Exception {
        openRpo(localPath).checkout()
                .setName(branchName)
                .call();
        return this;
    }

    /**
     * 所有分支(BranchList) git branch
     */
    public List<Ref> listBranch() throws Exception {
        return openRpo(localPath).branchList().call();
    }

    /**
     * 合并分支(Merge Branch) git merge dev
     *
     * @param branchName 分支名称
     * @param commitMsg  合并信息
     */
    public JgitUtil mergeBranch(String branchName, String commitMsg) throws Exception {
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
    public JgitUtil push() throws Exception {
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
                .call();
        return this;
    }

    /**
     * 推送(Push)
     *
     * @param branch 推送分支
     */
    public JgitUtil push(String branch) throws Exception {
        if (provider == null) {
            throw new RuntimeException("请设置Git账户");
        }
        openRpo(localPath).push()
                //设置推送的URL名称如"origin"
                .setRemote("origin")
                //设置需要推送的分支,如果远端没有则创建
                .setRefSpecs(new RefSpec(branch))
                //身份验证
                .setCredentialsProvider(provider)
                .call();
        return this;
    }

    /**
     * /拉取(Pull) git pull origin
     */
    public JgitUtil pull() throws Exception {
        if (provider == null) {
            throw new RuntimeException("请设置Git账户");
        }
        //判断localPath是否存在，不存在调用clone方法
        File directory = new File(localPath);
        if (!directory.exists()) {
            gitClone("main");
        }
        openRpo(localPath).pull()
                .setRemoteBranchName("main")
                .setCredentialsProvider(provider)
                .call();
        return this;
    }

    /**
     * /拉取(Pull) git pull
     *
     * @param branch 分支
     */
    public JgitUtil pull(String branch) throws Exception {
        if (provider == null) {
            throw new RuntimeException("请设置Git账户");
        }
        //判断localPath是否存在，不存在调用clone方法
        File directory = new File(localPath);
        if (!directory.exists()) {
            gitClone(branch);
        }
        openRpo(localPath).pull()
                .setRemoteBranchName(branch)
                .setCredentialsProvider(provider)
                .call();
        return this;
    }

    /**
     * 克隆(Clone)
     *
     * @param branch 分支
     */
    public JgitUtil gitClone(String branch) throws Exception {
        if (provider == null) {
            throw new RuntimeException("请设置Git账户");
        }
        Git git = Git.cloneRepository()
                .setURI(urlPath + ".git")
                .setDirectory(new File(localPath))
                .setCredentialsProvider(provider)
                //设置是否克隆子仓库
                .setCloneSubmodules(true)
                //设置克隆分支
                .setBranch(branch)
                .call();
        //关闭源，以释放本地仓库锁
        git.getRepository().close();
        git.close();

        return this;
    }
}
