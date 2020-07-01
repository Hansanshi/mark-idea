package ink.markidea.note.context.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ink.markidea.note.entity.vo.UserVo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author hansanshi
 * @date 2020/2/17
 */
@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, UserVo> userCache(){
        return Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterAccess(2, TimeUnit.HOURS)
                        .build();
    }
}
