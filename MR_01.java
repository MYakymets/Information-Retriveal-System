package avajava;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

@SuppressWarnings("unused")
public class MR_01 {

	public static final String FILES_TO_INDEX_DIRECTORY = "filesToIndex";

	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";

	// Store the index in memory:
	static Directory directory = new RAMDirectory();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		boolean flag = true;
		createIndex();
		while (flag == true) {
			Scanner in = new Scanner(System.in);
			System.out.print("Enter the query (enter 'ex' for exit): ");
			// read entered query
			String inQuery = in.nextLine();
			// compare with "ex" to exit
			if (inQuery.equals("ex"))
				System.exit(0);
			else
				searchIndex(inQuery);
		} // while
	}// main

	public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		// creating of index
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);
		// Get list of files in the folder
		File dir = new File(FILES_TO_INDEX_DIRECTORY);
		File[] files = dir.listFiles();
		// Adding documents
		for (File file : files) {
			Document document = new Document();
			String path = file.getCanonicalPath();
			document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.NOT_ANALYZED));
			Reader reader = new FileReader(file);
			document.add(new Field(FIELD_CONTENTS, reader));
			indexWriter.addDocument(document);
		} // for
		indexWriter.close();
	}// createIndex

	public static void searchIndex(String searchString) throws IOException, ParseException {
		// Search and output of results
		System.out.println("Searching for '" + searchString + "'");
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		IndexReader ireader = IndexReader.open(directory);
		IndexSearcher isearcher = new IndexSearcher(ireader);
		// Parse a simple query that searches for entered string:
		QueryParser parser = new QueryParser(Version.LUCENE_36, FIELD_CONTENTS, analyzer);
		Query query = parser.parse(searchString);
		ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
		System.out.println("Number of hits: " + hits.length);
		// Iterate through the results:
		for (int i = 0; i < hits.length; i++) {
			Document hitDoc = isearcher.doc(hits[i].doc);
			String path = hitDoc.get(FIELD_PATH);
			System.out.println("Hit: " + path);
		} // for
		isearcher.close();
		ireader.close();
	}// searchIndex
}// class MR_01