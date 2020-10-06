package ink.markidea.note.entity.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
public class UserNoteKey {

    private String username;

    private String notebookName;

    private String noteTitle;

}
