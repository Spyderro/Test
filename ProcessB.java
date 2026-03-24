package distcomp;

import java.util.Random;
import javax.jms.*;

public class ProcessB extends Thread {
    private final Connection con;
    private final Session session;
    private final MessageConsumer consumerAB;
    private final MessageProducer producerBC;
    private final MessageProducer topicProducer;
    private int clock = 0;
    private Random rand = new Random();

    public ProcessB() throws JMSException {
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumerAB = session.createConsumer(session.createQueue("A-B"));
        producerBC = session.createProducer(session.createQueue("B-C"));
        topicProducer = session.createProducer(session.createTopic("ReportTopic"));
    }

    private void updateClock(int receivedClock) {
        clock = Math.max(clock, receivedClock) + 1;
    }

    private void report(String action) throws JMSException {
        String msg = String.format("[Proces B] %s | Zegar: %d", action, clock);
        topicProducer.send(session.createTextMessage(msg));
        sleepRandomTime();
    }

    private void sleepRandomTime() {
        try { Thread.sleep((rand.nextInt(5) + 1) * 1000); } 
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Krok 1: Odbiór od A
                ObjectMessage om = (ObjectMessage) consumerAB.receive();
                if (om == null) break;
                MessagePayload payload = (MessagePayload) om.getObject();
                updateClock(payload.clock);
                report("Krok 1: Odebrano dane od Procesu A");

                // Krok 2: Logarytm
                clock++;
                for (int i = 0; i < payload.numbers.length; i++) {
                    payload.numbers[i] = Math.log(payload.numbers[i]);
                }
                report("Krok 2: Obliczono logarytmy naturalne");

                // Krok 3: Wysyłanie do C
                clock++;
                producerBC.send(session.createObjectMessage(new MessagePayload(payload.numbers, clock)));
                report("Krok 3: Wysłano wyniki do Procesu C");
            }
        } catch (JMSException e) { e.printStackTrace(); }
    }

    public void destroy() throws JMSException { con.close(); }
}