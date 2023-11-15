import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
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
 *
 * author: Raluca Tudor
 */
public class RoIndexer {
    static final String INDEX_PATH = "index";
    static final String DOC_TEXT_FIELD_NAME = "content";

    /**
     * Default path for the directory holding the documents to be indexed.
     */
    private static final String DEFAULT_DOCS_DIR_PATH_STR = "resources";

    public static void main(String[] args) throws IOException, TikaException, SAXException {
        // Specify the directory to store the index
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_PATH));

        // Use the custom Romanian Analyzer {@link RoAnalyzer} to process the documents.
        Analyzer analyzer = new RoAnalyzer();

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        // If the user wishes to not fully reindex, then use {@code CREATE_OR_APPEND} open mode.
        // indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter indexWriter = new IndexWriter(indexDir, indexWriterConfig);

        // Read the documents and add them to the index
        String docsDirPathString = args.length > 0 ? args[0] : DEFAULT_DOCS_DIR_PATH_STR;

        File[] filesForIndexing = new File(docsDirPathString).listFiles();
        if (filesForIndexing == null) {
            System.out.println("There are no files to index");
            return;
        }

        for (File file : filesForIndexing) {
            if (file.isFile()) {
                System.out.println("Processing file " + file.getName() + " and adding it to the index.");
                indexWriter.addDocument(createAndParseDocumentFrom(file));
            }
        }

        // Close the index writer to save the index.
        indexWriter.close();
    }

    static Document createAndParseDocumentFrom(File file) throws IOException, TikaException, SAXException {
        Document doc = new Document();
        // Add the filename field - using {@code StringField} since we don't want this to be tokenized
        doc.add(new StringField("filename", file.getName(), Field.Store.YES));

        // Extract the text content from the doc using Tika.
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        parser.parse(new FileInputStream(file), handler, metadata, context);

        // Add the content field
        doc.add(new TextField(DOC_TEXT_FIELD_NAME, handler.toString(), Field.Store.YES));
        return doc;
    }
}