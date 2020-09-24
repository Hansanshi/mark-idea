package ink.markidea.note.entity.req;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 批量删除请求
 * @author hansanshi
 * @date 2020/9/24
 */
@Getter
@Setter
public class BatchFileDeleteRequest {

    private String username;

    private List<String> fileNames;

}
