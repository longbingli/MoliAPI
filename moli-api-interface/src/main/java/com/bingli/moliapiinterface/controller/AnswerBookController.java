package com.bingli.moliapiinterface.controller;

import com.bingli.moliapiinterface.model.dto.AnswerBookResponse;
import com.bingli.moliapiinterface.service.AnswerBookService;
import com.bingli.moliapiinterface.util.Result;
import com.bingli.moliapiinterface.util.ResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/answer-book")
public class AnswerBookController {

    @Resource
    private AnswerBookService answerBookService;

    @GetMapping("/query")
    public Result<AnswerBookResponse> query(@RequestParam("question") String question) {
        log.info("Answer book query, question={}", question);
        return ResultUtils.success(answerBookService.answer(question));
    }
}
