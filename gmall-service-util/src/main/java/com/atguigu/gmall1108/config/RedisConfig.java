/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: RedisConfig
 * Author:   John
 * Date:     2018/4/16 10:17
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/16
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {

            @Value("${spring.redis.host:default}")
            private String host;

            @Value("${spring.redis.port:0}")
            private int port;

            @Bean
            public RedisUtil getJedis(){
                if(!"default".equals(host)){
                    RedisUtil redisUtil = new RedisUtil();
                    redisUtil.initJedisPool(host,port);
                    return redisUtil;
                }
                return null;
            }


}