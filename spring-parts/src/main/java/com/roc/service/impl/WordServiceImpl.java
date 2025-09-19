package com.roc.service.impl;

import com.roc.entity.Result;
import com.roc.entity.wordmanage.Word;
import com.roc.mapper.WordMapper;
import com.roc.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordServiceImpl implements WordService {
    @Autowired
    private WordMapper wordMapper;

    /**
     * 获取多个随机单词
     * @param num 单词数量
     * @return 随机单词列表结果
     */
    @Override
    public Result getRandomWords(Integer num) {
        try {
            // 参数校验
            if (num == null || num <= 0) {
                return Result.error("单词数量必须大于0");
            }

            // 限制最大数量，防止过多数据请求
            int maxNum = Math.min(num, 100); // 最多返回100个单词

            List<Word> randomWords = wordMapper.getRandomWords(maxNum);
            if (randomWords != null && !randomWords.isEmpty()) {
                return Result.success(randomWords);
            } else {
                return Result.error("未找到单词数据");
            }
        } catch (Exception e) {
            return Result.error("获取随机单词失败: " + e.getMessage());
        }
    }

}
