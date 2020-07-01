package ink.markidea.note.dao;

import ink.markidea.note.entity.UserDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author hansanshi
 * @date 2019/12/19
 */
@Repository
public interface UserRepository extends JpaRepository<UserDo,Integer> {

    UserDo findByUsername(String username);

    UserDo findByUsernameAndPassword(String username, String password);

    UserDo findFirstByStatus(Integer status);


}
