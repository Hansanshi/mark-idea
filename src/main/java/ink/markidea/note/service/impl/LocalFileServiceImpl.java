package ink.markidea.note.service.impl;

import ink.markidea.note.service.IFileService;
import ink.markidea.note.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author hansanshi
 * @date 2019/12/23
 */
@Service
@Slf4j
public class LocalFileServiceImpl implements IFileService {

    @Value("${fileDir}")
    private String filePath;

    private final String DIR_PREFIX = "/file/";

    @Override
    public String upload(MultipartFile sourceFile) {
        String filename = sourceFile.getOriginalFilename();

        //获取扩展名
        String fileExtensionName = filename.substring(filename.indexOf(".")+1);
        //防止可能上传同样文件名的文件 TODO
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        log.info("开始上传文件，上传文件的文件名:{}，上传的路径:{}，新文件名:{}",filename,filePath,uploadFileName);

        File targetFile = new File(filePath, uploadFileName);

        //上传文件
        try {
            sourceFile.transferTo(targetFile);
        } catch (IOException e) {
            log.error("上传文件异常",e);
            return null;
        }

        return DIR_PREFIX + targetFile.getName();
    }

    @Override
    public boolean writeStringToFile(String content, File targetFile){
        return FileUtil.writeStringToFile(content, targetFile);
    }



    @Override
    public String getContentFromFile(File file){
        return FileUtil.readFileAsString(file);
    }

    @Override
    public void deleteFile(File file){
        FileUtil.deleteFileOrDirectory(file);
    }
}
