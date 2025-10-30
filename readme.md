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
