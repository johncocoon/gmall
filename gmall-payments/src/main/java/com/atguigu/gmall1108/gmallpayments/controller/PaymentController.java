/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: PaymentController
 * Author:   John
 * Date:     2018/4/28 9:01
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.gmallpayments.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall1108.bean.OrderInfo;
import com.atguigu.gmall1108.bean.PaymentInfo;
import com.atguigu.gmall1108.bean.PaymentStatus;
import com.atguigu.gmall1108.gmallpayments.config.AlipayConfig;
import com.atguigu.gmall.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author John
 * @create 2018/4/28
 * @since 1.0.0
 */
@Controller
public class PaymentController {

    @Reference
    OrderService orderService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    AlipayClient alipayClient;

    @RequestMapping("/index")
    public String index(@RequestParam String orderId, Map map) {
        OrderInfo orderInfoById = orderService.getOrderInfoById(orderId);
        map.put("orderId", orderId);
        map.put("totalAmount", orderInfoById.getTotalAmount());

        return "index";
    }

    @RequestMapping(value = "/alipay/submit", method = RequestMethod.POST)
    public ResponseEntity<Object> paymentAlipay(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        //支付宝对接

        String orderId = request.getParameter("orderId");
        if(orderId==null||orderId.length()==0){
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        OrderInfo orderInfo = orderService.getOrderInfoById(orderId);
        if(orderInfo==null ){
            //没有对应的订单
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        //保存支付信息
        PaymentInfo paymentInfo =new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setSubject(orderInfo.getOrderSubject());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentService.savePaymentInfo(paymentInfo);


        //利用支付宝客户端生成表单页面
        AlipayTradePagePayRequest alipayRequest=new AlipayTradePagePayRequest();

        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String,String> paramMap=new HashMap<>();
        paramMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        paramMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        paramMap.put("total_amount",paymentInfo.getTotalAmount().toString());
        paramMap.put("subject",paymentInfo.getSubject());
        String paramJson = JSON.toJSONString(paramMap);
        alipayRequest.setBizContent(paramJson);
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        httpServletResponse.setContentType("text/html;charset=utf-8" );
        //消息队列    将支付结果延迟加载   返回三次  我不管有没有支付   都要返回结果
        PaymentInfo paymentInfo1 = new PaymentInfo();
        paymentInfo1.setOutTradeNo(paymentInfo.getOutTradeNo());
        paymentService.sendOrderInfoUpdateQueue(paymentInfo.getOutTradeNo(),3,10);

        //把表单html打印到客户端浏览器
        return  ResponseEntity.ok().body(form) ;
    }

    /***
     * 接收异步通知
     * 1、 验证签名
     * 2、 判断成功标志
     * 3、 该单据是否已经处理
     * 4、 修改支付信息状态
     * 5、 通知订单模块
     * 6、 给支付宝回执 success
     */
    @RequestMapping("alipay/callback/notify")
    @ResponseBody
    public String  alipayCallback(HttpServletRequest request,@RequestParam Map paramMap){
        boolean isChecked=false;
        try {
            isChecked  = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset);

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(!isChecked){
            System.out.println(" ----------验签不通过！！"  );
            return "验签不通过！！";
        }
        //验证通过的话   保存数据到  修改数据
        String tradeStatus = (String) paramMap.get("trade_status");
        if("TRADE_SUCCESS".equals(tradeStatus)||"TRADE_FINISHED".equals(tradeStatus)){
//检查当前支付状态
            String outTradeNo = (String) paramMap.get("out_trade_no");

            PaymentInfo paymentInfoQuery=new PaymentInfo();
            paymentInfoQuery.setOutTradeNo(outTradeNo);

            PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);

            if(paymentInfo.getPaymentStatus()==PaymentStatus.PAID||paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else{
                //修改状态
                PaymentInfo paymentInfoForUpdate=new PaymentInfo();
                paymentInfoForUpdate.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoForUpdate.setConfirmTime(new Date());
                paymentInfoForUpdate.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(paymentInfo.getOutTradeNo(),paymentInfoForUpdate);
                //发送通知给订单 // 消息队列
                //orderService.updateProcessStatus(paymentInfo.getOrderId(),ProcessStatus.PAID);
                paymentService.sendPaymentResult(paymentInfo.getOrderId(),"success");
                return "success";
            }
        }
        return "fail";
    }




    //假设它是支付成功的测试   进行订单是否要进行拆单   消息队列的传递
    //测试 支付宝支付成功后回调   消息队列进行
    @RequestMapping("paymentsendqueue")
    @ResponseBody
    public String testPaymentSendQueue(@RequestParam(name = "orderId") String orderId){
        paymentService.sendPaymentResult(orderId,"success");
        return " has been send";
    }




}