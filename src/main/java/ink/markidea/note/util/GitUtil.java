package ink.markidea.note.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import ink.markidea.note.entity.vo.NoteVersionVo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.eclipse.jgit.lib.ConfigConstants.*;

/**
 * @author hansanshi
 * @date 2019/12/21
 */
@Slf4j
public class GitUtil {

    private interface ChangeType {
        int NEW_OR_MODIFY = 0;
        int NEW = 1;
        int MODIFY = 2;
        int MOVE = 3;
        int COPY = 4;
        int RECOVER = 5;
        int RESET = 6;
        int DELETE = 7;
    }

    private static final String DEFAULT_REMOTE_ALIAS_NAME = "origin";

    private static final String DEFAULT_LOCAL_BRANCH_NAME = "master";

    private static final String DEFAULT_REMOTE_BRANCH_NAME = "master";

    private static final String GIT_FLAG_FILE = ".markidea";

    private static Git createNewGit(String path) {
        File gitDir = new File(path);
        return createNewGit(gitDir);
    }

    public static Git getOrInitGit(String path) {
        File gitDir = new File(path);
        return getOrInitGit(gitDir);
    }

    public static Git getOrInitGit(File gitDir) {
        if (!gitDir.exists() && !gitDir.mkdir()) {
            return null;
        } else if (gitDir.isFile()) {
            return null;
        }
        Git git = getLocalGit(gitDir);
        if (git == null) {
            git = createNewGit(gitDir);
        }
        return git;
    }

    private static Git createNewGit(File gitDir) {
        try {
            Git git = Git.init().setDirectory(gitDir).call();
            File file = new File(gitDir, GIT_FLAG_FILE);
            file.createNewFile();
            addAndCommit(git, GIT_FLAG_FILE);
            return git;
        } catch (GitAPIException | IOException e) {
            return null;
        }
    }

    public static List<NoteVersionVo> getNoteHistory(Git git, String fileName) {
        List<NoteVersionVo> noteVersionVoList = new ArrayList<>();
        List<RevCommit> revCommitList = getVersionHistory(git, fileName);

        Set<String> skippedRefs = new HashSet<>();
        for (RevCommit revCommit : revCommitList) {
            CommitMessage message ;
            message = JsonUtil.stringToObj(revCommit.getFullMessage(), CommitMessage.class);
            if (message == null || message.getChangeType() == null){
                continue;
            }
            if (message.getChangeType() == ChangeType.RESET) {
                skippedRefs.add(message.prevRef);
            }
            if (skippedRefs.contains(revCommit.getName())) {
                continue;
            }
            if (isContentChangeCommit(message.getChangeType(), fileName.equals(message.getFileName()))) {
                noteVersionVoList.add(new NoteVersionVo()
                        .setDate(DateTimeUtil.dateToStr(revCommit.getAuthorIdent().getWhen()))
                        .setRef(revCommit.getName()));
            }
        }
        return noteVersionVoList;
    }

    /**
     * get raw version history
     */
    private static List<RevCommit> getVersionHistory(Git git, String fileName) {
        List<RevCommit> list = new ArrayList<>();
        Iterable<RevCommit> iterable = null;
        try {
            iterable = git.log().addPath(fileName).call();
        } catch (GitAPIException e) {
            return list;
        }
        iterable.forEach(list::add);
        return list;
    }

    /**
     * add and then commit
     *
     * @param git
     * @param fileName
     */
    public static boolean addAndCommit(Git git, String fileName) {
        try {
            git.add().addFilepattern(fileName).call();
            git.commit().setMessage(getCommitMsgStr(ChangeType.NEW_OR_MODIFY, fileName)).call();
            return true;
        } catch (GitAPIException | JGitInternalException e) {
            log.error("can't add file: {}, cause is: {}", fileName, e.getMessage());
            return false;
        }
    }

    public static boolean mvAndCommit(Git git, String oldFilename, String newFilename) {
        try {
            git.add().addFilepattern(newFilename).call();
            git.rm().addFilepattern(oldFilename).call();
            git.commit().setMessage(getCommitMsgStr(ChangeType.MOVE, newFilename)).call();
            return true;
        } catch (GitAPIException | JGitInternalException e) {
            log.error("can't move file: {}, cause is: {}", oldFilename, e.getMessage());
            return false;
        }
    }

