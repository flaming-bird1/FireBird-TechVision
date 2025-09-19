package com.roc.controller;

import com.roc.entity.Result;
import com.roc.service.WordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class WordController {
    @Autowired
    private WordService wordService;

    /**
     * 获取多个随机单词
     *
     * @param num 单词数量
     * @return 随机单词列表结果
     */
    @GetMapping("/word/random")
    public Result getRandomWords(@RequestParam Integer num) {
        log.info("获取随机单词");
        return wordService.getRandomWords(num);
    }
}
