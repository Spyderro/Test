import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.concurrent.ConcurrentHashMap;

public class JmsWordCountProcessor {
    private static final ConcurrentHashMap<String, Integer> wordCounts = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic inputTopic = session.createTopic("stream-test");
        MessageConsumer consumer = session.createConsumer(inputTopic);

        System.out.println("JMS Procesor (Word Count) uruchomiony...");

        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage) {
                    String text = ((TextMessage) message).getText();
                    String[] words = text.toLowerCase().split("\\W+");

                    for (String word : words) {
                        if (!word.isEmpty()) {
                            wordCounts.merge(word, 1, Integer::sum);
                            System.out.println("Słowo: '" + word + "' wystąpiło: " + wordCounts.get(word) + " razy");
                        }
                    }
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(Long.MAX_VALUE);
    }
}