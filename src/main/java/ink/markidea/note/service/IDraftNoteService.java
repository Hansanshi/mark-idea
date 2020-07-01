package ink.markidea.note.service;

import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.DraftNoteVo;

import java.util.List;

/**
 * @author hansanshi
 * @date 2020/2/23
 */
public interface IDraftNoteService {
    ServerResponse<List<DraftNoteVo>> getDraftNotes();

    ServerResponse saveDraftNote(String notebookName, String title, String content);

    ServerResponse deleteDraftNote(String notebookName, String title);

    ServerResponse deleteDraftNotes();
}
