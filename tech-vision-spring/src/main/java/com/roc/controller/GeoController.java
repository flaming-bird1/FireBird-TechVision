package com.roc.controller;

import com.roc.entity.Result;
import com.roc.service.GeoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class GeoController {
    @Autowired
    private GeoService geoService;

    /*
    获取成都市的20个景点信息
     */
    @GetMapping("/get-chengdu-attraction")
    public Result getChengduAttraction() {
        log.info("获取成都市20个景点信息");
        return geoService.getChengduAttraction();
    }
}
