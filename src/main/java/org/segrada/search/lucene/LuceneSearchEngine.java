package org.segrada.search.lucene;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.segrada.search.SearchEngine;
import org.segrada.search.SearchHit;
import org.segrada.search.SearchResult;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Search engine: Lucene implementation
 */
@Singleton // thread safety should work
public class LuceneSearchEngine implements SearchEngine {
	private static final Logger logger = Logger.getLogger(LuceneSearchEngine.class.getName());

	/**
	 * reference to analyzer
	 */
	protected Analyzer analyzer;

	/**
	 * reference to directory
	 */
	protected Directory directory;

	/**
	 * stored and non text indexed field type
	 */
	protected final FieldType simpleIndexType;

	/**
	 * stored, indexed and searchable text type
	 */
	protected final FieldType indexedTextType;

	/**
	 * Constructor
	 *
	 * @param luceneDirectory directory to save hits in
	 * @param luceneAnalyzer  analyzer to use
	 */
	@Inject
	public LuceneSearchEngine(Directory luceneDirectory, Analyzer luceneAnalyzer) {
		this.directory = luceneDirectory;
		this.analyzer = luceneAnalyzer;

		// stored, indexed, but not tokenized
		simpleIndexType = new FieldType();
		simpleIndexType.setIndexed(true);
		simpleIndexType.setStored(true);
		simpleIndexType.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
		simpleIndexType.setTokenized(false);
		simpleIndexType.freeze();

		// stored, indexed and searchable text type
		indexedTextType = new FieldType();
		indexedTextType.setIndexed(true);
		indexedTextType.setStored(true);
		indexedTextType.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		indexedTextType.setStoreTermVectors(true);
		indexedTextType.setStoreTermVectorOffsets(true);
		indexedTextType.setStoreTermVectorPositions(true);
		indexedTextType.freeze();
	}

	@Override
	public synchronized boolean index(String id, String className, String title, String subTitles, String content, String[] tagIds, float weight) {
		try {
			// init index writer config
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_47, this.analyzer);

			// create new index writer
			IndexWriter iWriter = new IndexWriter(directory, indexWriterConfig);
			Document doc = new Document();

			doc.add(new Field("id", id, simpleIndexType));
			doc.add(new Field("className", className, simpleIndexType));

			Field field;
			if (title != null) {
				field = new Field("title", title, TextField.TYPE_STORED);
				field.setBoost(10f * weight);
				doc.add(field);
			}

			if (subTitles != null) {
				field = new Field("subTitles", subTitles, TextField.TYPE_STORED);
				field.setBoost(6f * weight);
				doc.add(field);
			}

			// add content
			if (content == null) content = "";
			field = new Field("content", content, indexedTextType);
			field.setBoost(weight);
			doc.add(field);

			// add tagIds
			if (tagIds != null)
				for (String tagId : tagIds) {
					field = new Field("tag", tagId, simpleIndexType);
					field.setBoost(weight);
					doc.add(field);
				}

			// create or update document
			iWriter.updateDocument(new Term("id", id), doc);
			iWriter.close();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not index document " + id + ": " + e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	public SearchResult search(String searchTerm, Map<String, String> filters) {
		SearchResult searchResult = new SearchResult();

		// to avoid NPEs
		if (filters == null) filters = new HashMap<>();

		try {
			DirectoryReader iReader = DirectoryReader.open(directory);
			IndexSearcher iSearcher = new IndexSearcher(iReader);

			// Parse a simple query that searches for "text":
			MultiFieldQueryParser parser;
			String[] containFields;
			// fo we have a filter to contain to certain fields?
			if (filters.containsKey("fields")) {
				String fields = filters.get("fields");
				if (fields.equalsIgnoreCase("title")) containFields = new String[]{"title"};
				else if (fields.equalsIgnoreCase("subTitles")) containFields = new String[]{"subTitles"};
				else if (fields.equalsIgnoreCase("content")) containFields = new String[]{"content"};
				else if (fields.equalsIgnoreCase("allTitles")) containFields = new String[]{"title", "subTitles"};
				else throw new RuntimeException("fields-Filter " + fields + " is not known.");
			} else containFields = new String[]{"title", "subTitles", "content"};
			parser = new MultiFieldQueryParser(Version.LUCENE_47, containFields, analyzer);

			// which operator do we use?
			if (filters.containsKey("operator")) {
				String operator = filters.get("operator");
				if (operator.equalsIgnoreCase("and")) parser.setDefaultOperator(QueryParser.Operator.AND);
				else if (operator.equalsIgnoreCase("or")) parser.setDefaultOperator(QueryParser.Operator.OR);
				else throw new RuntimeException("operator-Filter " + operator + " is not and/or.");
			}

			// filters for query
			List<Filter> searchFilters = new LinkedList<>();

			// class filter?
			if (filters.containsKey("class")) {
				TermQuery categoryQuery = new TermQuery(new Term("className", filters.get("class")));
				searchFilters.add(new QueryWrapperFilter(categoryQuery));
			}

			// tag filter
			if (filters.containsKey("tags")) {
				// split tags into array
				String[] tags = filters.get("tags").split(",");
				BooleanQuery booleanQuery = new BooleanQuery();
				for (String tag : tags) {
					booleanQuery.add(new TermQuery(new Term("tag", tag.trim())), BooleanClause.Occur.SHOULD);
				}
				searchFilters.add(new QueryWrapperFilter(booleanQuery));
			}

			// create filter - if multiple filters applied, add chained filter
			Filter filter = null;
			if (searchFilters.size() == 1)
				filter = searchFilters.get(0);
			else if (searchFilters.size() > 1) {
				Filter[] filterArray = new Filter[searchFilters.size()];
				searchFilters.toArray(filterArray);
				filter = new ChainedFilter(filterArray, ChainedFilter.AND);
			}

			// define query
			Query query = null;
			if (searchTerm != null)
				query = parser.parse(searchTerm);
			if (query == null) query = new MatchAllDocsQuery(); // fallback to match all documents

			// calculate maximum hits
			int maximum = 1000;
			if (filters.containsKey("limit")) {
				try {
					maximum = Integer.valueOf(filters.get("limit"));
				} catch (NumberFormatException e) {
					logger.warning("Could not parse " + filters.get("limit") + " to integer: " + e.getMessage());
				}
			}

			// do search
			TopDocs topDocs = iSearcher.search(query, filter, maximum);

			searchResult.setTotalHits(topDocs.totalHits); // TODO: better actual hits?
			searchResult.setStartIndex(1); // TODO: fix this - pagination?

			// highlighter
			FastVectorHighlighter highlighter = new FastVectorHighlighter();
			FieldQuery fieldQuery = null;
			// field query for highlighted terms
			if (searchTerm != null)
				fieldQuery = highlighter.getFieldQuery(new QueryParser(Version.LUCENE_47, "content", analyzer).parse(searchTerm), iReader);

			// cycle trough hits
			for (ScoreDoc hit : topDocs.scoreDocs) {
				Document hitDoc = iSearcher.doc(hit.doc);

				SearchHit searchHit = new SearchHit();
				searchHit.setId(hitDoc.get("id"));
				searchHit.setClassName(hitDoc.get("className"));
				searchHit.setTitle(hitDoc.get("title"));
				searchHit.setSubTitles(hitDoc.get("subTitles"));
				searchHit.setTagIds(hitDoc.getValues("tag"));
				searchHit.setRelevance(hit.score);

				// get highlighted components
				if (searchTerm != null) {
					String[] bestFragments = highlighter.getBestFragments(fieldQuery, iReader, hit.doc, "content", 18, 10);
					searchHit.setHighlightText(bestFragments);
				}

				// add hit
				searchResult.addHit(searchHit);
			}

			iReader.close();
		} catch (Throwable e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "Error in search: " + e.getMessage());
		}

