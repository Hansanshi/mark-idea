package ink.markidea.note.entity.req;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author hansanshi
 * @date 2020/1/24
 */
@Getter
@Setter
@Accessors(chain = true)
public class UserRequest {

    private String username;

    private String password;

    private String token;

    private String newPassword;
}
