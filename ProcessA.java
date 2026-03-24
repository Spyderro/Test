package distcomp;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import javax.jms.*;

public class ProcessA extends Thread {
    private final Session session;
    private final Connection con;
    private final MessageProducer producerAB;
    private final MessageProducer producerAD;
    private final MessageConsumer consumerCA;
    private final MessageProducer topicProducer;
    private int clock = 0;
    private Random rand = new Random();

    public ProcessA() throws JMSException {
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        this.session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.producerAB = session.createProducer(session.createQueue("A-B"));
        this.producerAD = session.createProducer(session.createQueue("A-D"));
        this.consumerCA = session.createConsumer(session.createQueue("C-A"));
        this.topicProducer = session.createProducer(session.createTopic("ReportTopic"));
    }

    private void updateClock(int receivedClock) {
        clock = Math.max(clock, receivedClock) + 1;
    }

    private void report(String action) throws JMSException {
        String msg = String.format("[Proces A] %s | Zegar: %d", action, clock);
        topicProducer.send(session.createTextMessage(msg));
        sleepRandomTime();
    }

    private void sleepRandomTime() {
        try { Thread.sleep((rand.nextInt(5) + 1) * 1000); } 
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    private double[] generateNumbers() {
        double[] result = new double[100];
        for (int i = 0; i < 100; i++) result[i] = rand.nextInt(900000) + 100000;
        return result;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Krok 1: Generowanie
                clock++;
                double[] numbers = generateNumbers();
                report("Krok 1: Wygenerowano 100 liczb");

                // Krok 2: Wysyłanie do B
                clock++;
                producerAB.send(session.createObjectMessage(new MessagePayload(numbers, clock)));
                report("Krok 2: Wysłano paczkę do Procesu B");

                // Krok 3: Wysyłanie do D
                clock++;
                producerAD.send(session.createObjectMessage(new MessagePayload(numbers, clock)));
                report("Krok 3: Wysłano paczkę do Procesu D");

                // Krok 4: Odbiór od C i zapis
                ObjectMessage om = (ObjectMessage) consumerCA.receive();
                if (om == null) break;
                MessagePayload payload = (MessagePayload) om.getObject();
                updateClock(payload.clock);
                
                try (PrintWriter out = new PrintWriter(new FileWriter("wyniki.txt", true))) {
                    out.println("Zegar " + clock + ": " + Arrays.toString(payload.numbers));
                } catch (IOException e) { e.printStackTrace(); }
                
                report("Krok 4: Odebrano dane od Procesu C i zapisano do pliku");
            }
        } catch (JMSException e) { e.printStackTrace(); }
    }

    public void destroy() throws JMSException { con.close(); }
}