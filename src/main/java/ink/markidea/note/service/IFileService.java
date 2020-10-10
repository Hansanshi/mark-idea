package ink.markidea.note.service;

import ink.markidea.note.entity.vo.UserFileVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;


/**
 * @author hansanshi
 */
public interface IFileService {

    /**
     * 上传文件
     * @param file 待上传文件
     * @return 文件的url
     */
    String upload(MultipartFile file);

    boolean writeStringToFile(String content, File targetFile);

    String getContentFromFile(File file);

    void deleteFile(File file);

    void batchDelete(List<String> fileNames);

    UserFileVo listUserFiles(int pageIndex, int pageSize);
}
