package com.roc.service.impl;

import com.roc.config.XunfeiConfig;
import com.roc.entity.Result;
import com.roc.entity.aichat.XunfeiMessage;
import com.roc.entity.aichat.XunfeiRequest;
import com.roc.entity.aichat.XunfeiResponse;
import com.roc.service.XunfeiSparkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class XunfeiSparkServiceImpl implements XunfeiSparkService {
    private final RestTemplate restTemplate;
    private final XunfeiConfig config;

    @Autowired
    public XunfeiSparkServiceImpl(RestTemplate restTemplate, XunfeiConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    public Result chatSync(String prompt) {
        try {
            // 1. 构建请求体（使用配置类参数）
            XunfeiRequest request = new XunfeiRequest(
                    config.getModel(),       // 从配置读取模型版本
                    "user123",               // 用户唯一ID
                    List.of(new XunfeiMessage("user", prompt)),
                    config.getTemperature(), // 从配置读取
                    4,                      // top_k（默认值）
                    false,                  // stream（同步调用设为false）
                    config.getMaxTokens(),  // 从配置读取
                    1,                      // presence_penalty（默认值）
                    1                       // frequency_penalty（默认值）
            );

            // 2. 发送请求
            ResponseEntity<XunfeiResponse> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(request, buildHeaders()),
                    XunfeiResponse.class
            );

            // 3. 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 获取AI响应结果
                String content = response.getBody().getChoices().get(0).getMessage().getContent();
                return Result.success(content); // 直接返回成功Result
            } else {
                log.error("讯飞API返回非成功状态码: {}", response.getStatusCode());
                return Result.error("AI服务返回异常状态: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            log.error("HTTP调用失败，状态码: {}，响应体: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Result.error("AI服务请求错误: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("系统异常 - 类型: {}，信息: {}", e.getClass().getSimpleName(), e.getMessage());
            return Result.error("系统繁忙，请稍后重试");
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey()); // 使用配置的API Key
        return headers;
    }

    // private String extractContent(ResponseEntity<XunfeiResponse> response) {
    //     if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
    //         // 获得AI响应结果
    //         return response.getBody().getChoices().get(0).getMessage().getContent();
    //     }
    //     throw new RuntimeException("API调用失败，状态码: " + response.getStatusCode());
    // }
}