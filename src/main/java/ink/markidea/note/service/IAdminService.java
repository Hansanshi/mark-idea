package ink.markidea.note.service;


import ink.markidea.note.entity.dto.WebsiteConfigDto;
import ink.markidea.note.entity.req.WebsiteConfigReq;
import ink.markidea.note.entity.resp.ServerResponse;

/**
 * 后台管理服务
 * @author hansanshi
 * @date 2020/1/27
 * @description TODO
 */
public interface IAdminService {

    ServerResponse setRemoteRepoUrl(String remoteRepoUrl);

    ServerResponse stopPushToRemoteRepo();

    ServerResponse<String> generateSshKey();

    ServerResponse checkPushTaskStatus();

    ServerResponse startPushToRemoteRepo();

    ServerResponse getRemoteRepoUrl();

    ServerResponse pullFromRemote();

    boolean updateWebSiteConfig(WebsiteConfigReq req);

    WebsiteConfigDto getWebsiteConfig();
}
