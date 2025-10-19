package com.roc.service;

import com.roc.entity.Result;
import com.roc.entity.filemanage.UploadFileDTO;

public interface FileService {

    Result uploadFile(UploadFileDTO uploadFileDTO);
}
