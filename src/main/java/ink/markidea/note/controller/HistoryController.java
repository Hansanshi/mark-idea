package ink.markidea.note.controller;

import ink.markidea.note.entity.req.QueryHistoryContentReq;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
public class HistoryController {


    @Autowired
    private INoteService noteService;

    @PostMapping("queryHistoryContent")
    public ServerResponse<String> queryHistoryContent(@RequestBody QueryHistoryContentReq req) {
        return noteService.getNoteHistoryContent(req.getNotebookName(), req.getNoteTitle(), req.getVersionRef());
    }

}
