package com.guo.pay.controller;

import com.guo.pay.impl.PayServiceImpl;
import com.guo.pay.pojo.PayInfo;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private WxPayConfig wxPayConfig;
    @Autowired
    private PayServiceImpl payService;

    @GetMapping("/create")
    public ModelAndView create(@RequestParam("orderId") String orderId,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("payType") BestPayTypeEnum bestPayTypeEnum
    ) {
        PayResponse payResponse = payService.create(orderId, amount, bestPayTypeEnum);
        //create不需要写create.ftl
        //参数传递
        //支付方式不同，渲染就不同，微信native支付使用codeUrl，
        //ALIPAY_PC使用body字段
        Map<String, String> map = new HashMap<>();
        if (bestPayTypeEnum == BestPayTypeEnum.WXPAY_NATIVE) {

            map.put("codeUrl", payResponse.getCodeUrl());
            map.put("orderId",orderId);
            map.put("returnUrl",wxPayConfig.getReturnUrl());
            return new ModelAndView("createForWxNative", map);

        } else if (bestPayTypeEnum == BestPayTypeEnum.ALIPAY_PC) {
            map.put("body", payResponse.getBody());
            return new ModelAndView("createForAlipayPc", map);
        }
        throw new RuntimeException("暂不支持这种支付");
    }
    @PostMapping("/notify")
    @ResponseBody
    public String asyncNotify(@RequestBody String notifyData){
        return payService.asyncNotify(notifyData);

        // log.info("notifyData{}",notifyData);
    }

    @GetMapping("/queryByOrderId")
    @ResponseBody
    public PayInfo queryByOrderId(@RequestParam String orderId){
        log.info("查询日志记录。。。。。");
        return payService.queryByOrderId(orderId);
    }

}
