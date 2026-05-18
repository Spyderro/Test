import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class JmsConsumerFilter {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic("stream-output");
        MessageConsumer consumer = session.createConsumer(topic);

        System.out.println("Oczekiwanie na wiadomości (JMS) z tematu 'stream-output'...");

        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage) {
                    System.out.println("Odebrano: " + ((TextMessage) message).getText());
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(Long.MAX_VALUE);
    }
}