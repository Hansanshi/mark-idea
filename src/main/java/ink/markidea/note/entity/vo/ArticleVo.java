package ink.markidea.note.entity.vo;

import ink.markidea.note.entity.ArticleDo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * @author hansanshi
 * @date 2020/12/15
 * @description TODO
 */
@Getter
@Setter
@Accessors(chain = true)
public class ArticleVo  {

    private Integer articleId;

    private String articleTitle;

    private String notebookName;

    private String noteTitle;

    private String content;
}
