/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: CookieHandler
 * Author:   John
 * Date:     2018/4/24 19:19
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1108.bean.CartInfo;
import com.atguigu.gmall1108.bean.SkuInfo;
import com.atguigu.gmall1108.constant.WebConst;
import com.atguigu.gmall1108.util.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/24
 * @since 1.0.0
 */
@Component
public class CookieHandler {



    String cartCookieName="CART";



    public void addToCart(SkuInfo skuInfo, Integer skuNum, HttpServletRequest request, HttpServletResponse response){
                //看cookie中是否存在
        try {
            List<CartInfo> cartList=new ArrayList<>();
            cartList = getCartList(request);

            CartInfo cart = null;
            if (cartList != null && cartList.size() > 0) {
                for (CartInfo c : cartList) {
                    // 判断购物车中是否存在该商品
                    if (c.getSkuId().equals(skuInfo.getId())) {
                        cart = c;
                        break;
                    }
                }
            }

            if (cart == null) {
                // 当前的购物车没有该商品
                cart = new CartInfo();
                cart.setSkuId(skuInfo.getId());
                cart.setSkuName(skuInfo.getSkuName());
                // 设置商品主图
                cart.setImgUrl(skuInfo.getSkuDefaultImg());
                cart.setSkuPrice(skuInfo.getPrice());
                cart.setCartPrice(skuInfo.getPrice());
                cart.setSkuNum(skuNum);

                cartList.add(cart);
            } else {
                // 在购物车中存在该商品
                cart.setSkuNum(cart.getSkuNum() + skuNum);
            }
            // 设置购物车的商品，过期时间7天
            CookieUtil.setCookie(request, response, cartCookieName, JSON.toJSONString(cartList), WebConst.COOKIE_MAXTIME,true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CartInfo> getCartList(HttpServletRequest request ){
        try {
            String cartListJson = CookieUtil.getCookieValue(request,cartCookieName , true);
            if (cartListJson!= null&&cartListJson.length()>0) {
                List<CartInfo> cartList = JSON.parseArray(cartListJson, CartInfo.class);
                return cartList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }

    public void deleteCartCookie(HttpServletRequest request,
                                 HttpServletResponse response){
        CookieUtil.deleteCookie(request,response,cartCookieName);
    }




    public void updateCartInfoRedis(HttpServletRequest request, HttpServletResponse response, String skuId, String isCheckedFlag) {
        List<CartInfo> cartList = getCartList(request);
        for (CartInfo cartInfo : cartList) {
            if(skuId.equals(cartInfo.getSkuId())){
                cartInfo.setIsChecked(isCheckedFlag);
            }
        }
        String cartInfoCookieJson = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request,response,cartCookieName,cartInfoCookieJson,WebConst.COOKIE_MAXTIME,true);

    }
}