package com.roc.controller;

import com.roc.entity.Result;
import com.roc.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class EmailController {
    @Autowired
    private EmailService emailService;

    @GetMapping("/send-captcha")
    public Result sendCaptcha(String email) {
        log.info("发送验证码，邮箱为：{}---email", email);
        try {
            String info = emailService.sendVerificationCode(email);
            return Result.success(info);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
