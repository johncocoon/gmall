/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: RedisUtil
 * Author:   John
 * Date:     2018/4/16 10:19
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/16
 * @since 1.0.0
 */
public class RedisUtil {

    JedisPool jedisPool ;

    public void initJedisPool(String host,int port){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(200);
        poolConfig.setMaxIdle(30);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(10*1000);
        poolConfig.setTestOnBorrow(true);
        jedisPool=new JedisPool(poolConfig,host,port,20*1000);

    }

    public Jedis getJedis(){

        Jedis jedis = jedisPool.getResource();
        return jedis;
    }


}