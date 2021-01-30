package ink.markidea.note.controller;

import ink.markidea.note.entity.dto.EditorConfigDto;
import ink.markidea.note.entity.req.EditorConfigReq;
import ink.markidea.note.entity.req.RemoteRepoRequest;
import ink.markidea.note.entity.req.UserRequest;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.UserVo;
import ink.markidea.note.service.IAdminService;
import ink.markidea.note.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author hansanshi
 * @date 2019/12/26
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IAdminService adminService;

    @Autowired
    private IUserService userService;

    @PostMapping("login")
    public ServerResponse<UserVo> login(@RequestBody UserRequest request){
        return userService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("validate")
    public ServerResponse validate(){
        // Validated by interceptor
        return ServerResponse.buildSuccessResponse();
    }

    @PostMapping("register")
    public ServerResponse<UserVo> register(@RequestBody UserRequest request){
        return userService.register(request.getUsername(), request.getPassword());
    }

    @PostMapping("/changePass")
    public ServerResponse changePassword(@RequestBody UserRequest request){
        return userService.changePassword(request.getPassword(), request.getNewPassword());
    }

    @PostMapping("/logout")
    public ServerResponse logout(){
        return userService.logout();
    }

    @PutMapping("remote")
    public ServerResponse setRemoteRepoUrl(@RequestBody RemoteRepoRequest request){
        return adminService.setRemoteRepoUrl(request.getRemoteRepoUrl());
    }

    @GetMapping("remote")
    public ServerResponse getRemoteRepoUrl(){
        return adminService.getRemoteRepoUrl();
    }

    @PostMapping("sshkey")
    public ServerResponse<String> genSshKeys(){
        return adminService.generateSshKey();
    }

    @DeleteMapping("push")
    public ServerResponse stopPushToRemote(){
        return adminService.stopPushToRemoteRepo();
    }

    @GetMapping("push")
    public ServerResponse checkPushStatus(){
        return adminService.checkPushTaskStatus();
    }

    @PostMapping("push")
    public ServerResponse startPushToRemote(){
        return adminService.startPushToRemoteRepo();
    }

    @PostMapping("pull")
    public ServerResponse pullFromRemote(){
        return adminService.pullFromRemote();
    }

    @GetMapping("editorConfig")
    public ServerResponse<EditorConfigDto> getEditorConfig() {
        return ServerResponse.buildSuccessResponse(userService.getEditorConfig());
    }

    @PostMapping("updateEditorConfig")
    public ServerResponse<EditorConfigDto> updateEditorConfig(@RequestBody EditorConfigReq req) {
        return ServerResponse.buildSuccessResponse(userService.updateEditorConfig(req));
    }
}
