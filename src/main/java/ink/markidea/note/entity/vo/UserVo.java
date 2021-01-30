package ink.markidea.note.entity.vo;

import ink.markidea.note.constant.UserConstant;
import ink.markidea.note.entity.dto.EditorConfigDto;
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

    /**
     * 用户类型
     */
    private int type = UserConstant.COMMON_USER;

    private EditorConfigDto editorConfig = new EditorConfigDto();
}
