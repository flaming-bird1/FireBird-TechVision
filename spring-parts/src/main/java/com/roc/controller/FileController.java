package com.roc.controller;

import com.roc.entity.Result;
import com.roc.entity.UploadFileDTO;
import com.roc.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class FileController {
    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     * MultipartFile file
     * Integer userId
     * String remark
     *
     * @return 文件上传结果
     */
    // @RequestBody 只能解析 JSON/XML，无法处理 multipart/form-data
    @PostMapping("/file/upload")
    public Result uploadFile(@RequestParam("file") MultipartFile file,       // 文件参数
                             @RequestParam("userId") Integer userId,         // 其他字段
                             @RequestParam(value = "remark", required = false) String remark) {
        UploadFileDTO uploadFileDTO = new UploadFileDTO(file, userId, remark);
        log.info("开始处理文件上传请求：{}", uploadFileDTO);
        return fileService.uploadFile(uploadFileDTO);
    }
}
