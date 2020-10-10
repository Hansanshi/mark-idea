package ink.markidea.note.context.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Weigher;
import ink.markidea.note.entity.dto.UserNoteKey;
import ink.markidea.note.entity.vo.UserVo;
import ink.markidea.note.service.IFileService;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Qualifier("userNoteCache")
    private LoadingCache<UserNoteKey, String> userNoteCache;

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
                .maximumWeight(125 * 1024 * 1024)
                .weigher(new Weigher<UserNoteKey, String>() {
                    @Override
                    public @NonNegative int weigh(@NonNull UserNoteKey key, @NonNull String value) {
                        return value.length();
                    }
                })
                .expireAfterWrite(12, TimeUnit.HOURS)
                .build(key -> loadNote(key.getUsername(), key.getNotebookName(), key.getNoteTitle()));
    }


    @Bean("userNotePreviewCache")
    public LoadingCache<UserNoteKey, String> userNotePreviewCache(){
        return Caffeine.newBuilder()
                .maximumWeight(10 * 1024 * 1024)
                .weigher(new Weigher<UserNoteKey, String>() {
                    @Override
                    public @NonNegative int weigh(@NonNull UserNoteKey key, @NonNull String value) {
                        return value.length();
                    }
                })
                .expireAfterWrite(12, TimeUnit.HOURS)
                .build(this::loadPreview);
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

    private String loadPreview(UserNoteKey key){
        String content = userNoteCache.get(key);
        if (content == null) {
            return  null;
        }
        return content.substring(0, Math.min(60, content.length()));
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
