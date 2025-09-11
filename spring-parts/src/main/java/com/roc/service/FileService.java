package com.roc.service;

import com.roc.entity.Result;
import com.roc.entity.UploadFileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    Result uploadFile(UploadFileDTO uploadFileDTO);
}
