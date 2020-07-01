package ink.markidea.note.controller;

import ink.markidea.note.entity.req.DraftNoteRequest;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.DraftNoteVo;
import ink.markidea.note.service.IDraftNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hansanshi
 * @date 2020/2/23
 */
@RequestMapping("/api/draftNote")
@RestController
public class DraftNoteController {

    @Autowired
    private IDraftNoteService draftNoteService;

    @GetMapping("")
    public ServerResponse<List<DraftNoteVo>> getDraftNotes(){
        return draftNoteService.getDraftNotes();
    }

    @DeleteMapping("")
    public ServerResponse delDraftNotes(){
        return draftNoteService.deleteDraftNotes();
    }

    @PostMapping("/{notebookName}/{title}")
    public ServerResponse saveOrUpdateDraftNote(@PathVariable String notebookName,
                                                @PathVariable String title,
                                                @RequestBody DraftNoteRequest request){
        return draftNoteService.saveDraftNote(notebookName, title, request.getContent());
    }

    @DeleteMapping("/{notebookName}/{title}")
    public ServerResponse deleteDraftNote(@PathVariable String notebookName,
                                          @PathVariable String title){
        return draftNoteService.deleteDraftNote(notebookName, title);
    }





}
