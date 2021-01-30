package ink.markidea.note.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import ink.markidea.note.constant.RegisterConstant;
import ink.markidea.note.constant.UserConstant;
import ink.markidea.note.dao.UserRepository;
import ink.markidea.note.entity.UserDo;
import ink.markidea.note.entity.dto.EditorConfigDto;
import ink.markidea.note.entity.exception.NoAuthorityException;
import ink.markidea.note.entity.exception.PromptException;
import ink.markidea.note.entity.req.EditorConfigReq;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.UserVo;
import ink.markidea.note.service.IUserService;
import ink.markidea.note.util.JsonUtil;
import ink.markidea.note.util.MD5Util;
import ink.markidea.note.util.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * @author hansanshi
 * @date 2019/12/31
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private Cache<String, UserVo> userCache;

    @Value("${username:admin}")
    private String adminUsername;

    @Value("${password:admin}")
    private String adminPassword;

    /**
     * 注册控制策略
     */
    @Value("${registerStrategy}")
    private volatile Integer registerStrategy;


    @Autowired
    private UserRepository userRepository;

    private static final String TOKEN_PREFIX = "token_";

    @PostConstruct
    private void initAdminUser(){
        UserDo userDO = userRepository.findFirstByStatus(0);
        if (userDO == null){
            UserDo adminUser = new UserDo().setUsername(adminUsername).setPassword(MD5Util.MD5EncodeUtf8(adminPassword)).setStatus(UserConstant.ADMIN_USER);
            userRepository.save(adminUser);
        }
    }

    @Override
    public ServerResponse<UserVo> login(String username, String password){
        String encodedPassword = MD5Util.MD5EncodeUtf8(password);
        UserDo userDO = userRepository.findByUsernameAndPassword(username, encodedPassword);
        if (userDO == null){
            return ServerResponse.buildErrorResponse("用户名或密码错误");
        }
        UserVo userVo = new UserVo().setToken(TOKEN_PREFIX + UUID.randomUUID().toString()).setUsername(username).setType(userDO.getStatus());
        if (StringUtils.isNotBlank(userDO.getEditorConfig())) {
            userVo.setEditorConfig(JsonUtil.stringToObj(userDO.getEditorConfig(), EditorConfigDto.class));
        }
        userCache.put(userVo.getUsername(), userVo);
        return ServerResponse.buildSuccessResponse(userVo);
    }

    @Override
    public ServerResponse validate(String username, String token){
        UserVo userVo = userCache.getIfPresent(username);
        if (userVo == null || !token.equals(userVo.getToken())){
            return ServerResponse.buildErrorResponse("Invalid token");
        }else {
            return ServerResponse.buildSuccessResponse();
        }
    }


    @Override
    public boolean checkAdminUser(String username, String token) {
        UserVo userVo = userCache.getIfPresent(username);
        if (userVo == null || !token.equals(userVo.getToken())){
            return false;
        }

        return userVo.getType() == UserConstant.ADMIN_USER;
    }

    @Override
    public ServerResponse logout() {
        userCache.invalidate(getUsername());
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public ServerResponse<UserVo> register(String username, String password){
        if (RegisterConstant.NOT_ALLOW_REGISTER == registerStrategy){
            throw new PromptException("禁止注册");
        }
        if (StringUtils.isAnyBlank(username, password)){
            throw new IllegalArgumentException();
        }
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            throw new IllegalArgumentException("Arguments can't be blank");
        }
        UserDo userDO = userRepository.save(new UserDo().setUsername(username).setPassword(MD5Util.MD5EncodeUtf8(password)));
        UserVo userVo = new UserVo().setUsername(userDO.getUsername());
        return ServerResponse.buildSuccessResponse(userVo);
    }

    @Override
    @Transactional
    public ServerResponse changePassword(String oldPassword, String newPassword){
        if (StringUtils.isAnyBlank(oldPassword, newPassword)){
            throw new IllegalArgumentException();
        }
        String oldEncodedPassword = MD5Util.MD5EncodeUtf8(oldPassword);
        UserDo userDO = userRepository.findByUsernameAndPassword(getUsername(), oldEncodedPassword);
        if (userDO == null){
            return ServerResponse.buildErrorResponse("Wrong password");
        }
        String newEncodedPassword = MD5Util.MD5EncodeUtf8(newPassword);
        userDO.setPassword(newEncodedPassword);
        userRepository.save(userDO);
        return ServerResponse.buildSuccessResponse();

    }

    private String getUsername(){
        return ThreadLocalUtil.getUsername();
    }

    @Override
    public void setRegisterStrategy(Integer strategy) {
        this.registerStrategy = strategy;
    }

    @Override
    public EditorConfigDto getEditorConfig() {
        String editorConfigStr = userRepository.findByUsername(getUsername()).getEditorConfig();
        if (StringUtils.isBlank(editorConfigStr)) {
            return new EditorConfigDto();
        } else {
            return JsonUtil.stringToObj(editorConfigStr, EditorConfigDto.class);
        }

    }

    @Override
    public EditorConfigDto updateEditorConfig(EditorConfigReq req) {
        UserDo userDo = userRepository.findByUsername(getUsername());
        userDo.setEditorConfig(JsonUtil.objToString(req));
        userRepository.save(userDo);
        return req;
    }
}
