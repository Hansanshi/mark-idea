package ink.markidea.note.dao;

import ink.markidea.note.entity.ArticleDo;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleDo, Integer> {

    ArticleDo findByIdAndUsername(Integer id, String username);

    ArticleDo findFirstByNotebookNameAndNoteTitleAndUsername(String notebookName, String noteTitle, String username);

    List<ArticleDo> findAllByUsername(String username);

    Page<ArticleDo> findAllByUsername(String username, Pageable pageable);

    /** Param username is used to ensure record deleted by owner */
    @Transactional
    void deleteByIdAndUsername(Integer id, String username);

    @Transactional
    void deleteAllByUsernameAndIdIn(String username, List<Integer> ids);

    @Transactional
    void deleteAllByUsername(String username);

}
