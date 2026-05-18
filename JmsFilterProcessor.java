import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class JmsFilterProcessor {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic inputTopic = session.createTopic("stream-test");
        Topic outputTopic = session.createTopic("stream-output");

        MessageConsumer consumer = session.createConsumer(inputTopic);
        MessageProducer producer = session.createProducer(outputTopic);

        final String KEYWORD = "pilne";
        System.out.println("JMS Procesor filtrujący uruchomiony. Szukam słowa: '" + KEYWORD + "'");

        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage) {
                    String text = ((TextMessage) message).getText();
                    if (text.toLowerCase().contains(KEYWORD)) {
                        producer.send(session.createTextMessage(text));
                        System.out.println("Przefiltrowano i przesłano dalej: " + text);
                    }
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(Long.MAX_VALUE);
    }
}