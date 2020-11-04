package com.guo.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wx")
@Data
public class WxAcountConfig {
    private String appId;
    private String mchId;
    private String mchKey;
    private String notifyUrl;
    private String returnUrl;
}
