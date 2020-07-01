package ink.markidea.note.service.impl;

import ink.markidea.note.context.task.NoteTimer;
import ink.markidea.note.dao.UserRepository;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.service.IAdminService;
import ink.markidea.note.util.SshUtil;
import ink.markidea.note.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author hansanshi
 * @date 2020/1/27
 */
@Service
@Slf4j
public class AdminServiceImpl implements IAdminService {

    @Autowired
    private NoteTimer noteTimer;

    @Value("${sshKeysDir}")
    private String sshKeysDir;

    @Autowired
    private UserRepository userRepository;

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
        return ServerResponse.buildSuccessResponse();    }

    @Override
    public ServerResponse getRemoteRepoUrl() {
        String remoteRepoUrl = userRepository.findByUsername(getUsername()).getRemoteRepository();
        return ServerResponse.buildSuccessResponse(remoteRepoUrl);
    }

    private String getUsername(){
        return ThreadLocalUtil.getUsername();
    }

}
