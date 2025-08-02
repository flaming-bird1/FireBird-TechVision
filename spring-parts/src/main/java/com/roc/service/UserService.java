package com.roc.service;

import com.roc.entity.RegisterDTO;
import com.roc.entity.Result;

public interface UserService {
    Result register(RegisterDTO user);
}
