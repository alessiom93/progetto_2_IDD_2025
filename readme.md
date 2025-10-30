### PROGETTO ###
Questo è un progetto per il secondo homework dell'esame di Ingegneria dei dati anno 2025/2026.
E' un sistema di indicizzazione e ricerca di file testuali con Apache Lucene.

### STRUTTURA PROGETTO ###
La cartella documents contiene 200 file di testo generati dall'IA.
La certella documents_old contiene 2 file ti testo generati da me per i primi test.
La cartella index contiene l'indice dopo il processo di indicizzazione.
La cartella src/main/java/indexer contiene 3 file:
- Index.java: è l'indicizzatore Lucene.
- IndexReader.java: è uno script generato dall'IA per leggere l'indice, solo per debug.
- Search.java: è ll ricercatore Lucene sull'indice .
Il file pom.xml contiene le dipendenze.
Il file readme.md è questo che stai leggendo.

### COMANDI ###
# Compile #
mvn compile
mvn clean compile -U
# Index #
mvn exec:java -Dexec.mainClass="indexer.Index"
# Index Reader #
mvn exec:java -Dexec.mainClass="indexer.IndexReader"
# Search #
mvn exec:java -Dexec.mainClass="indexer.Search"
