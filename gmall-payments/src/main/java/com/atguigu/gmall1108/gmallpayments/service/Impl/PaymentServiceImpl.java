/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: PaymentServiceImpl
 * Author:   John
 * Date:     2018/4/28 9:03
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.gmallpayments.service.Impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall1108.bean.PaymentInfo;
import com.atguigu.gmall1108.bean.PaymentStatus;
import com.atguigu.gmall1108.gmallpayments.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall1108.mq.ActiveMQUtil;
import com.atguigu.gmall1108.mq.QueueNameConst;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;


/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/28
 * @since 1.0.0
 */
@com.alibaba.dubbo.config.annotation.Service
public class PaymentServiceImpl implements PaymentService {


    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;



    public  void savePaymentInfo(PaymentInfo paymentInfo){
        //首先  判断  是否是有重复提交的
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(paymentInfo.getOrderId());
        PaymentInfo paymentInfoIsExist = paymentInfoMapper.selectOne(paymentInfoQuery);
        if(paymentInfoIsExist==null){
            paymentInfo.setCreateTime(new Date());
            paymentInfoMapper.insertSelective(paymentInfo);
            return ;
        }
        paymentInfoIsExist.setCreateTime(new Date());
        paymentInfoMapper.updateByPrimaryKey(paymentInfoIsExist);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {

        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(paymentInfoQuery);

        return paymentInfo;
    }

    @Override
    public void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfoForUpdate) {
            //更新支付信息
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);
        paymentInfoMapper.updateByExampleSelective(paymentInfoForUpdate,example);
    }


    //消息队列    只要队列对上就可以'
    @Override
    public void sendPaymentResult(String orderId, String  result){
        Connection connection = activeMQUtil.getConnection();

        Session session=null;
        try {
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue(QueueNameConst.paymentUpdateQueue);

            MessageProducer producer = session.createProducer(queue);
            MapMessage message = new ActiveMQMapMessage();
            message.setString("orderId",orderId);
            message.setString("result",result);
            producer.send(message);

            session.commit();

            session.close();
            producer.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }
    }
    //检查支付宝是否已经支付成功  3次延迟加载
    @Override
    public PaymentStatus checkAlipayPayment(PaymentInfo paymentInfo, int checkCount, int dalery){

        //先是查询一下数据库  如支付成功  状态
        if(paymentInfo.getOutTradeNo()!=null){
            paymentInfo = getPaymentInfo(paymentInfo);
        }
        if(paymentInfo.getPaymentStatus()==PaymentStatus.PAID){
                return PaymentStatus.PAID;
        }
        //查询alipay接口中的数据
        AlipayTradeQueryRequest alipayTradeQueryRequest = new AlipayTradeQueryRequest();
        alipayTradeQueryRequest.setBizContent("{\"out_trade_no\":\""+paymentInfo.getOutTradeNo()+"\"}");
        AlipayTradeQueryResponse response=null;
        try {
            response  = alipayClient.execute(alipayTradeQueryRequest);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){//这个只是调用成功
            String tradeStatus = response.getTradeStatus();
            if("TRADE_SUCCESS".equals(tradeStatus)||"TRADE_FINISHED".equals(tradeStatus)){
                if(paymentInfo.getPaymentStatus()!=PaymentStatus.PAID||paymentInfo.getPaymentStatus()!=PaymentStatus.ClOSED){
                    //如果结果是支付成功 ,则更新支付状态
                        PaymentInfo paymentInfoUpdate = new PaymentInfo();
                        paymentInfoUpdate.setPaymentStatus(PaymentStatus.PAID);
                        paymentInfoUpdate.setCallbackContent(response.getBody());
                        paymentInfoUpdate.setCreateTime(new Date());
                        paymentInfoUpdate.setId(paymentInfo.getId());
                          paymentInfoUpdate.setAlipayTradeNo(response.getTradeNo());
                        paymentInfoMapper.updateByPrimaryKeySelective(paymentInfoUpdate);

                        //发送队列到订单修改状态
                        sendOrderInfoUpdateQueue(paymentInfo.getOutTradeNo(),0,dalery);
                        return PaymentStatus.PAID;
                }else{
                    System.out.println("支付尚未完成 ？？？？？？？？？？ "    );
                    return PaymentStatus.UNPAID;
                }
            }else {
                System.out.println("支付尚未完成 ？？？？？？？？？？ "    );
                return PaymentStatus.UNPAID;
            }

        }

        return PaymentStatus.UNPAID;
    }



    @Override
    public void sendOrderInfoUpdateQueue(String outTradeNo, int checkCount, int dalery) {
        Connection connection = activeMQUtil.getConnection();
        Session session=null;
        try {
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue(QueueNameConst.alipayResultUpdateOrderQueue);
            MessageProducer producer = session.createProducer(queue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setInt("checkCount",checkCount);
            mapMessage.setInt("dalery",dalery);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,dalery*1000);
            producer.send(mapMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();

        }

    }


}


