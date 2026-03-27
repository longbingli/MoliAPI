package com.bingli.moliapiinterface.service.impl;

import com.bingli.moliapiinterface.exception.BusinessException;
import com.bingli.moliapiinterface.mapper.PoetryMapper;
import com.bingli.moliapiinterface.model.dto.PoetryResponse;
import com.bingli.moliapiinterface.model.entity.Poetry;
import com.bingli.moliapiinterface.service.PoetryService;
import com.bingli.moliapiinterface.util.ResponseEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class PoetryServiceImpl implements PoetryService {

    private final PoetryMapper poetryMapper;

    public PoetryServiceImpl(PoetryMapper poetryMapper) {
        this.poetryMapper = poetryMapper;
    }


    @Override
    public PoetryResponse getRandomPoetry() {
        Poetry poetry = poetryMapper.selectRandomOne();
        if (poetry == null || poetry.getIsDelete() == 1) {
            throw new BusinessException(ResponseEnum.DATA_NOT_FOUND);
        }
        return convertToResponse(poetry);
    }

    private PoetryResponse convertToResponse(Poetry poetry) {
        if (poetry == null) {
            return null;
        }
        PoetryResponse response = new PoetryResponse();
        BeanUtils.copyProperties(poetry, response);
        return response;
    }
}
