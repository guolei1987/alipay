package com.guo.pay.impl;

import com.google.gson.Gson;
import com.guo.pay.dao.PayInfoMapper;
import com.guo.pay.enums.PayPlatformEnum;
import com.guo.pay.pojo.PayInfo;
import com.guo.pay.service.IPayService;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PayServiceImpl implements IPayService {

    private static final String QUEUE_PAY_NOTIFY="payNotify";
    @Autowired
    private BestPayService bestPayService;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private PayInfoMapper payInfoMapper;
    /*
       创建/发起支付
    */
    @Override
    public PayResponse create(String orderId, BigDecimal amount,BestPayTypeEnum bestPayTypeEnum) {

        //写入数据库
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
                PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount);
        payInfoMapper.insertSelective(payInfo);

        PayRequest payRequest = new PayRequest();
        payRequest.setOrderName("1416020-订单号sdk");
        payRequest.setOrderId(orderId);
        payRequest.setOrderAmount(amount.doubleValue());
        payRequest.setPayTypeEnum(bestPayTypeEnum);
        PayResponse response = bestPayService.pay(payRequest);
        log.info("发起支付的response={}",response);
        return response;
    }
/*
异步通知处理
@param notifyData
 */
    @Override
    public String asyncNotify(String notifyData) {
        //1，签名校验
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("异步通知的payResponse={}",payResponse);
        //2,金额校验(从数据库拿到的金额和异步通知的金额）
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        if(payInfo==null){
            throw new RuntimeException("查询的订单信息是null");
        }
        //如果不是已支付，比较金额；
        if(!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())){
            //double类型精度问题，不好比较
            if(payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount()))!=0){
                throw new RuntimeException("异步通知的金额和数据库的不一致,orderNo="+payResponse.getOrderId());
            }
            //3，金额一致，修改支付状态为成功支付；
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            //交易流水号
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());
            payInfo.setUpdateTime(null);
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        //TODO pay发送MQ消息，mall接收MQ消息,不建议直接传payInfo..毕竟不是所有信息都要传递，暂时这么做
        //直接传对象 控制台看不到信息，只有是字符才能在rabbitMq客户端看到；接受的时候再转回去就好
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY,new Gson().toJson(payInfo));

//      4最后告诉支付平台，不用通知了已成功接收
        if(payResponse.getPayPlatformEnum()== BestPayPlatformEnum.WX){
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        }else if(payResponse.getPayPlatformEnum()==BestPayPlatformEnum.ALIPAY){
            return "success";
        }
        throw new RuntimeException("异步通知中错误的支付平台");
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {
        return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
