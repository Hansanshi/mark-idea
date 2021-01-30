package ink.markidea.note.entity;

import ink.markidea.note.constant.UserConstant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

/**
 * @author hansanshi
 * @date 2019/12/19
 * @description TODO
 */

@Entity
@Table(name = "user")
@Getter
@Setter
@Accessors(chain = true)
public class UserDo {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    /**
     * 默认0是管理员, 1是普通用户
     */
    @Column
    @ColumnDefault("1")
    private Integer status = UserConstant.COMMON_USER;

    @Column(name = "remote_repo")
    private String remoteRepository;

    /**
     * whether push to remote
     */
    @Column
    private boolean push = false;


    /** 编辑器配置 */
    @Column(name = "editor_config")
    private String editorConfig;
}
