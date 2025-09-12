package com.roc.mapper;

import com.roc.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper {
    void insertFile(FileInfo fileInfo);
}
