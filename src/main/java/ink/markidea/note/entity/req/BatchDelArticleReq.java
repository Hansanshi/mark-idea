package ink.markidea.note.entity.req;

import ink.markidea.note.entity.vo.ArticleVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hansanshi
 * @date 2020/12/17
 * @description TODO
 */
@Getter
@Setter
public class BatchDelArticleReq {


    private List<ArticleVo> articleList;

}
