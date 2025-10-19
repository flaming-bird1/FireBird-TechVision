package com.roc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attraction {

    private Integer id; // 主键ID

    private String name; // 景点名称

    private BigDecimal longitude; // 经度

    private BigDecimal latitude; // 纬度

    private String type; // 景点类型

    private String level; // 景点等级

    private String address; // 景点地址

    private BigDecimal ticketPrice; // 景点门票价格

    private String openTime; // 景点开放时间

    private String description; // 景点描述

    private LocalDateTime createdAt; // 创建时间

    private LocalDateTime updatedAt; // 更新时间
}
