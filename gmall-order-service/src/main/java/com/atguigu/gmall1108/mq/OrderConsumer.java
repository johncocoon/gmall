/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: OrderConsumer
 * Author:   John
 * Date:     2018/4/29 14:47
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall1108.bean.PaymentInfo;
import com.atguigu.gmall1108.bean.PaymentStatus;
import com.atguigu.gmall1108.bean.ProcessStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/29
 * @since 1.0.0
 */
@Component
public class OrderConsumer {
    @Autowired
    OrderService orderService;
   @Reference
    PaymentService paymentService;


    @Autowired
    ActiveMQUtil activeMQUtil;

    @JmsListener(destination = QueueNameConst.paymentUpdateQueue,containerFactory = "jmsQueueListener")
    public void updateProcessStatus(MapMessage message) throws JMSException {
        String orderId = message.getString("orderId");
        String result = message.getString("result");

        if("success".equals(result)){
            //更新
            orderService.updateProcessStatus(orderId,ProcessStatus.PAID);
            //通知 gware 物流 修改状态
            orderService.sendOrderStatus(orderId);
            //修改order 中的信息 更改为 待发货
            orderService.updateProcessStatus(orderId,ProcessStatus.WAITING_DELEVER);

        }else{
            orderService.updateProcessStatus(orderId,ProcessStatus.PAY_FAIL);
        }


    }
    //监听队列的   延迟加载   共发送三次
    @JmsListener(destination = QueueNameConst.alipayResultUpdateOrderQueue,containerFactory = "jmsQueueListener")
    public void updateProcessStatusByPayment(MapMessage mapMessage) throws JMSException {
        int delaySec = mapMessage.getInt("dalery");
        String outTradeNo = mapMessage.getString("outTradeNo");
        int checkCount = mapMessage.getInt("checkCount");

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);

        PaymentStatus paymentStatus = paymentService.checkAlipayPayment(paymentInfo,checkCount,delaySec);
        if(paymentStatus==PaymentStatus.UNPAID&&checkCount>0){

            paymentService.sendOrderInfoUpdateQueue(outTradeNo,checkCount-1,delaySec);
        }else{

            //已支付  还在继续循环  问题
            return ;
        }
    }


    @JmsListener(destination = QueueNameConst.skuDeductQueue,containerFactory = "jmsQueueListener")
    public void skuDeliverQueue(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");
        if("DEDUCTED".equals(status)){
            orderService.updateOrderStatus(  orderId,   ProcessStatus.WAITING_DELEVER);
        }else{
            orderService.updateOrderStatus(  orderId,   ProcessStatus.STOCK_EXCEPTION);
        }

    }

}