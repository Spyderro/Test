package distcomp;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Start {
    
    public static void deleteFileOrFolder(final Path path) throws IOException {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
        @Override public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return CONTINUE;
        }
        @Override public FileVisitResult visitFileFailed(final Path file, final IOException e) {
          e.printStackTrace(); 
          return TERMINATE;
        }
        @Override public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
          if(e!=null) { e.printStackTrace(); return TERMINATE; }
          Files.delete(dir);
          return CONTINUE;
        }
      });
    }

    public static void main (String[] args) throws Exception {
        File activemqData = new File("activemq-data");
        if(activemqData.exists()) {
            System.out.println("Usuwanie bazy ActiveMQ...");
            deleteFileOrFolder(activemqData.toPath());
        }
        
        // Zaczynamy od E, aby nasłuchiwał zanim inne zaczną nadawać
        final ProcessE processE = new ProcessE();
        processE.start();
        
        final ProcessC processC = new ProcessC();
        final ProcessD processD = new ProcessD();
        final ProcessB processB = new ProcessB();
        final ProcessA processA = new ProcessA();
        
        processC.start();
        processD.start();
        processB.start();
        processA.start();

        System.out.println("Wciśnij ENTER aby zakończyć...\n");
        System.in.read();

        processA.destroy();
        processB.destroy();
        processC.destroy();
        processD.destroy();
        processE.destroy();
    }
}