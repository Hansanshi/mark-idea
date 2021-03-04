package ink.markidea.note.context.config;

import com.github.benmanes.caffeine.cache.*;
import ink.markidea.note.entity.ArticleDo;
import ink.markidea.note.entity.dto.NotePreviewInfo;
import ink.markidea.note.entity.dto.UserNoteKey;
import ink.markidea.note.entity.vo.UserVo;
import ink.markidea.note.service.IArticleService;
import ink.markidea.note.service.IFileService;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author hansanshi
 * @date 2020/2/17
 */
@Configuration
public class CacheConfig implements EnvironmentAware {

    /**
     * token失效时间
     */
    private static volatile int tokenExpireTimeInHour;

    private static final long HOUR_DURATION_IN_NANO_SECONDS = 3600L * 1000L * 1000L;

    @Autowired
    private IFileService fileService;

    @Autowired
    private IArticleService articleService;

    @Autowired
    @Qualifier("userNoteCache")
    private LoadingCache<UserNoteKey, String> userNoteCache;

    @Value("${notesDir}")
    private String notesDir;

    @Bean
    public Cache<String, UserVo> userCache(){
        return Caffeine.newBuilder()
                        .maximumSize(500)
                .expireAfter(new Expiry<String, UserVo>() {
                    @Override
                    public long expireAfterCreate(@NonNull String key, @NonNull UserVo value, long currentTime) {
                        return currentTime + tokenExpireTimeInHour * HOUR_DURATION_IN_NANO_SECONDS;
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull String key, @NonNull UserVo value, long currentTime, @NonNegative long currentDuration) {
                        return currentTime + tokenExpireTimeInHour * HOUR_DURATION_IN_NANO_SECONDS;
                    }

                    @Override
                    public long expireAfterRead(@NonNull String key, @NonNull UserVo value, long currentTime, @NonNegative long currentDuration) {
                        return currentTime + tokenExpireTimeInHour * HOUR_DURATION_IN_NANO_SECONDS;
                    }
                }).build();
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
    public LoadingCache<UserNoteKey, NotePreviewInfo> userNotePreviewCache(){
        return Caffeine.newBuilder()
                .maximumWeight(10 * 1024 * 1024)
                .weigher(new Weigher<UserNoteKey, NotePreviewInfo>() {
                    @Override
                    public @NonNegative int weigh(@NonNull UserNoteKey key, @NonNull NotePreviewInfo value) {
                        return value.getPreviewContent() == null ? 0:value.getPreviewContent().length();
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

    private NotePreviewInfo loadPreview(UserNoteKey key){
        String content = userNoteCache.get(key);
        if (content == null) {
            return  null;
        }
        NotePreviewInfo previewInfo = new NotePreviewInfo().setPreviewContent(content.substring(0, Math.min(60, content.length())));
        ArticleDo articleDo = articleService.findByNotebookAndNoteTitle(key.getNotebookName(), key.getNoteTitle());
        if (articleDo != null) {
            previewInfo.setArticleId(articleDo.getId());
        }
        return previewInfo;
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

    public static void setTokenExpireTimeInHour(int tokenExpireTimeInHour) {
        CacheConfig.tokenExpireTimeInHour = tokenExpireTimeInHour;
    }

    @Override
    public void setEnvironment(Environment environment) {
        tokenExpireTimeInHour = Objects.requireNonNull(environment.getProperty("tokenExpireTimeInHour", Integer.class));
    }
}
