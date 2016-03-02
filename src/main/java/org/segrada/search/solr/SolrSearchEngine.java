package org.segrada.search.solr;

import com.google.inject.Singleton;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.segrada.search.SearchEngine;
import org.segrada.search.SearchHit;
import org.segrada.service.util.PaginationInfo;
import org.segrada.session.ApplicationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
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
 * Search engine: Solr implementation
 */
@Singleton // thread safety should work
public class SolrSearchEngine implements SearchEngine {
	//TODO: add test method

	private static final Logger logger = LoggerFactory.getLogger(SolrSearchEngine.class);

	/**
	 * reference to solr client
	 */
	protected SolrClient solr;

	/**
	 * reference to analyzer
	 */
	protected final Analyzer analyzer;

	/**
	 * mappings to field names
	 */
	private final String id;
	private final String className;
	private final String title;
	private final String subTitles;
	private final String content;
	private final String tag;
	private final String color; // should be stored only
	private final String icon; // should be stored only

	/**
	 * Constructor
	 * @param settings of application
	 * @param luceneAnalyzer lucene analyzer to use for parsing search queries
	 */
	public SolrSearchEngine(ApplicationSettings settings, Analyzer luceneAnalyzer) {
		this.analyzer = luceneAnalyzer;

		// define field values from settings
		id = settings.getSetting("solr.field_id", "id");
		className = settings.getSetting("solr.field_className", "className_s");
		title = settings.getSetting("solr.field_title", "title_t");
		subTitles = settings.getSetting("solr.field_subTitles", "subTitles_t");
		content = settings.getSetting("solr.field_content", "content_t");
		tag = settings.getSetting("solr.field_tag", "tag_ss");
		color = settings.getSetting("solr.field_color", "color_s");
		icon = settings.getSetting("solr.field_icon", "icon_s");

		String url = settings.getSetting("solr.server");
		if (url == null || url.isEmpty()) {
			logger.error("Could not connect to Solr server - empty server string."); // should not happen
			return;
		}

		// create client
		solr = new HttpSolrClient(url);

		// try to connect to server
		try {
			solr.ping();
		} catch (Exception e) {
			solr = null; // remove solr
			logger.error("Could not connect to Solr server", e);
		}
	}

