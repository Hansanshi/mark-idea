package ink.markidea.note.entity.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * @author hansanshi
 * @date 2020/2/23
 */
@Getter
@Setter
@Accessors(chain = true)
public class DraftNoteVo {

    private Integer id;

    private String notebookName;

    private String title;

    private String updateTime;

    private String content;
}
