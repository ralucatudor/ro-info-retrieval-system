import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Handles searching a query within a previously-created inverted index.
 * <p>
 * author: Raluca Tudor
 */
public class RoSearcher {
    private static final String DEFAULT_QUERY = "incredere";

    public static void main(String[] args) throws IOException, ParseException {
        // Use the same directory as the Ro Indexer. This is where the index was stored.
        Directory indexDir = FSDirectory.open(Paths.get(RoIndexer.INDEX_PATH));

        // Use the custom Romanian Analyzer {@link RoAnalyzer} to process the query.
        Analyzer analyzer = new RoAnalyzer();

        String queryString = args.length > 0 ? args[0] : DEFAULT_QUERY;
        // Parse correspondingly to the text field name used when indexing the docs.
        QueryParser queryParser = new QueryParser(RoIndexer.DOC_TEXT_FIELD_NAME, analyzer);
        Query query = queryParser.parse(queryString);

        int hitsPerPage = 10;
        // Create a searcher to search the index.
        IndexReader indexReader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, hitsPerPage);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        // Display the results found.
        System.out.println("Found " + scoreDocs.length + " hits.");
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docID = scoreDoc.doc;
            Document doc = indexReader.document(docID);
            System.out.println("Filename: " + doc.get("filename"));
            System.out.println("Score: " + scoreDoc.score);
        }

        // Close the index reader to release resources.
        indexReader.close();
    }
}