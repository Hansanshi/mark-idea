package ink.markidea.note.entity.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author hansanshi
 */
@Getter
@Setter
@Accessors(chain = true)
public class NoteVersionVo {

    /** 引用  */
    private String ref;

    /** 提交日期 */
    private String date;

    private String msg;

}
