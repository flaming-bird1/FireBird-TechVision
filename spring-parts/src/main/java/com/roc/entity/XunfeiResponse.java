package com.roc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XunfeiResponse {
    private List<XunfeiChoice> choices;
    private String sid;  // 会话ID
}
