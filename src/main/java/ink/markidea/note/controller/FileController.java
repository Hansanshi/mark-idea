package ink.markidea.note.controller;

import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.service.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author hansanshi
 * @date 2020/1/28
 */
@RequestMapping("/api/file")
@RestController
public class FileController {
    @Autowired
    private IFileService fileService;

    @PostMapping("")
    public ServerResponse<String> upload(MultipartFile file){
        String filePath = fileService.upload(file);
        if (filePath == null){
            return ServerResponse.buildErrorResponse("upload file failed");
        }
        return ServerResponse.buildSuccessResponse(filePath);
    }

}