    /**
     * 删除并提交
     *
     * @param git
     * @param fileName
     */
    public static boolean rmAndCommit(Git git, String fileName) {
        try {
            git.rm().addFilepattern(fileName).call();
            git.commit().setMessage(getCommitMsgStr(ChangeType.DELETE, fileName)).call();
            return true;
        } catch (GitAPIException | JGitInternalException e) {
            log.error("can't remove file: {}, cause is: {}", fileName, e.getMessage());
            return false;
        }
    }

    public static boolean resetAndCommit(Git git, String fileName, String versionRef) {
        try {
            git.reset().setRef(versionRef).addPath(fileName).call();
            git.checkout().addPath(fileName).call();
            git.commit().setMessage(JsonUtil.objToString(new CommitMessage().setChangeType(ChangeType.RESET).setFileName(fileName).setPrevRef(versionRef))).call();
            log.info("reset file: {} to version: {}", fileName, versionRef);
            return true;
        } catch (GitAPIException | JGitInternalException e) {
            log.error("can't reset file: {} to version: {}, cause is: {}", fileName, versionRef, e.getMessage());
            return false;
        }
    }


    public static void recoverDeletedFile(Git git, String fileName, String lastRef) {
        try {
            git.checkout().setStartPoint(lastRef).addPath(fileName).call();
            git.add().addFilepattern(fileName).call();
            git.commit()
                    .setMessage(JsonUtil.objToString(new CommitMessage().setChangeType(ChangeType.RECOVER).setFileName(fileName)))
                    .call();
            log.info("recover deleted file: {}", fileName);
        } catch (GitAPIException e) {
            log.error("Can't recover deleted file: {}", fileName, e);
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * @param privateKeyPath the path store user's private ssh key
     */
    public static void pushToRemoteViaSsh(Git git, String privateKeyPath) throws GitAPIException {
        SshSessionFactory sshSessionFactory = new MyJshConfigSessionFactory(privateKeyPath);
        git.push().setTransportConfigCallback(transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }).call();
    }

    public static void pullFromRemote(Git git, String privateKeyPath) throws GitAPIException {
        SshSessionFactory sshSessionFactory = new MyJshConfigSessionFactory(privateKeyPath);

        git.pull().setTransportConfigCallback(transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }).call();
    }

    public static void pushToRemoteViaHttp(Git git, String username, String password) throws GitAPIException {
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
    }


    public static void setRemoteRepository(Git git, String remoteAliasName, String remoteUrl) throws IOException {
        StoredConfig config = git.getRepository().getConfig();
        config.setString(CONFIG_REMOTE_SECTION, remoteAliasName, CONFIG_KEY_URL, remoteUrl);
        config.setString(CONFIG_REMOTE_SECTION, remoteAliasName, "fetch", "+refs/heads/*:refs/remotes/" + remoteAliasName + "/*");
        config.save();
    }

    public static void setRemoteRepository(Git git, String remoteUrl) throws IOException {
        setRemoteRepository(git, DEFAULT_REMOTE_ALIAS_NAME, remoteUrl);
    }

    public static void setRemoteBranch(Git git, String localBranch, String remoteAliasName, String remoteBranch) throws IOException {
        StoredConfig config = git.getRepository().getConfig();
        config.setString(CONFIG_BRANCH_SECTION, localBranch, CONFIG_KEY_REMOTE, remoteAliasName);
        config.setString(CONFIG_BRANCH_SECTION, localBranch, CONFIG_KEY_MERGE, "refs/heads/" + remoteBranch);
        config.save();
    }

    public static void setRemoteBranch(Git git) throws IOException {
        setRemoteBranch(git, DEFAULT_LOCAL_BRANCH_NAME, DEFAULT_REMOTE_ALIAS_NAME, DEFAULT_REMOTE_BRANCH_NAME);
    }

    public static void setRemoteRepositoryAndBranch(Git git, String remoteUrl) throws IOException {
        setRemoteRepository(git, remoteUrl);
        setRemoteBranch(git);
    }

