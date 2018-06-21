/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: ActiveMQUtil
 * Author:   John
 * Date:     2018/4/28 21:21
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/28
 * @since 1.0.0
 */
public class ActiveMQUtil {


    PooledConnectionFactory pooledConnectionFactory=null;


    public  void init(String brokerUrl){
        ActiveMQConnectionFactory activeMQConnectionFactory=new ActiveMQConnectionFactory(brokerUrl);
        pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setExpiryTimeout(2000);
        pooledConnectionFactory.setMaximumActiveSessionPerConnection(10);
        pooledConnectionFactory.setMaxConnections(30);
        pooledConnectionFactory.setReconnectOnException(true);


    }

    public Connection getConnection(){

        Connection connection = null;
        try {
            connection = pooledConnectionFactory.createConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return connection;
    }

}