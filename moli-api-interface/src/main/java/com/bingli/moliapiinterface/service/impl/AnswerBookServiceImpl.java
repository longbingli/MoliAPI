package com.bingli.moliapiinterface.service.impl;

import com.bingli.moliapiinterface.exception.BusinessException;
import com.bingli.moliapiinterface.mapper.AnswerBookMapper;
import com.bingli.moliapiinterface.model.dto.AnswerBookResponse;
import com.bingli.moliapiinterface.model.entity.AnswerBook;
import com.bingli.moliapiinterface.service.AnswerBookService;
import com.bingli.moliapiinterface.util.ResponseEnum;
import org.springframework.stereotype.Service;

@Service
public class AnswerBookServiceImpl implements AnswerBookService {

    private final AnswerBookMapper answerBookMapper;

    public AnswerBookServiceImpl(AnswerBookMapper answerBookMapper) {
        this.answerBookMapper = answerBookMapper;
    }

    @Override
    public AnswerBookResponse answer(String question) {
        if (question == null || question.trim().isEmpty()) {
            throw new BusinessException(ResponseEnum.PARAM_ERROR.getCode(), "question不能为空");
        }

        AnswerBook answerBook = answerBookMapper.selectRandomOne();
        if (answerBook == null || answerBook.getIsDelete() == 1) {
            throw new BusinessException(ResponseEnum.DATA_NOT_FOUND);
        }

        AnswerBookResponse response = new AnswerBookResponse();
        response.setQuestion(question.trim());
        response.setAnswer(answerBook.getAnswer());
        return response;
    }
}
