package ink.markidea.note.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Date;

/**
 * @author hansanshi
 * @date 2020/2/21
 */
@Entity
@Table(name = "draft_note")
@Getter
@Setter
@Accessors(chain = true)
public class DraftNoteDo {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, name = "notebook")
    private String notebookName;

    @Column(nullable = false, name = "title")
    private String title;

    @Column(nullable = false, name = "content")
    private String content;

    @Column(nullable = false, name = "update_time")
    private Date updateTime;

}
