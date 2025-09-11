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
     *
     * @param uploadFileDTO 文件上传信息
     * @return 文件上传结果
     */
    @PostMapping("/file/upload")
    public Result uploadFile(@RequestBody UploadFileDTO uploadFileDTO) {
        log.info("开始处理文件上传请求：{}", uploadFileDTO);
        return fileService.uploadFile(uploadFileDTO);
    }
}
