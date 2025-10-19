package com.roc.mapper;

import com.roc.entity.Attraction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GeoMapper {
    List<Attraction> getChengduAttraction();
}
