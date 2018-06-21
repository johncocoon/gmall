/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: OrderService
 * Author:   John
 * Date:     2018/4/26 11:07
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall.service;

import com.atguigu.gmall1108.bean.OrderInfo;
import com.atguigu.gmall1108.bean.ProcessStatus;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/26
 * @since 1.0.0
 */
public interface OrderService {

    public String saveOrderInfo(OrderInfo orderInfo);

    public OrderInfo getOrderInfoById(String orderId);

    void updateProcessStatus(String orderId, ProcessStatus processStatus);

    void sendOrderStatus(String orderId);

    String orderSpilt(String orderId, String wareSkuMap);

    void updateOrderStatus(String orderId, ProcessStatus waitingDelever);
}