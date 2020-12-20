package ink.markidea.note.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

/**
 * @author hansanshi
 * @date 2020/12/15
 */
@Entity
@Table(name = "article")
@Getter
@Setter
@Accessors(chain = true)
public class ArticleDo {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "notebook",nullable = false)
    private String notebookName;

    @Column(name = "note_title",nullable = false)
    private String noteTitle;

    @Column(nullable = false)
    private String username;

}
