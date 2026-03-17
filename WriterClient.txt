import java.io.*;
import java.net.Socket;
import java.util.Random;

public class WriterClient {
    public static void main(String[] args) throws Exception {
        String writerId = "Pisarz_" + new Random().nextInt(100);
        System.out.println("Uruchomiono pisarza: " + writerId);

        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Random rand = new Random();
            String myBookId = "BK_" + rand.nextInt(1000);

            // 1. Dodanie książki
            Book newBook = new Book(myBookId, writerId, "Tytul_" + myBookId, "2024", "Tresc ksiazki " + myBookId);
            Request reqInsert = new Request(Action.INSERT, writerId);
            reqInsert.bookPayload = newBook;
            out.writeObject(reqInsert); out.flush();
            System.out.println("Wstawianie: " + ((Response) in.readObject()).message);

            while (true) {
                Thread.sleep(3000 + rand.nextInt(4000)); // Losowe przerwy
                int action = rand.nextInt(3);

                if (action == 0) {
                    // Zablokuj
                    Request reqBlock = new Request(Action.BLOCK, writerId);
                    reqBlock.bookId = myBookId;
                    out.writeObject(reqBlock); out.flush();
                    System.out.println("Blokowanie: " + ((Response) in.readObject()).message);
                } else if (action == 1) {
                    // Sprawdź czy oddano i spróbuj zaktualizować
                    Request reqTest = new Request(Action.TEST_RETURNED, writerId);
                    reqTest.bookId = myBookId;
                    out.writeObject(reqTest); out.flush();
                    Response resTest = (Response) in.readObject();
                    
                    if (resTest.success && (Boolean) resTest.dataPayload) {
                        Request reqUpdate = new Request(Action.UPDATE, writerId);
                        newBook.title = "Zaktualizowany Tytul " + rand.nextInt(100);
                        reqUpdate.bookPayload = newBook;
                        out.writeObject(reqUpdate); out.flush();
                        System.out.println("Aktualizacja: " + ((Response) in.readObject()).message);
                    }
                } else {
                    // Zażądaj ogólnego raportu (dla testów)
                    Request reqReport = new Request(Action.REPORT, writerId);
                    out.writeObject(reqReport); out.flush();
                    System.out.println("Raport: " + ((Response) in.readObject()).message);
                }
            }
        }
    }
}