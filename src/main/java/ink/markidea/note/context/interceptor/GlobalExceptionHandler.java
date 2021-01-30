package ink.markidea.note.context.interceptor;

import ink.markidea.note.entity.exception.NoAuthorityException;
import ink.markidea.note.entity.exception.PromptException;
import ink.markidea.note.entity.resp.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @author hansanshi
 * @date 2019/10/6
 * @description
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public ServerResponse handleException(Exception e){
        log.error("exception ",e);
        return ServerResponse.buildErrorResponse("系统异常");
    }

    @ExceptionHandler(NullPointerException.class)
    public ServerResponse handleNullException(Exception e){
        log.error("Argument is null ",e);
        return ServerResponse.buildErrorResponse("参数为空");
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ServerResponse handleIllegalArgs(Exception e){
        log.error("Illegal arguments ", e);
        return ServerResponse.buildErrorResponse("非法参数");
    }

    @ExceptionHandler(NoAuthorityException.class)
    public ServerResponse handleNoAuthority(Exception e){
        return ServerResponse.buildErrorResponse(ServerResponse.ResponseCode.NEED_LOGIN.getCode(),"No authority");
    }

    @ExceptionHandler(PromptException.class)
    public ServerResponse handlePromptException(Exception e){
        log.error("exception ",e);
        return ServerResponse.buildErrorResponse(e.getMessage());
    }

}
