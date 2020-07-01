package ink.markidea.note.entity.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author hansanshi
 * @date 2020/1/4
 */
@Getter
@Setter
@Accessors(chain = true)
public class NoteVo {

    public static final int STATUS_PRIVATE = 0;

    public static final int STATUS_PUBLIC = 1;

    private String title;

    private int status =  STATUS_PRIVATE;

    private String content;
}
