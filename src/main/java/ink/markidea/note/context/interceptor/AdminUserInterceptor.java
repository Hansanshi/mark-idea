package ink.markidea.note.context.interceptor;

import ink.markidea.note.entity.exception.NoAuthorityException;
import ink.markidea.note.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author hansanshi
 * @date 2021/1/24
 */
@Component
@Slf4j
public class AdminUserInterceptor  implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        String token = request.getHeader("token");
        String username = request.getHeader("username");
        if (username == null || token == null || !userService.checkAdminUser(username, token)) {
            throw new NoAuthorityException();
        }
        return true;
    }
}
