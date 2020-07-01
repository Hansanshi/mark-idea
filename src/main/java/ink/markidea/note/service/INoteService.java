package ink.markidea.note.service;


import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.DeletedNoteVo;
import ink.markidea.note.entity.vo.NoteVersionVo;
import ink.markidea.note.entity.vo.NoteVo;

import java.util.List;

/**
 * @author hansanshi
 */
public interface INoteService {

    /**
     * list user's all notebooks and their notes
     */
    ServerResponse<List<String>> listNotebooks();

    /**
     * list all notes of the notebook
     */
    ServerResponse<List<NoteVo>> listNotes(String notebookName);

    /** create a notebook */
    ServerResponse createNotebook(String notebookName);

    ServerResponse createNote(String noteTitle, String notebookName, String content);

    /**
     * save changes of note.
     * If the note don't exist, it will be created.
     */
    ServerResponse saveNote(String noteTitle, String notebookName, String content);

    /**
     * delete note
     */
    ServerResponse deleteNote(String notebookName, String noteTitle);


    /**
     * get the content of note
     */
    ServerResponse<String> getNote(String notebookName, String noteTitle);

    /**
     * get version history of note
     */
    ServerResponse<List<NoteVersionVo>> getNoteHistory(String notebookName, String noteTitle);

    /**
     * recover note to a certain version
     */
    ServerResponse<String> resetAndGet(String notebookName, String noteTitle, String versionRef);

    /**
     * delete notebook and its all notes
     */
    ServerResponse deleteNotebook(String notebookName);

    /**
     * list all delnotes
     */
    ServerResponse<List<DeletedNoteVo>> listDelNotes();

    /** recover deleted note.
     * if the corresponding notebook is deleted, it will be created
     */
    ServerResponse recoverNote(Integer id);

    /**
     * clear delnote record
     */
    ServerResponse clearDelNote(Integer id);


    /**
     * move a note
     */
    ServerResponse moveNote(String srcNotebook, String srcTitle, String targetNotebook, String targetTitle);

    /**
     * clear all delnotes by username
     */
    ServerResponse clearAllDelNotes();

    /**
     * copy a note
     * @param srcNotebook
     * @param targetNotebook
     * @param title
     * @return
     */
    ServerResponse copyNote(String srcNotebook, String targetNotebook, String title);


}
