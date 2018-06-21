package com.atguigu.gmall1108.gmallpayments;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentsApplicationTests {

    @Test
    public void contextLoads() throws JMSException {

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.109.130:61616");
        Connection connection = activeMQConnectionFactory.createConnection("admin","admin");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("test1");
        MessageProducer producer = session.createProducer(queue);
        TextMessage message = new ActiveMQTextMessage();
        message.setText("fuckyou");
        producer.send(message);
        producer.close();
        session.close();
        connection.close();


    }
@Test
    public void consumer() throws JMSException {

                ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.109.130:61616");
                Connection connection = activeMQConnectionFactory.createConnection("admin","admin");
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue("test1");
                MessageConsumer consumer = session.createConsumer(queue);
                consumer.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(Message message) {
                        if(message instanceof  TextMessage){
                            try {
                                String text = ((TextMessage) message).getText();
                                System.out.println("text = " + text);
                            } catch (JMSException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });



    }

}
