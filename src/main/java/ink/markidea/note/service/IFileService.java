package ink.markidea.note.service;

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

    String getPreviewLines(File file);

    void deleteFile(File file);
}
