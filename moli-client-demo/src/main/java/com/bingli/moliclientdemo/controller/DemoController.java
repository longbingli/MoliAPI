package com.bingli.moliclientdemo.controller;


import com.bingli.moliclientsdk.client.MoliClient;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @Resource
    private MoliClient moliClient;


    @GetMapping("/poetry")
    public String random() {
        return moliClient.doRequest("GET", "/api/random/random", null);
    }
}