	@Override
	public boolean index(String id, String className, String title, String subTitles, String content, String[] tagIds, Integer color, String iconFileIdentifier, float weight) {
		try {
			SolrInputDocument doc = new SolrInputDocument();

			doc.addField(this.id, id);
			doc.addField(this.className, className);

			if (title != null)
				doc.addField(this.title, title, 10f * weight);

			if (subTitles != null)
				doc.addField(this.subTitles, subTitles, 6f * weight);

			// add content
			if (content == null) content = "";
			doc.addField(this.content, content, weight);

			// add tagIds
			if (tagIds != null)
				for (String tagId : tagIds) {
					doc.addField(this.tag, tagId, weight);
				}

			// add color and icon - just stored
			if (color != null)
				doc.addField(this.color, color, 0);

			// add color and icon - just stored
			if (iconFileIdentifier != null)
				doc.addField(this.icon, iconFileIdentifier, 0);

			// add document
			UpdateResponse response = solr.add(doc);

			// commit it - do soft commit
			solr.commit(false, false, true);
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
			// Parse a simple query that searches for "text":
			MultiFieldQueryParser parser;
			String[] containFields;
			// do we have a filter to contain to certain fields?
			if (filters.containsKey("fields")) {
				String fields = filters.get("fields");
				if (fields.isEmpty()) containFields = new String[]{this.title, this.subTitles, this.content};
				else if (fields.equalsIgnoreCase(this.title)) containFields = new String[]{this.title};
				else if (fields.equalsIgnoreCase(this.subTitles)) containFields = new String[]{this.subTitles};
				else if (fields.equalsIgnoreCase(this.content)) containFields = new String[]{this.content};
				else if (fields.equalsIgnoreCase("allTitles")) containFields = new String[]{this.title, this.subTitles};
				else throw new RuntimeException("fields-Filter " + fields + " is not known.");
			} else containFields = new String[]{this.title, this.subTitles, this.content};
			parser = new MultiFieldQueryParser(Version.LUCENE_47, containFields, analyzer);

			// which operator do we use?
			parser.setDefaultOperator(QueryParser.Operator.AND);
			if (filters.containsKey("operator")) {
				String operator = filters.get("operator");
				if (operator.equalsIgnoreCase("or")) parser.setDefaultOperator(QueryParser.Operator.OR);
				else if (!operator.isEmpty() && !operator.equalsIgnoreCase("and")) throw new RuntimeException("operator-Filter " + operator + " is not and/or.");
			}

			// filters for query
			SolrQuery query = new SolrQuery();
			// class filter
			if (filters.containsKey("class") && !filters.get("class").isEmpty()) {
				// multiple classes?
				String[] classes = filters.get("class").split(",");

				// single class
				if (classes.length <= 1) {
					query.addFilterQuery(this.className, filters.get("class"));
				} else { // multiple classes
					String chained = "(";
					for (int i = 0; i < classes.length; i++) {
						if (i > 0) chained += " OR ";
						chained += "className:" + classes[i].trim();
					}
					query.addFilterQuery(this.className, chained + ")");
				}
			}

			// tag filter
			if (filters.containsKey("tags") && !filters.get("tags").isEmpty()) {
				// split tags into array
				String[] tags = filters.get("tags").split(",");
				BooleanQuery booleanQuery = new BooleanQuery();
				for (String tagLocal : tags) {
					booleanQuery.add(new TermQuery(new Term("tag", tagLocal.trim())), BooleanClause.Occur.SHOULD);
				}
				query.addFilterQuery(this.tag, booleanQuery.toString());
			}

			// define query
			Query queryTerm = null;
			if (searchTerm != null)
				queryTerm = parser.parse(searchTerm);
			if (queryTerm == null) queryTerm = new MatchAllDocsQuery(); // fallback to match all documents
			query.setQuery(queryTerm.toString());

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

			// calculate start index
			int startIndex = (page - 1) * entriesPerPage;

			query.setStart(startIndex);
			query.setRows(entriesPerPage);

			query.setFields("*", "score");

			// define highlighting
			query.setHighlight(true);
			query.addHighlightField(this.content);
			query.setHighlightFragsize(18);
			query.setHighlightSnippets(10);
			query.setHighlightSimplePre("<b>");
			query.setHighlightSimplePost("</b>");

			// do query
			QueryResponse response = solr.query(query);
			SolrDocumentList results = response.getResults();

			// how many pages do we have?
			int pages = (int) (results.getNumFound() / entriesPerPage + 1);

			// cycle trough hits
			List<SearchHit> hits = new ArrayList<>();

			for (SolrDocument doc : results) {
				SearchHit searchHit = createHitFromDocument(doc);

				// add score
				Object score = doc.get("score");
				if (score != null && score instanceof Float)
					searchHit.setRelevance((float) score);

				// get highlighted components
				if (searchTerm != null) {
					if (response.getHighlighting().get(searchHit.getId()) != null) {
						List<String> fragments = response.getHighlighting().get(searchHit.getId()).get(this.content);
						String[] bestFragments = new String[fragments.size() > 10?10:fragments.size()];
						for (int i = 0; i < bestFragments.length; i++)
							bestFragments[i] = fragments.get(i);
						searchHit.setHighlightText(bestFragments);
					}
				}

				// add hit
				hits.add(searchHit);
			}

			// return pagination info
			return new PaginationInfo<>(page, pages, (int) results.getNumFound(), entriesPerPage, hits);
		} catch (Throwable e) {
			logger.error("Error in search.", e);
		}

		// return empty list result in order to avoid NPEs
		return new PaginationInfo<>(page, 1, 0, entriesPerPage, new ArrayList<>());
	}

