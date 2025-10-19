package com.roc.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "roc.upload")
public class UploadConfig {
    private String path;      // 文件存储路径
    private String urlPrefix; // URL前缀
    private String accessUrl; // 新增：可直接访问的HTTP URL前缀
}
