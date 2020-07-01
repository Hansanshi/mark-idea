package ink.markidea.note.dao;

import ink.markidea.note.entity.DelNoteDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author hansanshi
 * @date 2020/01/30
 */
@Repository
public interface DelNoteRepository extends JpaRepository<DelNoteDo, Integer> {

    /** Param username is used to ensure record deleted by owner */
    DelNoteDo findByIdAndUsername(Integer id, String username);

    List<DelNoteDo> findAllByUsername(String username);

    /** Param username is used to ensure record deleted by owner */
    @Transactional
    void deleteByIdAndUsername(Integer id, String username);

    @Transactional
    void deleteAllByUsername(String username);
}
