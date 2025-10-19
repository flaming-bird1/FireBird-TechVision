package com.roc.service.impl;

import com.roc.entity.Attraction;
import com.roc.entity.Result;
import com.roc.mapper.GeoMapper;
import com.roc.service.GeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeoServiceImpl implements GeoService {
    @Autowired
    private GeoMapper geoMapper;
    @Override
    public Result getChengduAttraction() {
        try {
            List<Attraction> chengduAttractions = geoMapper.getChengduAttraction();
            if (chengduAttractions != null && !chengduAttractions.isEmpty()){
                return Result.success(chengduAttractions);
            }else {
                return Result.error("未找到成都景点数据");
            }
        } catch (Exception e) {
            return Result.error("获取成都景点数据失败: " + e.getMessage());
        }
    }
}