    public static String getFileCurRef(Git git, String fileName) {
        List<NoteVersionVo> noteVersionVoList = getNoteHistory(git, fileName);
        for (NoteVersionVo noteVersionVo : noteVersionVoList) {
            return noteVersionVo.getRef();
        }
        return null;
    }


    /**
     * temporarily quit to complete the method
     */
    @Deprecated
    public static List<CommitChangeType> getChangeTypeList(Git git, List<RevCommit> revCommitList, String fileName) throws GitAPIException, IOException {
        List<CommitChangeType> list = new ArrayList<>();
        Iterator<RevCommit> iterator = revCommitList.iterator();
        if (!iterator.hasNext()) {
            return Collections.emptyList();
        }
        RevCommit newCommit = iterator.next();
        ObjectReader reader = git.getRepository().newObjectReader();
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        diffFormatter.setRepository(git.getRepository());
        while (iterator.hasNext()) {
            ObjectId newTree = git.getRepository().resolve(newCommit.getName() + "^{tree}");
            newTreeIter.reset(reader, newTree);

            RevCommit oldCommit = iterator.next();
            ObjectId oldTree = git.getRepository().resolve(oldCommit.getName() + "^{tree}");
            oldTreeIter.reset(reader, oldTree);
            String finalRef = newCommit.getName();
            diffFormatter.scan(oldTreeIter, newTreeIter)
                    .stream()
                    .filter(entry -> entry.getNewPath().equals(fileName) || entry.getOldPath().equals(fileName))
                    .findFirst()
                    .ifPresent(entry -> list.add(new CommitChangeType(finalRef, entry.getChangeType())));

            newCommit = oldCommit;
        }

        list.add(new CommitChangeType(newCommit.getName(), DiffEntry.ChangeType.ADD));
        return list;

    }

    private static DiffEntry.ChangeType getChangeType(Git git, List<DiffEntry> entryList, String fileName) {
        for (DiffEntry entry : entryList) {
            if (entry.getNewPath().equals(fileName) || entry.getOldPath().equals(fileName)) {
                return entry.getChangeType();
            }
        }
        return null;
    }


    private static class MyJshConfigSessionFactory extends JschConfigSessionFactory {

        //    ssh-keygen -t rsa -m PEM
        //    do not support openssh
        //    https://stackoverflow.com/questions/53134212/invalid-privatekey-when-using-jsch
        private String privateKeyPath;

        public MyJshConfigSessionFactory(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
        }

        @Override
        protected void configure(OpenSshConfig.Host hc, Session session) {
            // to solve UnknownHostKey Exception, but not secure
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch defaultJSch = super.createDefaultJSch(fs);

            defaultJSch.addIdentity(this.privateKeyPath);
            return defaultJSch;
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CommitMessage {

        private Integer changeType;

        private String fileName;

        private String oldFileName;

        private String newFileName;

        /**
         * the prev commit ref reset
         */
        private String prevRef;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Deprecated
    public static class CommitChangeType {

        private String ref;

        private DiffEntry.ChangeType changeType;

    }

    private static String getCommitMsgStr(int changeType, String fileName) {
        return JsonUtil.objToString(new CommitMessage().setFileName(fileName).setChangeType(changeType));
    }

    private static boolean isContentChangeCommit(int changeType, boolean checkFilename) {
        return changeType == ChangeType.NEW_OR_MODIFY
                || changeType == ChangeType.NEW
                || changeType == ChangeType.MODIFY
                || changeType == ChangeType.RESET
                || (changeType == ChangeType.MOVE && checkFilename);
    }

    /**
     * 判断是否是git仓库
     *
     * @param path
     * @return
     */
    public static boolean isGitDir(String path) {
        return getLocalGit(path) == null;
    }

    private static Git getLocalGit(String path) {
        File gitDirectory = new File(path);
        try {
            return Git.open(gitDirectory);
        } catch (IOException e) {
            return null;
        }
    }

    private static Git getLocalGit(File gitDir) {
        try {
            return Git.open(gitDir);
        } catch (IOException e) {
            return null;
        }
    }
}
