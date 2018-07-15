/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: CartService
 * Author:   John
 * Date:     2018/4/24 18:59
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall.service;

import com.atguigu.gmall1108.bean.CartInfo;
import com.atguigu.gmall1108.bean.SkuInfo;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/24
 * @since 1.0.0
 *    1233
 *
 *
 *
 */
public interface CartService {

    public void addToCart(SkuInfo skuInfo, String userId, Integer skuNum);

    public List<CartInfo> getCartList(String userId);

    public List<CartInfo> loadCartCache(String userId);

    public List<CartInfo> mergeToCart(String userId,List<CartInfo> cartInfoList);

    public List<CartInfo> updateCartInfoRedis(String isCheckedFlag,String skuId,String userId);

    public List<CartInfo> getCartCheckedList(String userId);

    public void delCartInfoByChecked(String userId);

    public String genTradeNo(String userId);

    public  boolean checkTradeNo(String userId,String trackNo);

    public  void delTradeNo(String userId );

}