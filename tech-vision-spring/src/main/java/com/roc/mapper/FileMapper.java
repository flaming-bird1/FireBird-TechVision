package com.roc.mapper;

import com.roc.entity.filemanage.FileInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper {
    void insertFile(FileInfo fileInfo);
}
