package com.roc.entity.aichat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XunfeiRequest {
    private String model;
    private String user; // 用户唯一ID
    private List<XunfeiMessage> messages;
    private Double temperature;
    private Integer top_k;
    private Boolean stream;
    private Integer max_tokens;
    private Integer presence_penalty;
    private Integer frequency_penalty;
}
