package com.roc.service;

import com.roc.entity.LoginDTO;
import com.roc.entity.RegisterDTO;
import com.roc.entity.Result;
import com.roc.entity.User;

public interface UserService {
    Result register(RegisterDTO user);

    Result login(LoginDTO loginDTO);
}
