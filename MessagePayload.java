package distcomp;

import java.io.Serializable;

public class MessagePayload implements Serializable {
    public double[] numbers;
    public int clock;

    public MessagePayload(double[] numbers, int clock) {
        this.numbers = numbers;
        this.clock = clock;
    }
}