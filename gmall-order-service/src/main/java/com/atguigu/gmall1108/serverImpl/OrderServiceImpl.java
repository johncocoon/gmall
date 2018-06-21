/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: OrderController
 * Author:   John
 * Date:     2018/4/25 20:24
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.serverImpl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall1108.bean.*;
import com.atguigu.gmall1108.mapper.OrderDetailMapper;
import com.atguigu.gmall1108.mapper.OrderInfoMapper;
import com.atguigu.gmall1108.mq.ActiveMQUtil;
import com.atguigu.gmall1108.mq.QueueNameConst;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈订单管理〉
 *
 * @author John
 * @create 2018/4/25
 * @since 1.0.0
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Reference
    CartService cartService;


    public String saveOrderInfo(OrderInfo orderInfo){

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR,1);
        orderInfo.setCreateTime(date);
        orderInfo.setExpireTime(calendar.getTime());
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        //生成订单流水号
        String outTradeNo = "gmall"+(new Random(1000).nextInt()+""+System.currentTimeMillis());
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.sumTotalAmount();
        orderInfoMapper.insertSelective(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(   orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        //保存后  删除购物车   删除数据库中   清空redis中的选中的数据
        cartService.delCartInfoByChecked(orderInfo.getUserId());
        return orderInfo.getId();
    }


    public OrderInfo getOrderInfoById(String orderId){
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);

         orderInfo = orderInfoMapper.selectOne(orderInfo);
         OrderDetail orderDetail = new OrderDetail();
         orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    /**
     * 处理队列信息
     */
    public void updateProcessStatus(String orderId,ProcessStatus processStatus){

      //这个处理业务的
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfo.setProcessStatus(ProcessStatus.PAID);
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);

    }

    @Override
    public void sendOrderStatus(String orderId) {
        //修改库存信息
        Connection connection = activeMQUtil.getConnection();
        Session session=null;
        try {
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue(QueueNameConst.orderUpdateQueue);
            MessageProducer producer = session.createProducer(queue);

            TextMessage textMessage = new ActiveMQTextMessage();

            OrderInfo orderInfoById = getOrderInfoById(orderId);
            Map map = initWateOrderTaskMap(orderInfoById);
            String wareOrderTaskJson = JSON.toJSONString(map);

            textMessage.setText(wareOrderTaskJson);

            producer.send(textMessage);
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

    @Override
    public String orderSpilt(String orderId, String wareSkuMap) {
        List<Map > wateOrderTaskMap = new ArrayList<>();
       // List<OrderInfo> orderInfoList = new ArrayList<>();
        //转换格式
        List<Map> wareSkuInfo = JSON.parseArray(wareSkuMap, Map.class);
        //查询出主表
        OrderInfo orderInfoById = getOrderInfoById(orderId);
        //转换json
        List<OrderDetail> orderDetailList = orderInfoById.getOrderDetailList();

        //组合
        for (Map wareSku : wareSkuInfo) {
           List<String > skuIds = (List<String>) wareSku.get("skuIds");

           //创建从表
            OrderInfo subOrderInfo = new OrderInfo();
            try {
                BeanUtils.copyProperties(subOrderInfo,orderInfoById);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            subOrderInfo.setParentOrderId(orderInfoById.getId());
            subOrderInfo.setWareId((String) wareSku.get("wareId"));
            subOrderInfo.setId(null);
            List<OrderDetail>  subOrderInfoDetail = new ArrayList<>();
            for (String skuId : skuIds) {
                 //判断是否是在一个库中
                for (OrderDetail orderDetail : orderDetailList) {
                    if(skuId.equals(orderDetail.getSkuId())){
                        orderDetail.setId(null);
                        orderDetail.setOrderId(orderId);
                        subOrderInfoDetail.add(orderDetail);
                    }
                }
            }
            subOrderInfo.setOrderDetailList(subOrderInfoDetail);
            subOrderInfo.setId(orderId);
            Map map = initWateOrderTaskMap(subOrderInfo);
            String jsonString = JSON.toJSONString(map);
            wateOrderTaskMap.add(map);
        }

        return JSON.toJSONString(wateOrderTaskMap);
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus waitingDelever) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(waitingDelever);
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    public Map initWateOrderTaskMap(OrderInfo orderInfo){
        //准备发送到仓库系统的订单
        String wareId = orderInfo.getWareId();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderId", orderInfo.getId());
        hashMap.put("consignee", orderInfo.getConsignee());
        hashMap.put("consigneeTel", orderInfo.getConsigneeTel());
        hashMap.put("orderComment", orderInfo.getOrderComment());
        hashMap.put("orderBody", orderInfo.getOrderSubject());

        hashMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        hashMap.put("paymentWay", "2");//1 货到付款 2 在线支付

        hashMap.put("wareId",wareId);

        List<HashMap<String, String>> details = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<String, String> detailMap = new HashMap<>();
            detailMap.put("skuId", orderDetail.getSkuId());
            detailMap.put("skuNum", "" + orderDetail.getSkuNum());
            detailMap.put("skuName", orderDetail.getSkuName());
            details.add(detailMap);
        }

        hashMap.put("details", details);

        return hashMap;
    }







}