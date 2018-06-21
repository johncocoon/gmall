/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: PaymentService
 * Author:   John
 * Date:     2018/4/28 9:04
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall.service;

import com.atguigu.gmall1108.bean.PaymentInfo;
import com.atguigu.gmall1108.bean.PaymentStatus;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/28
        * @since 1.0.0
        */
public interface PaymentService {
    public  void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfoForUpdate);

    //消息队列    只要队列对上就可以'
    void sendPaymentResult(String orderId, String result);

    //检查支付宝是否已经支付成功  3次延迟加载
    PaymentStatus checkAlipayPayment(PaymentInfo paymentInfo, int checkCount, int dalery);



    void sendOrderInfoUpdateQueue(String outTradeNo, int checkCount, int dalery);
}