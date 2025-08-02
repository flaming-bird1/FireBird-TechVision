package com.roc.service.impl;

import com.roc.entity.EmailVerification;
import com.roc.mapper.EmailMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
// @Transactional // 添加事务注解

public class EmailService implements com.roc.service.EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private EmailMapper emailMapper;
    private static final String FROM_EMAIL = "2739967874@qq.com";

    @Override
    public String sendVerificationCode(String email) {
        //     验证邮箱格式
        if (!isValidEmail(email)) {
            log.error("邮箱格式错误");
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        try {
            // 1. 生成随机验证码
            String randomCaptchaNumber = generateVerificationCode();
            // 验证码过期时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(5); // 5分钟后过期

            // 2. 构建邮件内容
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(email);
            message.setSubject("【焰翼码栈】验证码");
            message.setText("您的验证码是：" + randomCaptchaNumber + "，有效期5分钟。");

            // 3. 发送邮件
            mailSender.send(message);

            // 判断该邮箱是否存在，如果存在，则将该条验证码的信息对应的数据进行更新，如果不存在，则将该条信息插入到数据库中
            // 获得邮箱信息
            EmailVerification captcha = emailMapper.getCaptchaByEmail(email);
            if (captcha != null) {
                // 更新验证码
                emailMapper.updateCaptcha(email, randomCaptchaNumber, expiresAt);
            } else {
                // 插入验证码
                emailMapper.insertCaptcha(email, randomCaptchaNumber, expiresAt);
            }

            // 返回结果
            log.info("验证码发送成功");
            return "验证码发送成功";
        }catch (Exception e){
            log.error("验证码发送失败");
            return "验证码发送失败";
        }
    }


    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }

    /**
     * 使用正则表达式验证邮箱格式
     *
     * @param email
     * @return
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // 精确匹配：10位数字 + 严格小写@qq.com
        String strictRegex = "^\\d{10}@qq\\.com$";  // 注意：没有(?i)忽略大小写
        return email.matches(strictRegex);  // 直接调用String的matches方法
    }
}
