package com.roc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private Integer userId;
    private String username;
    private String avatarUrl;
    private String title;
    private String bio;
    private String email;
    private String blogUrl;
    private String gitcodeUrl;
    private Date birthday;
    private String gender;
    private String address;
}
