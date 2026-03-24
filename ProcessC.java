package distcomp;

import java.util.Random;
import javax.jms.*;

public class ProcessC extends Thread {
    private final Connection con;
    private final Session session;
    private final MessageConsumer consumerBC;
    private final MessageConsumer consumerDC;
    private final MessageProducer producerCA;
    private final MessageProducer topicProducer;
    private int clock = 0;
    private Random rand = new Random();

    public ProcessC() throws JMSException {
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumerBC = session.createConsumer(session.createQueue("B-C"));
        consumerDC = session.createConsumer(session.createQueue("D-C"));
        producerCA = session.createProducer(session.createQueue("C-A"));
        topicProducer = session.createProducer(session.createTopic("ReportTopic"));
    }

    private void updateClock(int receivedClock) {
        clock = Math.max(clock, receivedClock) + 1;
    }

    private void report(String action) throws JMSException {
        String msg = String.format("[Proces C] %s | Zegar: %d", action, clock);
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
                // Krok 1: Odbiór od B
                ObjectMessage omB = (ObjectMessage) consumerBC.receive();
                if (omB == null) break;
                MessagePayload payloadB = (MessagePayload) omB.getObject();
                updateClock(payloadB.clock);
                report("Krok 1: Odebrano logarytmy od Procesu B");

                // Krok 2: Odbiór od D
                ObjectMessage omD = (ObjectMessage) consumerDC.receive();
                if (omD == null) break;
                MessagePayload payloadD = (MessagePayload) omD.getObject();
                updateClock(payloadD.clock);
                report("Krok 2: Odebrano kwadraty od Procesu D");

                // Krok 3: Obliczanie różnicy
                clock++;
                double[] result = new double[payloadB.numbers.length];
                for(int i=0; i<result.length; i++) {
                    result[i] = Math.abs(payloadB.numbers[i] - payloadD.numbers[i]);
                }
                report("Krok 3: Obliczono różnicę między B a D");

                // Krok 4: Wysyłka do A
                clock++;
                producerCA.send(session.createObjectMessage(new MessagePayload(result, clock)));
                report("Krok 4: Wysłano wynik końcowy do Procesu A");
            }
        } catch (JMSException e) { e.printStackTrace(); }
    }

    public void destroy() throws JMSException { con.close(); }
}