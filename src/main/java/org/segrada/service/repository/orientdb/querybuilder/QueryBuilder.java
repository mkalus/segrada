package org.segrada.service.repository.orientdb.querybuilder;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.segrada.model.prototype.*;
import org.segrada.service.repository.orientdb.OrientDbFileRepository;
import org.segrada.service.repository.orientdb.OrientDbNodeRepository;
import org.segrada.service.repository.orientdb.OrientDbSourceRepository;
import org.segrada.service.repository.orientdb.base.AbstractOrientDbBaseRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class QueryBuilder extends AbstractOrientDbBaseRepository {
    private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

    private final OrientDbNodeRepository nodeRepository;
    private final OrientDbSourceRepository sourceRepository;
    private final OrientDbFileRepository fileRepository;

    /**
     * Constructor
     *
     * @param repositoryFactory
     */
    public QueryBuilder(OrientDbRepositoryFactory repositoryFactory) {
        super(repositoryFactory);

        nodeRepository = repositoryFactory.produceRepository(OrientDbNodeRepository.class);
        sourceRepository = repositoryFactory.produceRepository(OrientDbSourceRepository.class);
        fileRepository = repositoryFactory.produceRepository(OrientDbFileRepository.class);
    }

    /**
     * run saved query and return JSON data
     * @param query to be run
     * @return JSON representation of results
     */
    public JSONArray runSavedQueryAndGetJSONArray(ISavedQuery query) {
        List<ODocument> documents = runSavedQuery(query);

        if (documents == null) {
            return null;
        }

        JSONArray jsonArray = new JSONArray(documents.size());

        // duplicated extraction because not all entities contain toJSON method
        for (ODocument document : documents) {
            switch (document.getClassName()) {
                case "Node":
                    INode node = nodeRepository.convertToEntity(document);
                    jsonArray.put(node.toJSON());
                    break;
                case "Source":
                    ISource source = sourceRepository.convertToEntity(document);
                    jsonArray.put(source.toJSON());
                    break;
                case "File":
                    IFile file = fileRepository.convertToEntity(document);
                    jsonArray.put(file.toJSON());
                    break;
            }
        }

        return jsonArray;
    }

    /**
     * rund saved query and get a list of entities
     * @param query to be run
     * @return list of entities
     */
    public List<SegradaEntity> runSavedQueryAndGetEntities(ISavedQuery query) {
        List<ODocument> documents = runSavedQuery(query);

        if (documents == null) {
            return null;
        }

        List<SegradaEntity> list = new ArrayList<>(documents.size());

        for (ODocument document : documents) {
            switch (document.getClassName()) {
                case "Node":
                    list.add(nodeRepository.convertToEntity(document));
                    break;
                case "Source":
                    list.add(sourceRepository.convertToEntity(document));
                    break;
                case "File":
                    list.add(fileRepository.convertToEntity(document));
                    break;
            }
        }

        return list;
    }

    /**
     * rund saved query and get a list of documents
     * @param query to be run
     * @return list of documents
     */
    public List<ODocument> runSavedQuery(ISavedQuery query) {
        try {
            String queryString = runQueryParts(query);
            logger.info("query = " + queryString);

            initDb();

            return db.command(new OCommandSQL(queryString)).execute();
        } catch (Exception e) {
            logger.error("Error while running runSavedQueryAndGetDocuments on " + query.getId(), e);
        }

        return null;
    }

    /**
     * main worker for query parts: aggregate query and create command to be executed by OrientDB
     * @param query input
     * @return orient db command
     * @throws Exception
     */
    protected String runQueryParts(ISavedQuery query) throws Exception {
        // create json parts and run workers
        JSONArray queryParts = parseSavedQueryData(query);

        QueryPartWorkerFactory factory = new QueryPartWorkerFactory();

        // keeper of commands created
        List<ImmutablePair<String, String>> commandList = new LinkedList<>();

        for (int i = 0; i < queryParts.length(); i++) {
            JSONObject queryPart = queryParts.optJSONObject(i);

            if (queryPart == null) {
                logger.error("Index " + i + " could not be converted to JSON object in query " + query.getId());
                continue;
            }

            String queryPartId = queryPart.optString("id");
            if (queryPartId == null || queryPartId.equals("")) {
                queryPartId = getRandomString();
            }

            // let factory create class
            QueryPartWorker worker = factory.produceQueryPartWorker(queryPart.getString("type"));
            if (worker == null) {
                logger.error("Index " + i + " could not be created to worker in query " + query.getId());
                continue;
            }

            // let worker parse input
            String q = worker.createQuery(queryPart);
            if (q != null && !q.equals("")) {
                commandList.add(new ImmutablePair<>(queryPartId, q));
            }
        }

        // now, how many commands do we have?
        if (commandList.size() == 0) return "";
        if (commandList.size() == 1) {
            // only one command, return this command
            return commandList.get(0).right;
        }

        // multiple commands: create union
        String endQuery = "eq" + getRandomString();
        StringBuilder sb = new StringBuilder("SELECT EXPAND($");
        sb.append(endQuery).append(") LET ");

        for (ImmutablePair<String, String> command : commandList) {
            sb.append("$q").append(command.left).append(" = (").append(command.right).append("), ");
        }

        sb.append("$").append(endQuery).append(" = UNIONALL( ");
        boolean first = true;
        for (ImmutablePair<String, String> command : commandList) {
            if (first) first = false;
            else sb.append(", ");
            sb.append("$q").append(command.left);
        }
        sb.append(" )");

        return sb.toString();
    }

    /**
     * create random string
     * @return random string
     */
    protected String getRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Parse json data saved in query
     * @param query input
     * @return JSON data extracted
     */
    protected JSONArray parseSavedQueryData(ISavedQuery query) throws JSONException {
        return new JSONArray(new JSONTokener(query.getData()));
    }
}
