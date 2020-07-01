package ink.markidea.note.entity.req;

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
public class DraftNoteRequest {

    private String title;

    private String notebookName;

    private String content;

}
