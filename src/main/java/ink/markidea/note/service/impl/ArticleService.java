package ink.markidea.note.service.impl;

import com.github.benmanes.caffeine.cache.LoadingCache;
import ink.markidea.note.dao.ArticleRepository;
import ink.markidea.note.entity.ArticleDo;
import ink.markidea.note.entity.dto.NotePreviewInfo;
import ink.markidea.note.entity.dto.UserNoteKey;
import ink.markidea.note.entity.vo.ArticleVo;
import ink.markidea.note.service.IArticleService;
import ink.markidea.note.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author hansanshi
 * @date 2020/12/15
 * @description TODO
 */
@Service
public class ArticleService implements IArticleService {

    @Autowired
    @Qualifier("userNoteCache")
    LoadingCache<UserNoteKey, String> userNoteCache;


    @Autowired
    @Qualifier("userNotePreviewCache")
    LoadingCache<UserNoteKey, NotePreviewInfo> userNotePreviewCache;


    @Autowired
    ArticleRepository articleRepository;

    @Override
    public ArticleDo findByNotebookAndNoteTitle(String notebookName, String noteTitle) {
        return articleRepository.findFirstByNotebookNameAndNoteTitleAndUsername(notebookName, noteTitle, getUsername());
    }

    @Override
    public ArticleVo findByArticleIdAndUsername(Integer articleId, String username) {
        return convert(articleRepository.findByIdAndUsername(articleId, username));
    }

    @Override
    public Page<ArticleVo> listArticles(Integer page, Integer size) {
        return listArticles(getUsername(), page , size);
    }

    @Override
    public Page<ArticleVo> listArticles(String username, Integer page, Integer size) {
        return articleRepository.findAllByUsername(username, PageRequest.of(page, size)).map(this::convert);
    }

    @Override
    public ArticleVo findOrCreateArticle(String notebookName, String noteTitle) {
        NotePreviewInfo notePreviewInfo = userNotePreviewCache.get(buildUserNoteKey(notebookName, noteTitle, getUsername()));
        if (notePreviewInfo.getArticleId() != null) {
            return new ArticleVo().setArticleId(notePreviewInfo.getArticleId());
        }
        ArticleDo articleDo = new ArticleDo()
                .setNotebookName(notebookName)
                .setNoteTitle(noteTitle)
                .setUsername(getUsername());
        articleRepository.save(articleDo);
        invalidateCache(buildUserNoteKey(notebookName, noteTitle));
        return convert(articleDo);
    }


    @Override
    @Transactional
    public void moveArticle(Integer articleId, String targetNotebook, String targetNoteTitle) {
        ArticleDo articleDo = articleRepository.findByIdAndUsername(articleId, getUsername());
        if (articleDo == null) {
            return ;
        }
        articleDo.setNotebookName(targetNotebook).setNoteTitle(targetNoteTitle);
        articleRepository.save(articleDo);
    }

    @Override
    public void deleteArticle(Integer articleId) {
        articleRepository.deleteByIdAndUsername(articleId, getUsername());
    }

    @Override
    public void batchDeleteArticle(List<ArticleVo> articleList) {
        articleList.forEach(articleVo ->
                userNotePreviewCache.invalidate(buildUserNoteKey(articleVo.getNotebookName(), articleVo.getNoteTitle())));
        articleRepository.deleteAllByUsernameAndIdIn(getUsername(),
                articleList.stream().map(ArticleVo::getArticleId).collect(Collectors.toList()));
    }

    private String getUsername(){
        return ThreadLocalUtil.getUsername();
    }


    private UserNoteKey buildUserNoteKey(String notebookName, String noteTitle){
        return buildUserNoteKey(notebookName, noteTitle, getUsername());
    }


    private UserNoteKey buildUserNoteKey(String notebookName, String noteTitle, String username){
        return new UserNoteKey().setNotebookName(notebookName).setNoteTitle(noteTitle).setUsername(username);
    }

    void invalidateCache(UserNoteKey key){
        userNotePreviewCache.invalidate(key);
        userNoteCache.invalidate(key);
    }


    ArticleVo convert(ArticleDo articleDo) {
        if (articleDo == null) {
            return null;
        }
        return new ArticleVo().setArticleId(articleDo.getId())
                .setNotebookName(articleDo.getNotebookName())
                .setNoteTitle(articleDo.getNoteTitle());
    }

}
