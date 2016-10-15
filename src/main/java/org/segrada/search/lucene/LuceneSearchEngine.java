package org.segrada.search.lucene;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.BooleanFilterBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.store.Directory;
import org.segrada.search.SearchEngine;
import org.segrada.search.SearchHit;
import org.segrada.service.util.PaginationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private static final Logger logger = LoggerFactory.getLogger(LuceneSearchEngine.class);

	/**
	 * reference to analyzer
	 */
	protected final Analyzer analyzer;

	/**
	 * reference to directory
	 */
	protected final Directory directory;

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
		simpleIndexType.setStored(true);
		simpleIndexType.setIndexOptions(IndexOptions.DOCS);
		simpleIndexType.setTokenized(false);
		simpleIndexType.freeze();

		// stored, indexed and searchable text type
		indexedTextType = new FieldType();
		indexedTextType.setStored(true);
		indexedTextType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		indexedTextType.setStoreTermVectors(true);
		indexedTextType.setStoreTermVectorOffsets(true);
		indexedTextType.setStoreTermVectorPositions(true);
		indexedTextType.freeze();
	}

	@Override
	public synchronized boolean index(String id, String className, String title, String subTitles, String content, String[] tagIds, Integer color, String iconFileIdentifier, float weight) {
		try {
			// init index writer config
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(this.analyzer);

			// create new index writer
			IndexWriter iWriter = new IndexWriter(directory, indexWriterConfig);
			Document doc = new Document();

			doc.add(new Field("id", id, simpleIndexType));
			doc.add(new Field("className", className, simpleIndexType));

			Field field;
			if (title != null) {
				field = new Field("title", title, indexedTextType);
				field.setBoost(10f * weight);
				doc.add(field);
			}

			if (subTitles != null) {
				field = new Field("subTitles", subTitles, indexedTextType);
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

			// add color and icon - just stored
			if (color != null) {
				field = new IntField("color", color, IntField.TYPE_STORED);
				doc.add(field);
			}

			// add color and icon - just stored
			if (iconFileIdentifier != null) {
				field = new Field("iconFileIdentifier", iconFileIdentifier, TextField.TYPE_STORED);
				doc.add(field);
			}

			// create or update document
			iWriter.updateDocument(new Term("id", id), doc);
			iWriter.close();
		} catch (Exception e) {
			logger.error("Could not index document " + id, e);
			return false;
		}

		return true;
	}

	@Override
	public PaginationInfo<SearchHit> search(String searchTerm, Map<String, String> filters) {
		// to avoid NPEs
		if (filters == null) filters = new HashMap<>();

		// set defaults
		int page = 1;
		int entriesPerPage = 20;

		try {
			DirectoryReader iReader = DirectoryReader.open(directory);

			String[] containFields;
			// do we have a filter to contain to certain fields?
			if (filters.containsKey("fields")) {
				String fields = filters.get("fields");
				if (fields.isEmpty()) containFields = new String[]{"title", "subTitles", "content"};
				else if (fields.equalsIgnoreCase("title")) containFields = new String[]{"title"};
				else if (fields.equalsIgnoreCase("subTitles")) containFields = new String[]{"subTitles"};
				else if (fields.equalsIgnoreCase("content")) containFields = new String[]{"content"};
				else if (fields.equalsIgnoreCase("allTitles")) containFields = new String[]{"title", "subTitles"};
				else throw new RuntimeException("fields-Filter " + fields + " is not known.");
			} else containFields = new String[]{"title", "subTitles", "content"};

			// Parse a simple query that searches for "text":
			MultiFieldQueryParser parser = new MultiFieldQueryParser(containFields, analyzer);

			// which operator do we use?
			parser.setDefaultOperator(QueryParser.Operator.AND);
			if (filters.containsKey("operator")) {
				String operator = filters.get("operator");
				if (operator.equalsIgnoreCase("or")) parser.setDefaultOperator(QueryParser.Operator.OR);
				else if (!operator.isEmpty() && !operator.equalsIgnoreCase("and")) throw new RuntimeException("operator-Filter " + operator + " is not and/or.");
			}

			// filters for query
			List<Filter> searchFilters = new ArrayList<>();

			// class filter
			if (filters.containsKey("class") && !filters.get("class").isEmpty()) {
				// multiple classes?
				String[] classes = filters.get("class").split(",");

				// single class
				if (classes.length <= 1) {
					TermQuery categoryQuery = new TermQuery(new Term("className", filters.get("class")));
					searchFilters.add(new QueryWrapperFilter(categoryQuery));
				} else { // multiple classes
					Filter[] categories = new Filter[classes.length];
					for (int i = 0; i < classes.length; i++) {
						categories[i] = new QueryWrapperFilter(new TermQuery(new Term("className", classes[i].trim())));
					}
					BooleanFilter bFilter = new BooleanFilter();
					for (Filter f : categories)
						bFilter.add(f, BooleanClause.Occur.SHOULD);

					// add chained filter
					searchFilters.add(bFilter);
				}
			}

			// tag filter
			if (filters.containsKey("tags") && !filters.get("tags").isEmpty()) {
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
				BooleanFilter bFilter = new BooleanFilter();
				for (Filter f : searchFilters)
					bFilter.add(f, BooleanClause.Occur.MUST);

				filter = bFilter;
			}

			// define query
			Query query = null;
			if (searchTerm != null)
				query = parser.parse(searchTerm);
			if (query == null) query = new MatchAllDocsQuery(); // fallback to match all documents

			// get hits per page
			if (filters.containsKey("limit")) {
				try {
					entriesPerPage = Integer.valueOf(filters.get("limit"));
					if (entriesPerPage <= 0 || entriesPerPage > 1000) entriesPerPage = 20;
				} catch (NumberFormatException e) {
					logger.warn("Could not parse limit " + filters.get("limit") + " to integer", e);
				}
			}

			// get page number
			if (filters.containsKey("page")) {
				try {
					page = Integer.valueOf(filters.get("page"));
				} catch (NumberFormatException e) {
					logger.warn("Could not parse page " + filters.get("page") + " to integer", e);
				}
			}

			// calculate start/stop indexes
			int startIndex = (page - 1) * entriesPerPage;
			int endIndex = page * entriesPerPage;

			IndexSearcher iSearcher = new IndexSearcher(iReader);
			// do search
			TopDocs topDocs = iSearcher.search(query, filter, 1000);

			// update end index
			if (topDocs.scoreDocs.length < endIndex)
				endIndex = topDocs.scoreDocs.length;
			// how many pages do we have?
			int pages = topDocs.scoreDocs.length / entriesPerPage + 1;
			// reset page to sane limit, if needed
			if (page <= 0 || page > pages) page = 1;

			// highlighter
			FastVectorHighlighter highlighter = new FastVectorHighlighter();
			FieldQuery fieldQuery = null;
			// field query for highlighted terms
			if (searchTerm != null)
				fieldQuery = highlighter.getFieldQuery(new QueryParser("content", analyzer).parse(searchTerm), iReader);

			// cycle trough hits
			List<SearchHit> hits = new ArrayList<>();

			for (int i = startIndex ; i < endIndex ; i++) {
				ScoreDoc scoreDoc = topDocs.scoreDocs[i];
				Document hitDoc = iSearcher.doc(scoreDoc.doc);

				SearchHit searchHit = new SearchHit();
				searchHit.setId(hitDoc.get("id"));
				searchHit.setClassName(hitDoc.get("className"));
				searchHit.setTitle(hitDoc.get("title"));
				searchHit.setSubTitles(hitDoc.get("subTitles"));
				searchHit.setTagIds(hitDoc.getValues("tag"));
				String color = hitDoc.get("color");
				searchHit.setColor(color!=null?new Integer(color):null);
				searchHit.setIconFileIdentifier(hitDoc.get("iconFileIdentifier"));
				searchHit.setRelevance(scoreDoc.score);

				// get highlighted components
				if (searchTerm != null) {
					String[] bestFragments = highlighter.getBestFragments(fieldQuery, iReader, scoreDoc.doc, "content", 18, 10);
					searchHit.setHighlightText(bestFragments);
				}

				// add hit
				hits.add(searchHit);
			}

			iReader.close();

			// return pagination info
			return new PaginationInfo<>(page, pages, topDocs.totalHits, entriesPerPage, hits);
		} catch (Throwable e) {
			logger.error("Error in search.", e);
		}

		// return empty list result in order to avoid NPEs
		return new PaginationInfo<>(page, 1, 0, entriesPerPage, new ArrayList<>());
	}

	@Override
	public String[] searchInDocument(String searchTerm, String id) {
		// sanity check
		if (searchTerm == null || id == null || searchTerm.isEmpty() || id.isEmpty()) return new String[]{};

		try {
			DirectoryReader iReader = DirectoryReader.open(directory);
			IndexSearcher iSearcher = new IndexSearcher(iReader);

			// only search content
			MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"content"}, analyzer);

			// set operator and contain by id
			parser.setDefaultOperator(QueryParser.Operator.AND);
			Query query = parser.parse(searchTerm);
			Filter filter = new QueryWrapperFilter(new TermQuery(new Term("id", id)));

			// do search, maximum of 1 document
			TopDocs topDocs = iSearcher.search(query, filter, 1);

			if (topDocs.scoreDocs.length > 0) {
				ScoreDoc scoreDoc = topDocs.scoreDocs[0];

				// get highlighted text
				FastVectorHighlighter highlighter = new FastVectorHighlighter();
				FieldQuery fieldQuery = highlighter.getFieldQuery(new QueryParser("content", analyzer).parse(searchTerm), iReader);

				// return max of 100 highlighted elements
				return highlighter.getBestFragments(fieldQuery, iReader, scoreDoc.doc, "content", 100, 100);
			}
		} catch (Throwable e) {
			logger.error("Error in search.", e);
		}

		return new String[]{};
	}

	@Override
	public synchronized void remove(String id) {
		try {
			// init index writer config
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(this.analyzer);

			// create new index writer
			IndexWriter iWriter = new IndexWriter(directory, indexWriterConfig);

			PhraseQuery query = new PhraseQuery();
			query.add(new Term("id", id));

			iWriter.deleteDocuments(query);

			iWriter.close();
		} catch (Exception e) {
			logger.warn("Error while deleting document " + id, e);
		}
	}

	@Override
	public synchronized SearchHit getById(String id) {
		try {
			DirectoryReader iReader = DirectoryReader.open(directory);
			IndexSearcher iSearcher = new IndexSearcher(iReader);

			PhraseQuery query = new PhraseQuery();
			query.add(new Term("id", id));
			
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
			logger.warn("Error in getById", e);
		}

		return null;
	}

	@Override
	public synchronized void clearAllIndexes() {
		try {
			// init index writer config
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(this.analyzer);

			// create new index writer
			IndexWriter iWriter = new IndexWriter(directory, indexWriterConfig);

			iWriter.deleteAll();

			iWriter.close();
		} catch (Exception e) {
			logger.warn("Error while deleting all entries", e);
		}
	}

	@PreDestroy
	public void destroy() {
		logger.info("Shutting down Lucene index");

		try {
			directory.close();
		} catch (IOException e) {
			logger.warn("Error while closing lucene index", e);
		}
	}
}
