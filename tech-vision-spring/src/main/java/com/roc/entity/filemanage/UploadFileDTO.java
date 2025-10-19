package com.roc.entity.filemanage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileDTO {
    private  MultipartFile file; //  文件
    private  Integer userId;
    private String remark;
}
