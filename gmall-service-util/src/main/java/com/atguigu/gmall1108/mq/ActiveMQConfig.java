/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: ActiveMQConfig
 * Author:   John
 * Date:     2018/4/28 21:21
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.zookeeper.data.ACL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.Connection;
import javax.jms.Session;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author John
 * @create 2018/4/28
 * @since 1.0.0
 */
@Configuration
public class ActiveMQConfig {


    @Value("${spring.activemq.broker-url:disabled}")
    String brokerURL;

    @Value("${activemq.listener.enable:disabled}")
    String listenerEnable;



    @Bean
    public ActiveMQUtil getActiveMQUtil() {
        if("disabled".equals(brokerURL)){
            return null;
        }
        ActiveMQUtil activeMQUtil = new ActiveMQUtil();
        activeMQUtil.init(brokerURL);
        return activeMQUtil;
    }





    @Bean(name = "jmsQueueListener")
    public DefaultJmsListenerContainerFactory jmsQueueListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory) {
        //监听客户端的    会自动监听是否有队列的信息过来

        if("disabled".equals(listenerEnable)){
            return null;
        }

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(activeMQConnectionFactory);

        factory.setSessionTransacted(false);

        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        factory.setConcurrency("5");
        factory.setRecoveryInterval(5000L);

        return factory;

    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory ( ){

        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory(  brokerURL);

        return activeMQConnectionFactory;
    }


}