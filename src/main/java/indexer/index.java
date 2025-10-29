package indexer;

import org.apache.lucene.analysis.Analyzer; // analisi del testo (tokenizzazione, stemming, ecc.)
// import org.apache.lucene.analysis.standard.StandardAnalyzer; // analyzer standard di Lucene per l'inglese
import org.apache.lucene.analysis.it.ItalianAnalyzer; // analyzer per la lingua italiana
import org.apache.lucene.analysis.custom.CustomAnalyzer; // analyzer custom per il filename
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory; // tokenizzatore che usa gli spazi bianchi
import org.apache.lucene.analysis.core.LowerCaseFilterFactory; // filtro per convertire in minuscolo
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory; // filtro per dividere le parole
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper; // analyzer per campo specifico
import org.apache.lucene.codecs.simpletext.SimpleTextCodec; // codec per il debug in formato testuale
import org.apache.lucene.document.*; // per creare documenti e campi
import org.apache.lucene.index.*; // per l'indicizzazione
import org.apache.lucene.store.*; // per gestire l'archiviazione dell'indice

import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Index {
    public static void main(String[] args) {
        // path dei documenti da indicizzare
        Path docsDir = Paths.get("documents");
        // path dell'indice
        Path indexDir = Paths.get("index");

        try {
            // analyzer per l'analisi del testo
            // si applica in automatico ai campi di tipo TextField, ignora i campi di tipo StringField
            // Versione italiana (tokenizzazione, rimozione stop words italiane, stemming italiano e normalizzazione)
            Analyzer analyzerIT = new ItalianAnalyzer();
            // Versione inglese standard (tokenizzazione, rimozione stop words inglesi e normalizzazione)
            // Per stemming inglese usare EnglishAnalyzer
            // Analyzer analyzerEN = new StandardAnalyzer();
            // analyzer custom per il filename, meno invadente
            Analyzer analyzerFilename = CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class) // tokenizza usando gli spazi bianchi
                    .addTokenFilter(LowerCaseFilterFactory.class) // converte tutto in minuscolo
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class) // divide le parole basandosi su maiuscole, numeri, simboli, ecc.
                    .build();
            // creazione di un analyzer in base al Field da indicizzare
            Map<String, Analyzer> analyzerPerField = new HashMap<>();
            analyzerPerField.put("filename", analyzerFilename); // usa l'analyzer custom per il campo "filename"
            analyzerPerField.put("content", analyzerIT); // usa l'analyzer italiano per il campo "content"
            // per Field non specificati usa l'analyzer italiano di default
            Analyzer analyzer = new PerFieldAnalyzerWrapper(analyzerIT, analyzerPerField);
            // apertura della directory per l'indice
            Directory directory = FSDirectory.open(indexDir);
            // configurazione dell'IndexWriter con l'analyzer scelto
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            // per debug, legge l'indice in formato testuale leggibile (non funziona retroattivamente), di default è il formato binario
            // config.setCodec(new SimpleTextCodec());
            // creazione dell'IndexWriter che gestisce l'indicizzazione
            IndexWriter writer = new IndexWriter(directory, config);

            // Scansione dei file .txt
            // genera uno stream per tutti i file con estensione .txt nella directory docsDir
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(docsDir, "*.txt")) {
                // per ogni file trovato
                for (Path file : stream) {
                    // legge il contenuto del file come stringa UTF-8
                    String content = Files.readString(file, StandardCharsets.UTF_8);
                    // crea un nuovo documento Lucene
                    Document doc = new Document();
                    // aggiunge il Field per il nome del file (non analizzato)
                    // Field.Store.YES indica che il valore deve poter essere recuperato dalla ricerca
                    // uso StringField perché serve un match esatto sul nome del file
                    doc.add(new TextField("filename", file.getFileName().toString(), Field.Store.YES));
                    // aggiunge il Field per il contenuto del file (analizzato)
                    // Field.Store.YES indica che il valore deve poter essere recuperato dalla ricerca
                    // uso TextField perché il contenuto deve essere tokenizzato e analizzato dall'analyzer
                    doc.add(new TextField("content", content, Field.Store.YES));
                    // aggiorna (delete + add) il documento nell'indice (se esiste già, altrimenti lo aggiunge come nuovo documento)
                    // uso il nome del file come termine di identificazione univoco
                    writer.updateDocument(new Term("filename", file.getFileName().toString()), doc);
                    System.out.println("Indicizzato: " + file.getFileName());
                }
            }
            // chiude l'IndexWriter, fa implicitamente il commit delle modifiche
            writer.close();
            System.out.println("\nIndicizzazione completata con successo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
