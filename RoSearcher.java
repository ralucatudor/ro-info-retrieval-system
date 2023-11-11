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

public class RoSearcher {
    private static final String FIELD_FROM_INDEX = "content";
    private static final String DEFAULT_QUERY = "incredere";

    public static void main(String[] args) throws IOException, ParseException {
        // Specify the directory where the index is stored
        Directory indexDir = FSDirectory.open(Paths.get(RoIndexer.INDEX_PATH));

        // Create an analyzer to process the query
        Analyzer analyzer = new RoAnalyzer();

        // Create a searcher to search the index
        IndexReader reader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser queryParser = new QueryParser(FIELD_FROM_INDEX, analyzer);

        // Create a query to search for the specified term
//        String queryText = "lucene";
//        TermQuery query = new TermQuery(new Term("content", queryText));

        // Execute the query and get the results
        // 2. query
        String queryString = args.length > 0 ? args[0] : DEFAULT_QUERY;

        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query query = queryParser.parse(queryString);

        int hitsPerPage = 10;
        TopDocs topDocs = searcher.search(query, hitsPerPage);
//        hits
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        // 4. display results
        System.out.println("Found " + scoreDocs.length + " hits.");
//        for(int i=0; i<hits.length; i++) {
//            int docId = hits[i].doc;
//            Document d = searcher.doc(docId);
//            System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
//        }
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docID = scoreDoc.doc;
            Document doc = reader.document(docID);
            System.out.println("Filename: " + doc.get("filename"));
            System.out.println("Score: " + scoreDoc.score);
        }

        // Close the reader to release resources
        reader.close();
    }
}