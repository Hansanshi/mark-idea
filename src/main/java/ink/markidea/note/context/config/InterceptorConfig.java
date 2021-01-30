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

    @Autowired
    private HandlerInterceptor adminUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorityInterceptor)
                .addPathPatterns("/api/note/**", "/api/note", "/api/user/**","/api/file", "/api/file/**")
                .addPathPatterns("/api/delnote/**", "/api/delnote")
                .addPathPatterns("/api/article", "/api/article/batchDel")
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/user/login","/api/user/register");


        registry.addInterceptor(httpRequestInterceptor)
                .addPathPatterns("/api/note/**","/api/note", "/api/admin/**")
                .addPathPatterns("/api/delnote/**", "/api/delnote")
                .addPathPatterns( "/api/file", "/api/file/**")
                .addPathPatterns("/api/user/**")
                .addPathPatterns("/api/**/**");

        registry.addInterceptor(adminUserInterceptor)
                .addPathPatterns("/api/admin/**");
    }

}
