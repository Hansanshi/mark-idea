package ink.markidea.note.controller;

import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.DeletedNoteVo;
import ink.markidea.note.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hansanshi
 * @date 2020/1/30
 */
@RestController
@RequestMapping("/api/delnote")
public class DelNoteController {

    @Autowired
    private INoteService noteService;

    /**
     * get all delnotes
     */
    @GetMapping("")
    public ServerResponse<List<DeletedNoteVo>> getDelNotes(){
        return noteService.listDelNotes();
    }

    /**
     * clear all delnotes
     * @return
     */
    @DeleteMapping("")
    public ServerResponse clearAllDelNotes(){
        return noteService.clearAllDelNotes();
    }

    /**
     * clear a single delnote
     * @param recover whether to recover the corresponding note , when the recordeis deleted.
     */
    @DeleteMapping("/{id}")
    public ServerResponse clearDelNote(@PathVariable Integer id, Boolean recover){
        if (recover != null && recover){
            return noteService.recoverNote(id);
        }else{
            return noteService.clearDelNote(id);
        }
    }

}
