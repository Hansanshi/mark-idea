package ink.markidea.note.entity.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author hansanshi
 * @date 2020/9/13
 */
@Getter
@Setter
@Accessors(chain = true)
public class UserFileVo {


    /**
     * 文件总数
     */
    int totalSize;

    /**
     * 页数，即共计多少页
     */
    int pageNum;

    /**
     * 每页展示数量大小
     */
    int pageSize;

    /**
     * 第几页
     */
    int pageIndex;

    List<FileDetailVo> fileDetailList;

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class FileDetailVo{

        /**
         * 最后修改时间
         */
        String lastModifiedTime;

        /**
         * 文件大小
         */
        String fileSize;

        /**
         * 文件名
         */
        String fileName;
    }

}
