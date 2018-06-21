/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: CartServiceImpl
 * Author:   John
 * Date:     2018/4/24 18:26
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall1108.bean.CartInfo;
import com.atguigu.gmall1108.bean.SkuInfo;
import com.atguigu.gmall1108.config.RedisUtil;
import com.atguigu.gmall1108.constant.OrderConst;
import com.atguigu.gmall1108.mapper.CartInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author John
 * @create 2018/4/24
 * @since 1.0.0
 */
@Service
public class CartServiceImpl implements CartService {

    public static final String CARTINFO_IS_CHECKED="1";

    @Autowired
     CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtil redisUtil;




    //这个是假设用户已经登录的后
    public void addToCart(SkuInfo skuInfo, String userId, Integer skuNum) {
        //先检查是否已经存在
        String userCartKey="user:"+userId+":cart";
        //插入前先检查
        CartInfo cartInfoQuery=new CartInfo();
        cartInfoQuery.setSkuId(skuInfo.getId());
        cartInfoQuery.setUserId(userId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfoQuery);
        if(cartInfoExist!=null) {
            cartInfoExist.setSkuPrice(skuInfo.getPrice());
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
            //更新缓存
            Jedis jedis = redisUtil.getJedis();
            jedis.hset(userCartKey,skuInfo.getId(),JSON.toJSONString(cartInfoExist) );
            jedis.close();
        }else{
            //插入数据库
            CartInfo cartInfo=new CartInfo();
            cartInfo.setSkuId(skuInfo.getId());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfoMapper.insertSelective(cartInfo);

            //更新缓存
            Jedis jedis = redisUtil.getJedis();
            jedis.hset("user:"+userId+":cart",skuInfo.getId(),JSON.toJSONString(cartInfo));
            Long ttl = jedis.ttl("user:" + userId + ":info");
            jedis.expire(userCartKey, ttl.intValue());
            jedis.close();
        }

    }


    public List<CartInfo> getCartList(String userId){

        //优先从缓存中取值
        Jedis jedis = redisUtil.getJedis();
        List<String> skuJsonlist = jedis.hvals("user:" + userId + ":cart");
        List<CartInfo> cartInfoList=new ArrayList<>();

        if(skuJsonlist!=null &&skuJsonlist.size()!=0){
            //序列化
            for (String skuJson : skuJsonlist) {
                CartInfo cartInfo = JSON.parseObject(skuJson, CartInfo.class);
                cartInfoList.add( cartInfo);
            }
            //缓存中的值取出来是没有序的 用id进行排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Long.compare(Long.parseLong(o2.getId()) ,Long.parseLong(o1.getId())) ;
                }
            });
            return cartInfoList;
        }else{
//如果缓存没有就总数据库中加载
            cartInfoList = loadCartCache(  userId);
            return cartInfoList;
        }
    }


    public List<CartInfo> loadCartCache(String userId){
        List<CartInfo> cartlist = cartInfoMapper.selectCartListWithCurPrice(userId);
        if(cartlist==null||cartlist.size()==0){
            return null;
        }
        Jedis jedis = redisUtil.getJedis();
        String userCartKey="user:"+userId+":cart";
        String userInfoKey="user:"+userId+":info";
        Map cartMap =new HashMap(cartlist.size());
        for (CartInfo cartInfo : cartlist) {
            cartMap.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(userCartKey,cartMap);
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey, ttl.intValue());
        return cartlist;
    }

    //合并cookie中的  与用户的购物车中的
    public List<CartInfo> mergeToCart(String userId,List<CartInfo> cartInfoList){
        //1查出用户的购物车
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfoExistList  = cartInfoMapper.select(cartInfo);
      a:  for (CartInfo cartInfoCookie : cartInfoList) {
            boolean cartInfoIsExist =  false;
            for (CartInfo cartInfodb : cartInfoExistList) {
                    if(cartInfodb.getSkuId().equals(cartInfoCookie.getSkuId())){
                                cartInfodb.setSkuNum(cartInfoCookie.getSkuNum()+cartInfodb.getSkuNum());
                                cartInfoMapper.updateByPrimaryKey(cartInfodb);
                                cartInfoIsExist=true;
                                continue a;
                    }
            }
            if(!cartInfoIsExist){
                cartInfoCookie.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCookie);
            }
        }
        List<CartInfo> cartInfos = loadCartCache(userId);
        return cartInfos;


    }

    public List<CartInfo> updateCartInfoRedis(String isCheckedFlag,String skuId,String userId){
        String userInfoKey="user:"+userId+":info";
        String userCartKey="user:"+userId+":cart";
        Jedis jedis = redisUtil.getJedis();
        List<String> cartInfoList = jedis.hvals(userCartKey);
        List<CartInfo> cartInfoCheckStatus= new ArrayList<>(cartInfoList.size());
        for (String cartInfoJson : cartInfoList) {
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            if(skuId.equals(cartInfo.getSkuId())){
                cartInfo.setIsChecked(isCheckedFlag);
            }
            cartInfoCheckStatus.add(cartInfo);
        }
        Map cartMap =new HashMap(cartInfoCheckStatus.size());
        for (CartInfo cartInfo : cartInfoCheckStatus) {
            cartMap.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(userCartKey,cartMap);
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey, ttl.intValue());
        jedis.close();

        return cartInfoCheckStatus;
    }

    //提交订单到后台  选取选中的   redis中获取
    public List<CartInfo> getCartCheckedList(String userId){
        String userCartKey="user:"+userId+":cart";
        //从redis中获取
        Jedis jedis = redisUtil.getJedis();
        List<String> cartInfoRedis = jedis.hvals(userCartKey);
        List<CartInfo> cartInfoCheckList = new ArrayList<>();
        for (String cartInfoJson : cartInfoRedis) {
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            if("1".equals(cartInfo.getIsChecked())){
                cartInfoCheckList.add(cartInfo);
            }
        }
        jedis.close();
        return cartInfoCheckList;
    }



    //删除购物车的选中的  根据redis中选中的数据  删除redis中的选中的数据缓存
    public void delCartInfoByChecked(String userId){
        String userCartKey="user:"+userId+":cart";
            //读取redis中的
        Jedis jedis = redisUtil.getJedis();
        List<String> cartInfoJsonList = jedis.hvals(userCartKey);
        for (String cartInfoJson : cartInfoJsonList) {
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            if(CARTINFO_IS_CHECKED.equals(cartInfo.getIsChecked())){
                        //删除缓存
                        jedis.hdel(userCartKey,cartInfo.getSkuId());
                        //删除数据库中
                       cartInfoMapper.deleteByPrimaryKey(cartInfo.getId());
            }

        }
    }

    //生成流水号
    @Override
    public String genTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String userTradeNoKey="user:"+userId+":tradeNo";
        UUID uuid = UUID.randomUUID();
        jedis.setex(userTradeNoKey,OrderConst.TRADE_EXPIRE,uuid.toString());
        return uuid.toString();
    }

    //验证流水号
    @Override
    public  boolean checkTradeNo(String userId,String trackNo){
        Jedis jedis = redisUtil.getJedis();
        String userTradeNoKey="user:"+userId+":tradeNo";
        String  uuid = jedis.get(userTradeNoKey);
        if(trackNo!=null&&trackNo.equals(uuid)){
            return  true;
        }
        return  false;
    }

    //删除流水号
    @Override
    public  void delTradeNo(String userId ){
        Jedis jedis = redisUtil.getJedis();
        String userTradeNoKey="user:"+userId+":tradeNo";
        jedis.del(userTradeNoKey);
        return  ;
    }



}