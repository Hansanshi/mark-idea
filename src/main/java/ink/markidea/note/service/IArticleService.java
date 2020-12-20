package ink.markidea.note.service;

import ink.markidea.note.entity.ArticleDo;
import ink.markidea.note.entity.vo.ArticleVo;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IArticleService {

     ArticleDo findByNotebookAndNoteTitle(String notebookName, String noteTitle);

     ArticleVo findByArticleTileAndUsername(String articleTitle, String username);

     ArticleVo findByArticleIdAndUsername(Integer articleId, String username);

     Page<ArticleVo> listArticles(Integer page, Integer size);

     Page<ArticleVo> listArticles(String username, Integer page, Integer size);

     ArticleVo findOrCreateArticle(String notebookName, String noteTitle);

     void moveArticle(Integer articleId, String targetNotebook, String targetNoteTitle);

     void deleteArticle(Integer articleId);

     void batchDeleteArticle(List<ArticleVo> idList);

}
