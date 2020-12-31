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

    public static final int STATUS_TMP_SAVED = 2;

    private String title;

    private int status =  STATUS_PRIVATE;

    private String content;

    private String previewContent;

    private String lastModifiedTime;

    private String notebookName;

    private int searchCount;

    private Integer articleId;
}
