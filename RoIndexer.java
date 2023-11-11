import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Handles creating inverted index from a set of documents in Romanian.
 */
public class RoIndexer {
    private static final Logger LOG = LogManager.getLogger(RoIndexer.class);

    public static void main(String[] args) throws IOException, TikaException, SAXException {
        // Specify the directory to store the index
        String indexPath = "index";
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));

        // Create an analyzer to process the documents
        Analyzer analyzer = new RoAnalyzer();

        // Create an index writer to write the index
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
//        create or append?
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);

        // Read the documents and add them to the index
        File folder = new File("resources");
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                LOG.info("Processing next document");
                System.out.printf("process");
                Document doc = new Document();
                // Add the filename field - using string field since we don't want this to be tokenized
                doc.add(new StringField("filename", file.getName(), Field.Store.YES));

                // Extract the text content from the document using Apache Tika
                AutoDetectParser parser = new AutoDetectParser();
                BodyContentHandler handler = new BodyContentHandler();
                Metadata metadata = new Metadata();
                ParseContext context = new ParseContext();

                parser.parse(new FileInputStream(file), handler, metadata, context);

                // Add the content field
                doc.add(new TextField("content", handler.toString(), Field.Store.YES));

                writer.addDocument(doc);
            }
        }

        // Close the index writer to save the index
        writer.close();
    }
}