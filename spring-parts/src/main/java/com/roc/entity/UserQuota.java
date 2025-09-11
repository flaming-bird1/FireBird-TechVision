package com.roc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQuota {
    private Integer id; // 主键ID
    private Integer userId; // 用户ID
    private Long totalQuota; // 总额度
    private Long usedQuota; // 已使用额度
}
