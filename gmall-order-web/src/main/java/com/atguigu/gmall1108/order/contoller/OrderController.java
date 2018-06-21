package com.atguigu.gmall1108.order.contoller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall1108.annonation.LoginRequire;
import com.atguigu.gmall1108.bean.*;
import com.atguigu.gmall1108.util.HttpclientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


/**
 * @param
 * @return
 */

@Controller
public class OrderController {


    @Value("${ware_sys_url}")
    public String ware_sys_url;

    @Reference
    UserService userService;
    @Reference
    CartService cartService;
   @Reference
    ManageService manageService;
   @Reference
    OrderService orderService;

    @LoginRequire
    @RequestMapping("/trade")
    public String trade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");

        List<UserAddress> userAddressList = userService.getUserAddressList(userId);

        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);
        String tradeNo = cartService.genTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        request.setAttribute("userAddressList",userAddressList);
        request.setAttribute("cartCheckedList",cartCheckedList);
        return "trade";
    }
    @LoginRequire
    @RequestMapping(value = "/submitOrder",method = RequestMethod.POST)
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");
        boolean checkTradeNo = cartService.checkTradeNo(userId, tradeNo);
        if(!checkTradeNo){
            request.setAttribute("errMsg","结算页面过期或已失效，请重新结算。");
            return "tradeFail";
        }
        //验库存、验价

        orderInfo.setUserId(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            if(!skuInfo.getPrice().equals(orderDetail.getOrderPrice())){
                request.setAttribute("errMsg","商品价格变动，请重新下单");
                return "tradeFile";
            }

            String isChecked = HttpclientUtil.doGet(ware_sys_url+"?skuId="+orderDetail.getSkuId()+"&num="+orderDetail.getSkuNum());
            if(!"1".equals(isChecked)){
                request.setAttribute("errMsg","商品库存不足");
                return "tradeFail";
            }
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            orderDetail.setSkuName(skuInfo.getSkuName());

        }
        //保存后  删除购物车   删除数据库中   清空redis中的选中的数据
        String orderId = orderService.saveOrderInfo(orderInfo);
        //删除重付提交的码
        cartService.delTradeNo(userId);
        return "redirect://payment.gmall.com/index?orderId="+orderId;

    }

    /**
     * 用户支付的时候  进行的订单的分割
     * @param
     * @return
     */

    @RequestMapping(value = "orderSplit",method = RequestMethod.POST)
    @ResponseBody
    public String orderSpilt (HttpServletRequest request){
        String orderId = request.getParameter("orderId");
            String wareSkuMap = request.getParameter("wareSkuMap");
        //返回一个字符串  处理  分单
        String waskOrderTaskJson =  orderService.orderSpilt(orderId,wareSkuMap);

        return waskOrderTaskJson;

    }



}
