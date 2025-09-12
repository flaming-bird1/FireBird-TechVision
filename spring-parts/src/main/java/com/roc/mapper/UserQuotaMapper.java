package com.roc.mapper;

import com.roc.entity.UserQuota;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserQuotaMapper {
    UserQuota selectByUserId(Integer userId);

    void updateUserQuota(UserQuota userQuota);
}
