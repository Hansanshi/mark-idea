package ink.markidea.note.entity.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author hansanshi
 * @date 2019/12/26
 * @description TODO
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerResponse<T> {

    private Integer code;

    private String msg;

    private T data;

    private ServerResponse(Integer code){
        this.code = code;
    }

    private ServerResponse(Integer code, T data){
        this.code = code;
        this.data = data;
    }

    private ServerResponse(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }


    public static <T> ServerResponse<T> buildSuccessResponse(T data){
        return new ServerResponse<>(ResponseCode.SUCCESS.getCode(),data);
    }

    public static ServerResponse buildSuccessResponse(){
        return new ServerResponse(ResponseCode.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> buildErrorResponse(String msg){
        return new ServerResponse(ResponseCode.ERROR.getCode(),msg);
    }

    public static ServerResponse buildErrorResponse(int code, String msg){
        return new ServerResponse(code,msg);
    }

    public boolean isSuccess(){
        return Integer.valueOf(0).equals(code);
    }

    @Getter
    public enum ResponseCode {

        // 成功
        SUCCESS(0),

        // 错误
        ERROR(1),

        // 需要登录
        NEED_LOGIN(2);

        private int code;

        ResponseCode(int code){
            this.code = code;
        }
    }

}
