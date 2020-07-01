package ink.markidea.note.service.impl;

import ink.markidea.note.dao.DraftNoteRepository;
import ink.markidea.note.entity.DraftNoteDo;
import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.vo.DraftNoteVo;
import ink.markidea.note.service.IDraftNoteService;
import ink.markidea.note.util.DateTimeUtil;
import ink.markidea.note.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author hansanshi
 * @date 2020/2/23
 * @description TODO
 */
@Service
public class DraftNoteServiceImpl implements IDraftNoteService {

    @Autowired
    private DraftNoteRepository draftNoteRepository;

    @Override
    public ServerResponse<List<DraftNoteVo>> getDraftNotes(){
        List<DraftNoteDo> draftNoteDoList = draftNoteRepository.findByUsername(getUsername());
        List<DraftNoteVo> draftNoteVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(draftNoteDoList)){
            draftNoteDoList.forEach( draftNoteDo -> {
                DraftNoteVo draftNoteVo = new DraftNoteVo();
                draftNoteVo.setId(draftNoteDo.getId())
                        .setNotebookName(draftNoteDo.getNotebookName())
                        .setTitle(draftNoteDo.getTitle())
                        .setContent(draftNoteDo.getContent())
                        .setUpdateTime(DateTimeUtil.dateToStr(draftNoteDo.getUpdateTime()));
                draftNoteVoList.add(draftNoteVo);
            });
        }
        return ServerResponse.buildSuccessResponse(draftNoteVoList);
    }

    @Override
    public ServerResponse saveDraftNote(String notebookName, String title, String content) {
        draftNoteRepository.save(new DraftNoteDo()
                .setUsername(getUsername())
                .setContent(content)
                .setNotebookName(notebookName)
                .setUpdateTime(new Date())
                .setTitle(title));
        return ServerResponse.buildSuccessResponse();
    }


    @Override
    public ServerResponse deleteDraftNote(String notebookName, String title) {
        draftNoteRepository.deleteByUsernameAndNotebookNameAndTitle(getUsername(),notebookName, title);
        return ServerResponse.buildSuccessResponse();
    }

    @Override
    public ServerResponse deleteDraftNotes() {
        draftNoteRepository.deleteByUsername(getUsername());
        return ServerResponse.buildSuccessResponse();
    }


    private String getUsername(){
        return ThreadLocalUtil.getUsername();
    }
}
