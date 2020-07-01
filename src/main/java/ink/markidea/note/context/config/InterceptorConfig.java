package ink.markidea.note.context.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author hansanshi
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private HandlerInterceptor authorityInterceptor;

    @Autowired
    private HandlerInterceptor httpRequestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorityInterceptor)
                .addPathPatterns("/api/note/**", "/api/note", "/api/user/validate", "/api/user/logout", "/api/file", "/api/admin/**")
                .addPathPatterns("/api/delnote/**", "/api/delnote")
                .addPathPatterns("/api/draftNote", "/api/draftNote/**");


        registry.addInterceptor(httpRequestInterceptor)
                .addPathPatterns("/api/note/**","/api/note", "/api/admin/**")
                .addPathPatterns("/api/delnote/**", "/api/delnote")
                .addPathPatterns("/api/user/validate", "/api/user/logout", "/api/user/changePass")
                .addPathPatterns("/api/draftNote", "/api/draftNote/**");
    }

}
