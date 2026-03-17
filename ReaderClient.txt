import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Random;

public class ReaderClient {
    public static void main(String[] args) throws Exception {
        String readerId = "Czytelnik_" + new Random().nextInt(100);
        System.out.println("Uruchomiono czytelnika: " + readerId);

        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Random rand = new Random();

            while (true) {
                Thread.sleep(2000 + rand.nextInt(3000));

                // 1. Szukaj książki
                Request searchReq = new Request(Action.SEARCH, readerId);
                searchReq.keyword = "Tytul_"; // Szukamy czegokolwiek z "Tytul_"
                out.writeObject(searchReq); out.flush();
                Response searchRes = (Response) in.readObject();

                List<Book> foundBooks = (List<Book>) searchRes.dataPayload;
                if (foundBooks != null && !foundBooks.isEmpty()) {
                    // Wybierz losową książkę z wyników
                    Book bookToBorrow = foundBooks.get(rand.nextInt(foundBooks.size()));
                    
                    // 2. Wypożycz
                    Request borrowReq = new Request(Action.BORROW, readerId);
                    borrowReq.bookId = bookToBorrow.id;
                    out.writeObject(borrowReq); out.flush();
                    Response borrowRes = (Response) in.readObject();
                    
                    System.out.println("Próba wypożyczenia [" + bookToBorrow.id + "]: " + borrowRes.message);

                    if (borrowRes.success) {
                        System.out.println(readerId + " czyta książkę...");
                        Thread.sleep(4000); // Symulacja czytania
                        
                        // 3. Zwróć
                        Request returnReq = new Request(Action.RETURN, readerId);
                        returnReq.bookId = bookToBorrow.id;
                        out.writeObject(returnReq); out.flush();
                        System.out.println("Zwrot: " + ((Response) in.readObject()).message);
                    }
                } else {
                    System.out.println("Nie znaleziono książek, czekam...");
                }
            }
        }
    }
}