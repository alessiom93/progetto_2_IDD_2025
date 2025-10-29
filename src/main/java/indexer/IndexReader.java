package indexer;

import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.search.*;
import java.io.IOException;
import java.nio.file.Paths;
// GENERATO DALL'AI
// Lettura e visualizzazione dei termini indicizzati
public class IndexReader {

    public static void main(String[] args) {
        try {
            // Apri l'indice
            Directory dir = FSDirectory.open(Paths.get("index"));
            DirectoryReader reader = DirectoryReader.open(dir);

            System.out.println("=== Termini indicizzati per campo ===\n");

            // Ciclo su ogni foglia dell'indice
            for (LeafReaderContext leaf : reader.leaves()) {
                LeafReader leafReader = leaf.reader();

                // Lista dei campi da leggere
                String[] fields = {"filename", "content"};
                for (String field : fields) {
                    Terms terms = leafReader.terms(field);
                    if (terms != null) {
                        TermsEnum termsEnum = terms.iterator();
                        System.out.println("Campo: " + field);
                        BytesRef term;
                        while ((term = termsEnum.next()) != null) {
                            System.out.println("  " + term.utf8ToString());
                        }
                        System.out.println();
                    }
                }
            }

            reader.close();
            dir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

