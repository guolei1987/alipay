package com.guo.pay.config;

import com.lly835.bestpay.config.AliPayConfig;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BestPayConfig {
    @Autowired
    private  WxAcountConfig wxAcountConfig;
    @Autowired
    private AliPayAccountConfig aliPayAccountConfig;
    @Bean
    public WxPayConfig wxPayConfig(){
        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setAppId(wxAcountConfig.getAppId());
        wxPayConfig.setMchId(wxAcountConfig.getMchId());
        wxPayConfig.setMchKey(wxAcountConfig.getMchKey());
        wxPayConfig.setNotifyUrl(wxAcountConfig.getNotifyUrl());
        wxPayConfig.setReturnUrl(wxAcountConfig.getReturnUrl());

        return wxPayConfig;
    }
    @Bean
    public BestPayService bestPayService(WxPayConfig wxPayConfig){
        AliPayConfig aliPayConfig = new AliPayConfig();
        aliPayConfig.setAppId(aliPayAccountConfig.getAppId());
        aliPayConfig.setPrivateKey(aliPayAccountConfig.getPrivateKey());
        aliPayConfig.setAliPayPublicKey(aliPayAccountConfig.getPublicKey());
        aliPayConfig.setNotifyUrl(aliPayAccountConfig.getNotifyUrl());
        aliPayConfig.setReturnUrl(aliPayAccountConfig.getReturnUrl());

        BestPayServiceImpl bestPayService = new BestPayServiceImpl();
        bestPayService.setWxPayConfig(wxPayConfig);
        bestPayService.setAliPayConfig(aliPayConfig);
        return bestPayService;

    }
}
