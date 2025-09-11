package com.roc.service.impl;

import com.roc.entity.*;
import com.roc.mapper.EmailMapper;
import com.roc.mapper.UserMapper;
import com.roc.service.UserService;
import com.roc.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
            } else if (!captcha.getVerificationCode().equals(registerDto.getVerificationCode())) {
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


    /**
     * 登录
     *
     * @param loginDTO 登录信息
     * @return
     */
    @Override
    public Result login(LoginDTO loginDTO) {
        try {
            // 1. 校验邮箱格式
            if (loginDTO.getEmail() == null || loginDTO.getEmail().trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            // 2. 校验密码有效性
            if (loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
                return Result.error("密码不能为空");
            }

            // 3. 查询用户信息
            User user = userMapper.findByEmail(loginDTO.getEmail());
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 4. 校验密码
            if (!user.getPassword().equals(loginDTO.getPassword())) {
                return Result.error("密码错误");
            }

            // 5. 生成JWT令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("username", user.getUsername());
            claims.put("email", user.getEmail());

            String token = JwtUtils.generateJwt(claims);

            // 6. 清理验证码记录
            emailMapper.deleteByEmail(loginDTO.getEmail());
            UserInfoDTO userInfo = new UserInfoDTO(
                    user.getUserId(),
                    user.getUsername(),
                    user.getAvatarUrl(),
                    user.getRole(),
                    user.getBio(),
                    user.getEmail(),
                    user.getBlogUrl(),
                    user.getGitcodeUrl(),
                    user.getBirthday(),
                    user.getGender(),
                    user.getAddress()
            );
            // 4. 返回用户信息和token (注意: 不返回密码等敏感信息)
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userInfo", userInfo);

            return Result.success(data);
        } catch (Exception e) {
            log.error("登录异常：{}", e.getMessage());
            return Result.error("系统繁忙，请稍后再试");
        }
    }
}
