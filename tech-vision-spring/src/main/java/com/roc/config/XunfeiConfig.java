package com.roc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "xunfei.spark")
@Getter
@Setter
public class XunfeiConfig {
    private String apiUrl;
    private String apiKey;  // 对应配置文件中的APIPassword
    private String model;
    private Double temperature;// 用于控制模型生成文本随机性和创造性的参数。
    private Integer maxTokens; // 注意配置文件是max-tokens
}
