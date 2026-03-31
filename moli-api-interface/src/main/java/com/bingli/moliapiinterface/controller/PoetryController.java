package com.bingli.moliapiinterface.controller;

import com.bingli.moliapiinterface.model.dto.PoetryResponse;
import com.bingli.moliapiinterface.util.Result;
import com.bingli.moliapiinterface.util.ResultUtils;
import com.bingli.moliapiinterface.service.PoetryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/poetry")
public class PoetryController {

    @Resource
    private PoetryService poetryService;

    @GetMapping("/random")
    public Result<PoetryResponse> getRandomPoetry() {
        log.info("Get random poetry");
        PoetryResponse response = poetryService.getRandomPoetry();
        return ResultUtils.success(response);
    }
}
