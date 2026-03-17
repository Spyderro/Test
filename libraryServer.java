import java.io.*;
import java.net.*;
import java.util.*;

public class LibraryServer {
    private static final int PORT = 8080;
    
    // Zwykła ArrayLista zamiast HashMapy
    private static final List<Book> library = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Serwer Biblioteki uruchomiony na porcie " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
            ) {
                while (true) {
                    Request req = (Request) in.readObject();
                    Response res = handleRequest(req);
                    out.writeObject(res);
                    out.flush();
                }
            } catch (EOFException | SocketException e) {
                // Klient się rozłączył
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Response handleRequest(Request req) {
            // Ręczna synchronizacja zasobu współdzielonego - kluczowe na labach!
            synchronized (library) {
                
                // Zmienna pomocnicza do znalezienia książki, o którą chodzi w zapytaniu
                Book targetBook = null;
                String idToFind = req.bookId != null ? req.bookId : (req.bookPayload != null ? req.bookPayload.id : null);
                
                if (idToFind != null) {
                    for (Book b : library) {
                        if (b.id.equals(idToFind)) {
                            targetBook = b;
                            break;
                        }
                    }
                }

                switch (req.action) {
                    case INSERT:
                        if (targetBook == null) {
                            library.add(req.bookPayload);
                            return new Response(true, "Dodano książkę.", null);
                        }
                        return new Response(false, "Książka o tym ID już istnieje.", null);

                    case BLOCK:
                        if (targetBook != null && targetBook.author.equals(req.clientId)) {
                            targetBook.isBlocked = true;
                            return new Response(true, "Książka zablokowana.", null);
                        }
                        return new Response(false, "Brak uprawnień lub brak książki.", null);

                    case TEST_RETURNED:
                        if (targetBook != null && targetBook.author.equals(req.clientId)) {
                            boolean returned = targetBook.currentReaders.isEmpty();
                            return new Response(true, returned ? "Wszyscy oddali" : "Nadal czytana", returned);
                        }
                        return new Response(false, "Błąd testu.", null);

                    case DESTROY:
                        if (targetBook != null && targetBook.author.equals(req.clientId) && targetBook.isBlocked && targetBook.currentReaders.isEmpty()) {
                            library.remove(targetBook);
                            return new Response(true, "Zniszczono książkę.", null);
                        }
                        return new Response(false, "Nie można zniszczyć (czytana, niezablokowana lub brak uprawnień).", null);

                    case UPDATE:
                        if (targetBook != null && targetBook.author.equals(req.clientId) && targetBook.isBlocked && targetBook.currentReaders.isEmpty()) {
                            targetBook.title = req.bookPayload.title;
                            targetBook.content = req.bookPayload.content;
                            targetBook.isBlocked = false; // Odblokuj po edycji
                            return new Response(true, "Zaktualizowano i odblokowano.", null);
                        }
                        return new Response(false, "Nie można edytować.", null);

                    case SEARCH:
                        List<Book> found = new ArrayList<>();
                        for (Book b : library) {
                            if (b.title.contains(req.keyword) || b.content.contains(req.keyword)) {
                                found.add(b);
                            }
                        }
                        return new Response(true, "Znaleziono " + found.size() + " książek.", found);

                    case BORROW:
                        if (targetBook != null && !targetBook.isBlocked) {
                            targetBook.currentReaders.add(req.clientId);
                            return new Response(true, "Wypożyczono.", targetBook);
                        }
                        return new Response(false, "Książka zablokowana lub nie istnieje.", null);

                    case RETURN:
                        if (targetBook != null) {
                            targetBook.currentReaders.remove(req.clientId);
                            return new Response(true, "Zwrócono książkę.", null);
                        }
                        return new Response(false, "Błąd zwrotu.", null);

                    case REPORT:
                        System.out.println("=== RAPORT BIBLIOTEKI ===");
                        for (Book b : library) {
                            System.out.println(b.toString());
                        }
                        System.out.println("=========================");
                        return new Response(true, "Raport wygenerowany na serwerze.", null);

                    default:
                        return new Response(false, "Nieznana akcja.", null);
                }
            } // Koniec bloku synchronized
        }
    }
}