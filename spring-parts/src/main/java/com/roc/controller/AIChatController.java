package com.roc.controller;

import com.roc.entity.Result;
import com.roc.service.XunfeiSparkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
// @RequestMapping("/api/chat") // 添加统一前缀
public class AIChatController {
    private final XunfeiSparkService sparkService;

    // 构造器注入（推荐方式）
    public AIChatController(XunfeiSparkService sparkService) {
        this.sparkService = sparkService;
    }

    // @PostMapping
    // public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
    //     try {
    //         String content = sparkService.chatSync(request.get("prompt"));
    //         return ResponseEntity.ok(Collections.singletonMap("content", content));
    //     } catch (Exception e) {
    //         log.error("API调用异常", e);
    //         return ResponseEntity.internalServerError().body(e.getMessage());
    //     }
    // }

    @GetMapping("/AI-chat")
    public Result chat(String param) {
        log.info("param:{}", param);
        return sparkService.chatSync(param);
    }
}