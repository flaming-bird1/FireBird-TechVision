package com.roc.controller;


import com.roc.entity.RegisterDTO;
import com.roc.entity.Result;
import com.roc.entity.User;
import com.roc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;


    /**
     * 注册
     *
     * @param registerDto 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDto) {
        log.info("开始处理用户注册请求：{}", registerDto);
        return userService.register(registerDto);
    }
}
