import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JmsWindowedCountProcessor {
    private static final AtomicInteger messageCount = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic("stream-test");
        MessageConsumer consumer = session.createConsumer(topic);

        System.out.println("JMS Okna Czasowe (1 minuta) uruchomione...");

        consumer.setMessageListener(message -> messageCount.incrementAndGet());

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            int currentCount = messageCount.getAndSet(0);
            System.out.println("--- Podsumowanie Okna (Ostatnia minuta) ---");
            System.out.println("Liczba odebranych wiadomości: " + currentCount);
            System.out.println("-------------------------------------------");
        }, 1, 1, TimeUnit.MINUTES);

        Thread.sleep(Long.MAX_VALUE);
    }
}