	/**
	 * helper to convert main stuff of solr document to hit
	 * @param doc
	 * @return
	 */
	private SearchHit createHitFromDocument(SolrDocument doc) {
		SearchHit searchHit = new SearchHit();
		searchHit.setId((String) doc.get(this.id));
		searchHit.setClassName((String) doc.get(this.className));
		searchHit.setTitle(getOneValueFromField(doc, this.title));
		searchHit.setSubTitles(getOneValueFromField(doc, this.subTitles));

		// get tags
		if (doc.containsKey(this.tag)) {
			Object o = doc.get(this.tag);
			if (o instanceof List) {
				List<String> l = (List<String>) o;
				String[] tags = new String[l.size()];
				tags = l.toArray(tags);
				searchHit.setTagIds(tags);
			} else
				logger.warn("Unknown type " + o.getClass() + " for field " + this.tag);
		}

		// color
		Object colorLocal = doc.get("color");
		if (colorLocal != null && !(colorLocal instanceof String)) colorLocal = colorLocal.toString();
		searchHit.setColor(colorLocal!=null?new Integer((String)colorLocal):null);

		searchHit.setIconFileIdentifier(getOneValueFromField(doc, this.icon));

		return searchHit;
	}

	/**
	 * helper to retrieve one value from a field
	 * @param doc to examine
	 * @param field to check doc for
	 * @return retrieved string or null
	 */
	private static String getOneValueFromField(SolrDocument doc, String field) {
		// sanity
		if (doc == null || field == null || field.isEmpty() || !doc.containsKey(field)) return null;

		Object o = doc.get(field);
		if (o == null) return null; // should not happen, but anyways

		if (o instanceof List) return (String) ((List)o).get(0);
		if (o instanceof String) return (String) o;

		logger.warn("Unknown type " + o.getClass() + " for field " + field);
		return null;
	}

	@Override
	public String[] searchInDocument(String searchTerm, String id) {
		// sanity check
		if (searchTerm == null || id == null || searchTerm.isEmpty() || id.isEmpty()) return new String[]{};

		try {
			// only search content
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_47, new String[]{"content"}, analyzer);

			SolrQuery query = new SolrQuery();

			// set operator and contain by id
			parser.setDefaultOperator(QueryParser.Operator.AND);
			query.setQuery(parser.parse(searchTerm).toString());

			// filter by id
			query.addFilterQuery("id:" + id);
			query.setRows(1);

			// define highlighting
			query.setHighlight(true);
			query.addHighlightField(this.content);
			query.setHighlightFragsize(100);
			query.setHighlightSnippets(100);
			query.setHighlightSimplePre("<b>");
			query.setHighlightSimplePost("</b>");

			// do query
			QueryResponse response = solr.query(query);
			SolrDocumentList results = response.getResults();

			if (!results.isEmpty()) {
				if (response.getHighlighting().get(id) != null) {
					List<String> fragments = response.getHighlighting().get(id).get(this.content);
					String[] bestFragments = new String[fragments.size() > 100?100:fragments.size()];
					for (int i = 0; i < bestFragments.length; i++)
						bestFragments[i] = fragments.get(i);
					return bestFragments;
				}
			}
		} catch (Throwable e) {
			logger.error("Error in search.", e);
		}

		return new String[]{};
	}

	@Override
	public void remove(String id) {
		try {
			solr.deleteById(id);
			solr.commit();
		} catch (Exception e) {
			logger.error("Solr remove error.", e);
		}
	}

	@Override
	public SearchHit getById(String id) {
		try {
			SolrDocument doc = solr.getById(id);
			SearchHit searchHit = createHitFromDocument(doc);
			searchHit.setHighlightText(new String[]{getOneValueFromField(doc, this.content)});

			return searchHit;
		} catch (Exception e) {
			logger.error("Solr getById error.", e);
		}

		return null;
	}

	@Override
	public void clearAllIndexes() {
		try {
			solr.deleteByQuery("*:*");
			solr.commit();
		} catch (Exception e) {
			logger.error("Solr clearAllIndexes error.", e);
		}
	}
}
