package ink.markidea.note.controller;

import ink.markidea.note.entity.req.UserRequest;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.UserVo;
import ink.markidea.note.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author hansanshi
 * @date 2019/12/26
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

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

    @PostMapping("")
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
}
