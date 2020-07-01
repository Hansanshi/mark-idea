package ink.markidea.note.entity.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author hansanshi
 * @date 2020/1/4
 */
@Getter
@Setter
@Accessors(chain = true)
public class NotebookVo {

    private String notebookName;

    private List<NoteVo> noteList;

}
