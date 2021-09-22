package org.segrada.service.repository.orientdb.querybuilder;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.service.repository.orientdb.OrientDbFileRepository;
import org.segrada.service.repository.orientdb.OrientDbNodeRepository;
import org.segrada.service.repository.orientdb.OrientDbSourceRepository;
import org.segrada.util.FlexibleDateParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class QueryPartWorkerDynamicQuery implements QueryPartWorker {
    private static final Logger logger = LoggerFactory.getLogger(QueryPartWorkerManualList.class);

    @Override
    public String createQuery(JSONObject data) {
        try {
            String field = data.getString("field");

            StringBuilder sb = new StringBuilder("SELECT FROM ");
            sb.append(field);

            // optionally add stuff
            List<String> constraints = new ArrayList<>();

            // search text
            String searchTerm = data.optString("search");
            if (!(searchTerm == null || searchTerm.equals(""))) {
                switch (field) {
                    case "node":
                        constraints.add(OrientDbNodeRepository.createSearchTermFullText(searchTerm));
                        break;
                    case "source":
                        constraints.add(OrientDbSourceRepository.createSearchTermFullText(searchTerm));
                        break;
                    case "file":
                        constraints.add(OrientDbFileRepository.createSearchTermFullText(searchTerm));
                        break;
                }
            }

            // start/stop dates
            String start = data.optString("start");
            if (!(start == null || start.equals(""))) {
                FlexibleDateParser parser = new FlexibleDateParser();
                Long minJD = parser.inputToJd(start, "G", false);
                if (minJD > Long.MIN_VALUE) constraints.add("minJD >= " + minJD);
            }
            String stop = data.optString("stop");
            if (!(stop == null || stop.equals(""))) {
                FlexibleDateParser parser = new FlexibleDateParser();
                Long maxJD = parser.inputToJd(stop, "G", true);
                if (maxJD < Long.MAX_VALUE) constraints.add("maxJD <= " + maxJD);
            }

            // geo
            // TODO

            // tags
            JSONArray tags = data.optJSONArray("tags");
            if (!(tags == null || tags.length() == 0)) {
                StringBuilder tagSb = new StringBuilder();

                tagSb.append("in('IsTagOf') IN [");
                for (int i = 0; i < tags.length(); i++) {
                    if (i > 0) tagSb.append(", ");
                    tagSb.append(tags.getString(i));
                }
                tagSb.append("]");

                constraints.add(tagSb.toString());
            }

            // explode constraints
            if (constraints.size() > 0) {
                sb.append(" WHERE ");
                boolean first = true;

                for (String constraint : constraints) {
                    if (first) first = false;
                    else sb.append(" AND ");

                    sb.append(constraint);
                }
            }

            // add sorting
            if (field.equals("source")) {
                sb.append(" ORDER BY shortTitleasc ASC");
            } else  {
                sb.append(" ORDER BY titleasc ASC");
            }

            return sb.toString();
        } catch (Exception e) {
            logger.error("Error creating query: ", e);
        }

        return null;
    }
}
