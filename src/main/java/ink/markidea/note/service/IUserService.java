package ink.markidea.note.service;


import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.UserVo;

/**
 * @author hansanshi
 * @date 2019/12/31
 */
public interface IUserService {

    ServerResponse<UserVo> login(String username, String password);

    ServerResponse validate(String username, String token);

    ServerResponse logout();

    ServerResponse<UserVo> register(String username, String password);

    ServerResponse changePassword(String oldPassword, String newPassword);
}
