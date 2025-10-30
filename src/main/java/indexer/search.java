package indexer;

import org.apache.lucene.analysis.Analyzer; // analisi del testo (tokenizzazione, stemming, ecc.)
import org.apache.lucene.analysis.it.ItalianAnalyzer; // analyzer per la lingua italiana
import org.apache.lucene.analysis.custom.CustomAnalyzer; // analyzer custom per il filename
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory; // tokenizzatore che usa gli spazi bianchi
import org.apache.lucene.analysis.core.LowerCaseFilterFactory; // filtro per convertire in minuscolo
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory; // filtro per dividere le parole
import org.apache.lucene.document.Document; // per creare documenti e campi
import org.apache.lucene.index.DirectoryReader; // per la lettura dell'indice
import org.apache.lucene.index.StoredFields; // per accedere ai campi memorizzati
import org.apache.lucene.queryparser.classic.QueryParser; // per il parsing delle query
import org.apache.lucene.search.similarities.ClassicSimilarity; // similarità TF-IDF
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

import java.nio.file.*;
import java.util.Scanner;

public class Search {
    public static void main(String[] args) {
      // path dell'indice
        Path indexDir = Paths.get("index");

        try {
            // numero massimo di risultati (documenti) da recuperare
            int top_n = 10;
            // apertura della directory per l'indice
            Directory directory = FSDirectory.open(indexDir);
            // ottiene accesso in lettura all'indice
            DirectoryReader directoryReader = DirectoryReader.open(directory);
            // configurazione dell'IndexSearcher con il DirectoryReader
            IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
            // definisce un criterio di similarità (opzionale), utilizza il classico TF-IDF
            ClassicSimilarity classicSimilarity = new ClassicSimilarity();
            // di default Lucene usa BM25Similarity
            //BM25Similarity bm25Similarity = new BM25Similarity();
            // imposta il criterio di similarità per l'IndexSearcher
            indexSearcher.setSimilarity(classicSimilarity);
            // analyzer per l'analisi del testo
            // usa gli stessi analyzer definiti in fase di indicizzazione
            // Versione italiana (tokenizzazione, rimozione stop words italiane, stemming italiano e normalizzazione) per il content
            // Versione custom per il filename
            Analyzer analyzerIT = new ItalianAnalyzer();
            Analyzer analyzerFilename = CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class) // tokenizza usando gli spazi bianchi
                    .addTokenFilter(LowerCaseFilterFactory.class) // converte tutto in minuscolo
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class) // divide le parole basandosi su maiuscole, numeri, simboli, ecc.
                    .build();
            // input da console per la query di ricerca
            Scanner input = new Scanner(System.in);
            System.out.println("=== Sistema di Ricerca ===");
            System.out.println("Scrivi la query seguendo questa sintassi:");
            System.out.println(" - filename <termine>  → cerca nel nome del file");
            System.out.println(" - content <termine>   → cerca nel contenuto del file");
            System.out.println("Puoi usare le virgolette per phrase query (es: contenuto \"ricerca di testo\")");
            System.out.print("\nQuery: ");
            String queryStr = input.nextLine().trim();
            // determina il campo di ricerca in base al prefisso della query (filename o content)
            String fieldToSearch;
            // seleziona l'analyzer corretto in base al campo (filename o content)
            Analyzer analyzerToUse;
            // se la query inizia con "filename "
            if (queryStr.toLowerCase().startsWith("filename ")) {
                // imposta il campo di ricerca su "filename"
                fieldToSearch = "filename";
                // rimuove il prefisso dalla query
                queryStr = queryStr.substring(9).trim();
                // usa l'analyzer custom per filename
                analyzerToUse = analyzerFilename;
            // se la query inizia con "content "
            } else if (queryStr.toLowerCase().startsWith("content ")) {
                // imposta il campo di ricerca su "content"
                fieldToSearch = "content";
                // rimuove il prefisso dalla query
                queryStr = queryStr.substring(8).trim();
                // usa l'analyzer italiano per content
                analyzerToUse = analyzerIT;
            // altrimenti mostra un messaggio di errore
            } else {
                System.out.println("Errore: specifica il campo di ricerca (filename o content).");
                directoryReader.close();
                input.close();
                return;
            }
            // crea il parser per la query con il campo e l'analyzer selezionati
            QueryParser queryParser = new QueryParser(fieldToSearch, analyzerToUse);
            // definisce la query di ricerca
            Query query = queryParser.parse(queryStr);
            // per cercare in tutti i documenti
            // Query allDocsQuery = new MatchAllDocsQuery();
            // cerca i migliori top_n documenti che soddisfano la query
            TopDocs results = indexSearcher.search(query, top_n);
            System.out.println("\nRisultati trovati: " + results.totalHits.value + "\n");
            // prende i campi memorizzati dei documenti nell'indice
            StoredFields storedFields = indexSearcher.storedFields();
            // itera sui risultati (docunmenti trovati)
            for (ScoreDoc hit : results.scoreDocs) {
                // recupera il documento risultante dall'indice
                Document doc = storedFields.document(hit.doc);
                // ottiene il nome del file dal campo "filename"
                String filename = doc.get("filename");
                // ottiene il punteggio di rilevanza del documento (ranking)
                float score = hit.score;
                System.out.println("- " + filename + " (score: " + score + ")");
            }
            directoryReader.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
