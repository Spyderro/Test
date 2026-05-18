import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.Scanner;

public class JmsProducer {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic("stream-test");
        MessageProducer producer = session.createProducer(topic);

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Wpisz wiadomość (JMS) i wciśnij ENTER (wpisz 'exit' aby wyjść):");
            while (true) {
                String text = scanner.nextLine();
                if ("exit".equalsIgnoreCase(text)) break;

                TextMessage message = session.createTextMessage(text);
                producer.send(message);
                System.out.println("Wysłano: " + text);
            }
        }

        producer.close();
        session.close();
        connection.close();
    }
}