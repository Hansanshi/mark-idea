package ink.markidea.note.controller;

import ink.markidea.note.entity.req.ArticleCreateReq;
import ink.markidea.note.entity.req.BatchDelArticleReq;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.ArticleVo;
import ink.markidea.note.service.IArticleService;
import ink.markidea.note.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author hansanshi
 * @date 2020/12/15
 * 统一用POST  发现rest不适合批量操作
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    IArticleService articleService;


    @Autowired
    INoteService noteService;

    @GetMapping("")
    ServerResponse<Page<ArticleVo>> listArticles(Integer page, Integer size) {
        return ServerResponse.buildSuccessResponse(articleService.listArticles(page, size));
    }

    @GetMapping("/{author}/{articleId}")
    ServerResponse<ArticleVo>  getArticle(@PathVariable String author,
                                          @PathVariable Integer articleId) {
        ArticleVo articleVo = articleService.findByArticleIdAndUsername(articleId, author);
        if (articleVo == null) {
            return ServerResponse.buildErrorResponse("未找到");
        }
        ServerResponse<String> response = noteService.getNote(articleVo.getNotebookName(), articleVo.getNoteTitle(), author);
        articleVo.setContent(response.getData());
        return ServerResponse.buildSuccessResponse(articleVo);

    }

    @PostMapping("")
    ServerResponse<ArticleVo> createArticle(@RequestBody ArticleCreateReq req) {
        ArticleVo articleVo = articleService.findOrCreateArticle(req.getNotebookName(), req.getNoteTitle());
        return ServerResponse.buildSuccessResponse(articleVo);
    }


    @PostMapping("/batchDel")
    ServerResponse batchDeleteArticle(@RequestBody BatchDelArticleReq req) {
        if (CollectionUtils.isEmpty(req.getArticleList())) {
            return ServerResponse.buildErrorResponse("为空");
        }
        articleService.batchDeleteArticle(req.getArticleList());
        return ServerResponse.buildSuccessResponse();
    }

    @GetMapping("/{author}")
    ServerResponse<Page<ArticleVo>> listUserArticles(@PathVariable String author, Integer pageIndex, Integer pageSize) {
        return ServerResponse.buildSuccessResponse(articleService.listArticles(author, pageIndex, pageSize));
    }

}
