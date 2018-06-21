package com.atguigu.gmall1108.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall1108.annonation.LoginRequire;
import com.atguigu.gmall1108.bean.CartInfo;
import com.atguigu.gmall1108.bean.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class CartController {


    @Reference
    CartService cartService;
    @Reference
    ManageService manageService;

    @Autowired
    CookieHandler cookieHandler;

    @LoginRequire(autoRedirect = false)
    @RequestMapping(value = "/addToCart", method = RequestMethod.POST)
    public String addCart(HttpServletRequest request, HttpServletResponse response) {

        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        if (userId != null) {
            cartService.addToCart(skuInfo, userId, Integer.parseInt(skuNum));

        } else {
            //用户没有 登录的情况  存cookie中
            cookieHandler.addToCart(skuInfo,Integer.parseInt(skuNum),request,response);
        }
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }
    @LoginRequire(autoRedirect = false)
    @RequestMapping("/cartList")
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        //列出购物车列表
        //1 用户未登录     拿cookie中的值
        String userId = (String) request.getAttribute("userId");
        List<CartInfo>  cartInfoList = cookieHandler.getCartList(request);
        if(userId==null){

        }else{
            cartInfoList = cookieHandler.getCartList(request);
            if(cartInfoList!=null&&cartInfoList.size()>0){
                cartInfoList= cartService.mergeToCart(userId, cartInfoList);

                cookieHandler.deleteCartCookie(request,response);
            }else{
                //用户登录的情况下  关联查询
                cartInfoList = cartService.getCartList(userId);

            }
        }
       // BigDecimal totalAmunt = sumCartInfoList(cartInfoList);
      //  request.setAttribute("totalAmunt",totalAmunt);
        request.setAttribute("cartInfoList",cartInfoList);
        return "cartList";
    }

    @RequestMapping(value = "checkCart",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String isCheckedFlag = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");

        //选中的状态   修改redis   cookie
        if(userId!=null){
            //修改redis中的状态
            cartService.updateCartInfoRedis(isCheckedFlag, skuId, userId);
        }else{
            //修改cookie中的值
            cookieHandler.updateCartInfoRedis(request,response,skuId,isCheckedFlag);
        }

    }
    @LoginRequire
    @RequestMapping("/toTrade")
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartInfoCookieList = cookieHandler.getCartList(request);
        if(cartInfoCookieList!=null&&cartInfoCookieList.size()>0){
            List<CartInfo> cartInfoList = cartService.mergeToCart(userId, cartInfoCookieList);
            cookieHandler.deleteCartCookie(request,response);

        }

        return "redirect://order.gmall.com/trade";
    }






    //计算总价
    public BigDecimal sumCartInfoList(List<CartInfo> cartInfoList){
        int sum =0;
        for (CartInfo cartInfo : cartInfoList) {
            sum+=Integer.parseInt(cartInfo.sumcartInfoBySkuNum().toString());
        }
        return new BigDecimal(sum);
    }





}
