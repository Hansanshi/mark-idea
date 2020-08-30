package ink.markidea.note.controller;

import ink.markidea.note.entity.resp.ServerResponse;
import ink.markidea.note.entity.resp.VditorFileUploadResponse;
import ink.markidea.note.service.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    @PostMapping("vditor")
    public VditorFileUploadResponse upload(StandardMultipartHttpServletRequest request){
        List<MultipartFile> files = request.getMultiFileMap().get("file[]");
        Map<String, String> succFileMap = new HashMap<>();
        List<String> errorFileList = new ArrayList<>();
        files.forEach(file -> {
            try{
                succFileMap.put(file.getOriginalFilename(), fileService.upload(file));
            }catch (Exception e){
                errorFileList.add(file.getOriginalFilename());
            }
        });
        VditorFileUploadResponse response = new VditorFileUploadResponse();
        response.setData(new HashMap<>());
        response.getData().put("errFiles", errorFileList);
        response.getData().put("succMap", succFileMap);
        return response;
    }

}
