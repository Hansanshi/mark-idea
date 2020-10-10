package ink.markidea.note.context.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import ink.markidea.note.entity.dto.UserNoteKey;
import ink.markidea.note.entity.vo.UserVo;
import ink.markidea.note.service.IFileService;
import ink.markidea.note.util.GitUtil;
import ink.markidea.note.util.ThreadLocalUtil;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author hansanshi
 * @date 2020/2/17
 */
@Configuration
public class CacheConfig {

    @Autowired
    private IFileService fileService;

    @Value("${notesDir}")
    private String notesDir;

    @Bean
    public Cache<String, UserVo> userCache(){
        return Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterAccess(2, TimeUnit.HOURS)
                        .build();
    }

    @Bean("userNoteCache")
    public LoadingCache<UserNoteKey, String> userNoteCache(){
        return Caffeine.newBuilder()
                .maximumSize(2000)
//                .expireAfterAccess(1, TimeUnit.DAYS)
                .build(key -> loadNote(key.getUsername(), key.getNotebookName(), key.getNoteTitle()));
    }

    private String loadNote(String username, String notebookName, String noteTitle) {
        String relativeFileName = getRelativeFileName(notebookName,noteTitle);
        File noteFile = new File(getOrCreateUserNotebookDir(username), relativeFileName);
        if (!noteFile.exists()) {
            return  null;
        }
        String content = fileService.getContentFromFile(noteFile);
        return content;
    }


    private File getOrCreateUserNotebookDir(String username){
        File dir = new File(notesDir, username);
        if (dir.exists()){
            return dir;
        }
        dir.mkdir();
        return dir;
    }

    private String getRelativeFileName(String notebookName, String noteTitle) {

        return notebookName + "/" + noteTitle+".md";
    }
}
