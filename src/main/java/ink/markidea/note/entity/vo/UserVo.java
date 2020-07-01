package ink.markidea.note.entity.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author hansanshi
 * @date 2019/12/31
 */
@Getter
@Setter
@Accessors(chain = true)
public class UserVo {

    private String username;

    private String token;
}
