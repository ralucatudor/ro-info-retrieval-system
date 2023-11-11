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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Handles creating inverted index from a set of documents in Romanian.
 */
public class RoIndexer {
    static final String INDEX_PATH = "index";

    /** Default path for the directory holding the documents to be indexed. */
    private static final String DEFAULT_DOCS_DIR_PATH_STR = "resources";

    public static void main(String[] args) throws IOException, TikaException, SAXException {
        // Specify the directory to store the index
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_PATH));

        // Create an analyzer to process the documents
        Analyzer analyzer = new RoAnalyzer();

        // Create an index writer to write the index
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
//        create or append?
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);

        // Read the documents and add them to the index
        String pathString = args.length > 0 ? args[0] : DEFAULT_DOCS_DIR_PATH_STR;
        File folder = new File(pathString);
        File[] files = folder.listFiles();

        if (files == null) {
            System.out.println("There are no files to index");
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                System.out.println("Processing file " + file.getName() + " and adding it to the index.");
                writer.addDocument(createAndParseDocumentFrom(file));
            }
        }

        // Close the index writer to save the index
        writer.close();
    }

    static Document createAndParseDocumentFrom(File file) throws IOException, TikaException, SAXException {
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
        return doc;
    }
}