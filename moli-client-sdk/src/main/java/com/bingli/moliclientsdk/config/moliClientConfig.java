package com.bingli.moliclientsdk.config;


import com.bingli.moliclientsdk.client.MoliClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "moli.client")
@Data
@ComponentScan
@Configuration
public class moliClientConfig {


    private String accessKey;
    private String secretKey;
    /**
     * 网关地址
     */
    private String gatewayHost;

    @Bean
    public MoliClient moliClient(){
        return new MoliClient(accessKey,secretKey,gatewayHost);
    }
}
