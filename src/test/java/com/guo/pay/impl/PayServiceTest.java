package com.guo.pay.impl;

import com.guo.pay.PayApplicationTests;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import org.junit.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class PayServiceTest extends PayApplicationTests {

    @Autowired
    private PayServiceImpl payService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void create() {
        payService.create("123456789657321", BigDecimal.valueOf(0.01), BestPayTypeEnum.WXPAY_NATIVE);
    }

    @Test
    public void sendMsgMq(){
        amqpTemplate.convertAndSend("payNotify","hello");
    }
}