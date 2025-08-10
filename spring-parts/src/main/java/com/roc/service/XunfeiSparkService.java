package com.roc.service;

import com.roc.entity.Result;

public interface XunfeiSparkService {
    /**
     * 同步调用讯飞星火API
     */
    Result chatSync(String prompt);
}
