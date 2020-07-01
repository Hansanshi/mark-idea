package ink.markidea.note.entity;

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

    @Column
    private String avatar;

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private String intro;

    /**
     * 默认0是管理员, 1是普通用户
     */
    @Column
    @ColumnDefault("1")
    private Integer status;

    @Column
    private String ext;

    @Column(name = "remote_repo")
    private String remoteRepository;

    /**
     * whether push to remote
     */
    @Column
    private boolean push = false;

    /** ssh 私钥 */
    @Column(name = "prv_key")
    private String prvKey;

    /** ssh 公钥 */
    @Column(name = "pub_key")
    private String pubKey;

}
