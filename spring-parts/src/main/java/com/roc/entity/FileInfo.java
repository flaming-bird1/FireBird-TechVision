package com.roc.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {
    private Integer id;
    private String originalFilename; // 原始文件名
    private String newFilename; // 唯一文件名
    private String path;
    private Long fileSize;
    private Integer userId;
    private Integer folderId;
    private Date uploadTime;
    private Integer downloadCount;
    private String remark; // 文件备注
}
