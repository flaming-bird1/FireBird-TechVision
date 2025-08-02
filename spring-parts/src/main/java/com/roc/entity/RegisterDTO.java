package com.roc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    private String email;  // 邮箱
    private String password;  // 密码
    private String verificationCode;  // 验证码
}
