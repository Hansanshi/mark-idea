package ink.markidea.note.entity.req;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NoteSearchRequest {

    String keyWord;
    List<String> searchNotebookList;
}
