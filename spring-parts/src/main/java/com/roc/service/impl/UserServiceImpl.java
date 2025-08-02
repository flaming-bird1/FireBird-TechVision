package com.roc.service.impl;

import com.roc.entity.EmailVerification;
import com.roc.entity.RegisterDTO;
import com.roc.entity.Result;
import com.roc.entity.User;
import com.roc.mapper.EmailMapper;
import com.roc.mapper.UserMapper;
import com.roc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailMapper emailMapper;


    /**
     * 注册用户
     *
     * @param registerDto
     * @return
     */
    @Override
    public Result register(RegisterDTO registerDto) {
        try {
            // 1. 校验邮箱
            if (registerDto.getEmail() == null || registerDto.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }

            // 2. 校验密码
            if (registerDto.getPassword() == null || registerDto.getPassword().trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            if (registerDto.getPassword().length() < 6 || registerDto.getPassword().length() > 20) {
                return Result.error("密码长度必须为6-20位");
            }

            // 3. 校验验证码
            if (registerDto.getVerificationCode() == null || registerDto.getVerificationCode().trim().isEmpty()) {
                return Result.error("验证码不能为空");
            }
            if (registerDto.getVerificationCode().length() != 6) {
                return Result.error("验证码长度必须为6位");
            }

            // 4. 业务逻辑（验证码检查、用户注册等）
            EmailVerification captcha = emailMapper.getCaptchaByEmail(registerDto.getEmail());
            log.info("captcha: {}", captcha);
            if (captcha == null) {
                return Result.error("验证码不存在");
            }else if (!captcha.getVerificationCode().equals(registerDto.getVerificationCode())) {
                return Result.error("验证码错误");
            } else if (captcha.getExpiresAt().isBefore(LocalDateTime.now())) {
                emailMapper.deleteByEmail(registerDto.getEmail());
                return Result.error("验证码已过期");
            }

            User user = new User();
            user.setEmail(registerDto.getEmail());
            // 默认设置邮箱号为用户名
            user.setUsername(registerDto.getEmail());
            user.setPassword(registerDto.getPassword());

            userMapper.insert(user);
            emailMapper.deleteByEmail(registerDto.getEmail());

            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("注册失败：{}", e.getMessage());
            return Result.error("注册失败，请稍后再试");
        }
    }
}
