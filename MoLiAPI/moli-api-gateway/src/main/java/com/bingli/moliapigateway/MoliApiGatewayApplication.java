package com.bingli.moliapigateway;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDubbo
@EnableDiscoveryClient
@SpringBootApplication
public class MoliApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoliApiGatewayApplication.class, args);
    }



}
