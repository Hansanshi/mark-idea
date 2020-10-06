package ink.markidea.note.controller;

import ink.markidea.note.entity.req.NoteRequest;
import ink.markidea.note.entity.req.NoteSearchRequest;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.NoteVersionVo;
import ink.markidea.note.entity.vo.NoteVo;
import ink.markidea.note.entity.vo.NotebookVo;
import ink.markidea.note.service.INoteService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Note
 * @author hansanshi
 * @date 2019/12/26
 */
@RestController
@RequestMapping("/api/note")
public class NoteController {

    @Autowired
    private INoteService noteService;

    /**
     * save changes of note
     * @param notebookName
     * @param noteTitle
     * @param request
     * @return
     */
    @PostMapping("/{notebookName}/{noteTitle}")
    public ServerResponse saveNote(@PathVariable String notebookName,
                                   @PathVariable String noteTitle,
                                   @RequestBody NoteRequest request){

        if (StringUtils.isNotBlank(request.getVersionRef())){
            return noteService.resetAndGet(notebookName, noteTitle, request.getVersionRef());
        }
        return noteService.saveNote(noteTitle, notebookName, request.getContent());
    }

    @PostMapping("/search")
    public ServerResponse<List<NoteVo>> searchNote(NoteSearchRequest request){
        if (StringUtils.isBlank(request.getKeyWord())){
            throw new IllegalArgumentException();
        }
        return noteService.search(request.getKeyWord(), request.getSearchNotebookList());

    }

    /**
     * get all notebooks, including notes
     */
    @GetMapping("")
    public ServerResponse<List<NotebookVo>> getNotebooks(){
        List<String>  notebookNames = noteService.listNotebooks().getData();
        List<NotebookVo> notebookVoList = new ArrayList<>(notebookNames.size());
        for (String notebookName : notebookNames){
            NotebookVo notebookVo = new NotebookVo().setNotebookName(notebookName).setNoteList(new ArrayList<>());
            notebookVo.setNoteList(noteService.listNotes(notebookName).getData());
            notebookVoList.add(notebookVo);
        }
        return ServerResponse.buildSuccessResponse(notebookVoList);
    }

    /**
     * get all notes of a notebook
     * @param notebookName
     * @return
     */
    @GetMapping("/{notebookName}")
    public ServerResponse<List<NoteVo>> getNotes(@PathVariable String notebookName){
        return noteService.listNotes(notebookName);
    }


    /**
     * get content of note
     */
    @GetMapping("/{notebookName}/{noteTitle}")
    public ServerResponse<String> getNote(@PathVariable String notebookName, @PathVariable String noteTitle){
        return noteService.getNote(notebookName, noteTitle);
    }


    @GetMapping("/{notebookName}/{noteTitle}/history")
    public ServerResponse<List<NoteVersionVo>> getNoteHistory(@PathVariable String notebookName, @PathVariable String noteTitle){
        return noteService.getNoteHistory(notebookName, noteTitle);
    }

    /**
     * create a notebook
     */
    @PutMapping("/{notebookName}")
    public ServerResponse createNotebook(@PathVariable String notebookName){
        return noteService.createNotebook(notebookName);
    }


    /**
     * copy or move a note to other notebook
     */
    @PutMapping("/{targetNotebook}/{targetNoteTitle}")
    public ServerResponse copyOrMoveNote(@PathVariable String targetNotebook,
                                         @PathVariable String targetNoteTitle,
                                         @RequestBody NoteRequest request){
        String srcNotebook = request.getSrcNotebook();
        String srcTitle = request.getSrcTitle();
        if (request.getMove() != null && request.getMove()){
            return noteService.moveNote(srcNotebook, srcTitle, targetNotebook, targetNoteTitle);
        }else {
            return noteService.copyNote(srcNotebook, targetNotebook, targetNoteTitle);
        }
    }


    /**
     * delete a notebook
     * @param notebookName
     * @return
     */
    @DeleteMapping("/{notebookName}")
    public ServerResponse delNotebook(@PathVariable String notebookName){
        return noteService.deleteNotebook(notebookName);
    }

    /**
     * delete a note
     */
    @DeleteMapping("/{notebookName}/{noteTitle}")
    public ServerResponse delNote(@PathVariable String notebookName,
                                  @PathVariable String noteTitle){
        return noteService.deleteNote(notebookName, noteTitle);
    }





}
