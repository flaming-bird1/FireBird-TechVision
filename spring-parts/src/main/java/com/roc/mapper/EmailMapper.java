package com.roc.mapper;


import com.roc.entity.EmailVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface EmailMapper {
    EmailVerification getCaptchaByEmail(String email);


    void insertCaptcha(
            @Param("email") String email,
            @Param("verificationCode") String verificationCode,
            @Param("expiresAt") LocalDateTime expiresAt
    );

    void updateCaptcha(
            @Param("email") String email,
            @Param("verificationCode") String randomCaptchaNumber,
            @Param("expiresAt") LocalDateTime expiresAt);
}