		return searchResult;
	}

	@Override
	public synchronized void remove(String id) {
		try {
			// init index writer config
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_47, this.analyzer);

			// create new index writer
			IndexWriter iWriter = new IndexWriter(directory, indexWriterConfig);

			PhraseQuery query = new PhraseQuery();
			query.add(new Term("id", id));

			iWriter.deleteDocuments(query);

			iWriter.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while deleting document " + id + ": " + e.getMessage());
		}
	}

	@Override
	public synchronized SearchHit getById(String id) {
		try {
			DirectoryReader iReader = DirectoryReader.open(directory);
			IndexSearcher iSearcher = new IndexSearcher(iReader);

			PhraseQuery query = new PhraseQuery();
			query.add(new Term("id", id));

			//iSearcher.doc(id);
			TopDocs topDocs = iSearcher.search(query, null, 1);

			// not found?
			if (topDocs.totalHits == 0) {
				iReader.close();
				return null;
			}

			// fetch hit
			Document document = iSearcher.doc(0);

			SearchHit searchHit = new SearchHit();
			searchHit.setId(document.get("id"));
			searchHit.setClassName(document.get("className"));
			searchHit.setTitle(document.get("title"));
			searchHit.setSubTitles(document.get("subTitles"));
			searchHit.setHighlightText(new String[]{document.get("content")});
			searchHit.setTagIds(document.getValues("tag"));

			iReader.close();

			return searchHit;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error in getById: " + e.getMessage());
		}

		return null;
	}

	@Override
	public synchronized void clearAllIndexes() {
		try {
			// init index writer config
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_47, this.analyzer);

			// create new index writer
			IndexWriter iWriter = new IndexWriter(directory, indexWriterConfig);

			iWriter.deleteAll();

			iWriter.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while deleting all entries: " + e.getMessage());
		}
	}

	@PreDestroy
	public void destroy() {
		logger.info("Shutting down Lucene index");

		try {
			directory.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error while closing lucene index: " + e.getMessage());
		}
	}
}
