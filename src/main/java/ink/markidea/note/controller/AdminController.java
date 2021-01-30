package ink.markidea.note.controller;

import ink.markidea.note.entity.dto.WebsiteConfigDto;
import ink.markidea.note.entity.req.RemoteRepoRequest;
import ink.markidea.note.entity.req.WebsiteConfigReq;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.service.IAdminService;
import ink.markidea.note.service.IFileService;
import ink.markidea.note.service.INoteService;
import ink.markidea.note.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author hansanshi
 * @date 2020/1/26
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @GetMapping("websiteConfig")
    public ServerResponse<WebsiteConfigDto> pullWebsiteConfig(){
        return ServerResponse.buildSuccessResponse(adminService.getWebsiteConfig());
    }

    @PostMapping("updateWebsiteConfig")
    public ServerResponse updateWebsiteConfig(@RequestBody WebsiteConfigReq req) {
        return adminService.updateWebSiteConfig(req) ? ServerResponse.buildSuccessResponse() : ServerResponse.buildErrorResponse("更新失败");
    }

}
