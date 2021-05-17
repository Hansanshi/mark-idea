package ink.markidea.note.service.impl;

import ink.markidea.note.constant.RegisterConstant;
import ink.markidea.note.context.config.CacheConfig;
import ink.markidea.note.context.task.NoteTimer;
import ink.markidea.note.dao.UserRepository;
import ink.markidea.note.entity.dto.WebsiteConfigDto;
import ink.markidea.note.entity.exception.PromptException;
import ink.markidea.note.entity.req.WebsiteConfigReq;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.service.IAdminService;
import ink.markidea.note.service.IUserService;
import ink.markidea.note.util.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author hansanshi
 * @date 2020/1/27
 */
@Service
@Slf4j
public class AdminServiceImpl implements IAdminService {

    @Value("${notesDir}")
    private String notesDir;

    @Value(("${frontDir}"))
    private String frontDir;

    @Autowired
    private NoteTimer noteTimer;

    @Value("${sshKeysDir}")
    private String sshKeysDir;

    @Value("${configDir}")
    private String configDir;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IUserService userService;

    @Override
    public ServerResponse setRemoteRepoUrl(String remoteRepoUrl) {
        userRepository.save(userRepository.findByUsername(getUsername()).setRemoteRepository(remoteRepoUrl));
        noteTimer.refreshPushTaskList();
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public ServerResponse stopPushToRemoteRepo(){
        userRepository.save(userRepository.findByUsername(getUsername()).setPush(false));
        noteTimer.refreshPushTaskList();
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public ServerResponse<String> generateSshKey(){
        String username = getUsername();
        String pubKeyContent = SshUtil.genAndStoreKeyPair(username + ".prv", username + ".pub", sshKeysDir);
        return ServerResponse.buildSuccessResponse(pubKeyContent);
    }

    @Override
    public ServerResponse checkPushTaskStatus(){
        Boolean result = noteTimer.checkPushTaskStatus(getUsername());
        if (result != null && !result){
            return ServerResponse.buildErrorResponse("Can't push to remote repo");
        }
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public ServerResponse startPushToRemoteRepo() {
        userRepository.save(userRepository.findByUsername(getUsername()).setPush(true));
        noteTimer.refreshPushTaskList();
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public ServerResponse getRemoteRepoUrl() {
        String remoteRepoUrl = userRepository.findByUsername(getUsername()).getRemoteRepository();
        return ServerResponse.buildSuccessResponse(remoteRepoUrl);
    }

    @Override
    public ServerResponse pullFromRemote() {
        Git git = getOrCreateUserGit();
        try {
            String prvKeyPath = new File(sshKeysDir, getUsername() + ".prv").getAbsolutePath();
            GitUtil.pullFromRemote(git, prvKeyPath);
        } catch (GitAPIException e) {
            throw new PromptException("拉取远程仓库失败");
        }
        return ServerResponse.buildSuccessResponse();
    }

    private File getOrCreateUserNotebookDir(){
        File dir = new File(notesDir, getUsername());
        if (dir.exists()){
            return dir;
        }
        dir.mkdir();
        return dir;
    }

    @Override
    public synchronized boolean updateWebSiteConfig(WebsiteConfigReq req) {
        WebsiteConfigDto originWebsiteConfig = getWebsiteConfig();
        validateWebsiteConfig(req);
        updateWebsiteTitleIfChange(req, originWebsiteConfig);
        updateRegisterStrategyIfChange(req, originWebsiteConfig);
        updateTokenExpireTimeIfChange(req, originWebsiteConfig);
        File websiteConfigFile = new File(configDir, "website-config.json");
        return FileUtil.writeStringToFile(JsonUtil.objToString(req), websiteConfigFile);
    }

    private void updateTokenExpireTimeIfChange(WebsiteConfigReq req, WebsiteConfigDto originWebsiteConfig) {
        if (req.getTokenExpireTimeInHour() == originWebsiteConfig.getTokenExpireTimeInHour()) {
            return ;
        }
        CacheConfig.setTokenExpireTimeInHour(req.getTokenExpireTimeInHour());
    }

    private void updateRegisterStrategyIfChange(WebsiteConfigReq req, WebsiteConfigDto originWebsiteConfig) {
        if (originWebsiteConfig.getRegisterStrategy().equals(req.getRegisterStrategy())) {
            return ;
        }
        userService.setRegisterStrategy(req.getRegisterStrategy());
    }

    private void updateWebsiteTitleIfChange(WebsiteConfigReq req, WebsiteConfigDto originWebsiteConfig) {
        if (originWebsiteConfig.getWebsiteTitle().equals(req.getWebsiteTitle())) {
            return ;
        }
        File indexHtmlFile = new File(frontDir, "index.html");
        if (!indexHtmlFile.exists()) {
            // 支持前后端分离事 没有前端文件
            return ;
        }
        String indexHtmlStr  = FileUtil.readFileAsString(indexHtmlFile);
        String newIndexHtml = indexHtmlStr.replace("<title>" + originWebsiteConfig.getWebsiteTitle() + "</title>", "<title>" + req.getWebsiteTitle() + "</title>");
        FileUtil.writeStringToFile(newIndexHtml, indexHtmlFile);
    }

    private void validateWebsiteConfig(WebsiteConfigReq req) {
        if (req.getWebsiteTitle().contains("<")) {
            throw new IllegalArgumentException();
        }
        if (req.getRegisterStrategy() != RegisterConstant.NOT_ALLOW_REGISTER && req.getRegisterStrategy() != RegisterConstant.ALLOW_REGISTER) {
            throw new IllegalArgumentException();
        }
        try{
            Integer num = Integer.valueOf(req.getMaxUploadFileSize().substring(0, req.getMaxUploadFileSize().length() - 2));
            req.setMaxUploadFileSize(num + "MB");
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public WebsiteConfigDto getWebsiteConfig() {
        File websiteConfigFile = new File(configDir, "website-config.json");
        if (!websiteConfigFile.exists()) {
            return new WebsiteConfigDto();
        }
        String configStr = FileUtil.readFileAsString(websiteConfigFile);
        return JsonUtil.stringToObj(configStr, WebsiteConfigDto.class);
    }

    private Git getOrCreateUserGit(){
        return GitUtil.getOrInitGit(getOrCreateUserNotebookDir());
    }

    private String getUsername(){
        return ThreadLocalUtil.getUsername();
    }

}
